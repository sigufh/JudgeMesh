package server

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"log/slog"
	"net/http"
	"os/exec"
	"strings"
	"time"

	"github.com/judgemesh/judge-worker/internal/config"
	"github.com/judgemesh/judge-worker/internal/judge"
	"github.com/judgemesh/judge-worker/internal/metrics"
	"github.com/prometheus/client_golang/prometheus/promhttp"
)

type Server struct {
	cfg          config.Config
	version      string
	runner       *judge.Runner
	slots        chan struct{}
	sandboxReady bool
	sandboxError string
}

func New(cfg config.Config, version string) *Server {
	maxConcurrency := cfg.MaxConcurrency
	if maxConcurrency < 1 {
		maxConcurrency = 1
	}
	sandboxReady, sandboxError := sandboxHealth(cfg)
	return &Server{
		cfg:          cfg,
		version:      version,
		runner:       judge.NewRunner(cfg, version),
		slots:        make(chan struct{}, maxConcurrency),
		sandboxReady: sandboxReady,
		sandboxError: sandboxError,
	}
}

// Handler 用 Go 1.22 ServeMux 模式路由。
func (s *Server) Handler() http.Handler {
	mux := http.NewServeMux()
	mux.HandleFunc("GET /health", s.health)
	mux.HandleFunc("GET /healthz", s.health)
	mux.Handle("GET /metrics", promhttp.Handler())
	mux.HandleFunc("POST /judge", s.judge)
	return mux
}

func (s *Server) health(w http.ResponseWriter, r *http.Request) {
	statusCode := http.StatusOK
	healthStatus := "UP"
	if !s.sandboxReady {
		statusCode = http.StatusServiceUnavailable
		healthStatus = "DOWN"
	} else if len(s.slots) >= cap(s.slots) {
		healthStatus = "SATURATED"
	}
	writeJSON(w, statusCode, map[string]any{
		"status":         healthStatus,
		"workerId":       s.cfg.WorkerID,
		"version":        s.version,
		"inflight":       len(s.slots),
		"maxConcurrency": cap(s.slots),
		"sandboxReady":   s.sandboxReady,
		"error":          s.sandboxError,
	})
}

func (s *Server) judge(w http.ResponseWriter, r *http.Request) {
	var task judge.Task
	if err := json.NewDecoder(r.Body).Decode(&task); err != nil {
		metrics.JudgeTasksTotal.WithLabelValues("unknown", "bad_request").Inc()
		writeJSON(w, http.StatusBadRequest, map[string]string{"error": "bad json: " + err.Error()})
		return
	}
	language := strings.ToLower(strings.TrimSpace(task.Language))
	if language == "" {
		language = "unknown"
	}
	select {
	case s.slots <- struct{}{}:
	default:
		metrics.JudgeTasksTotal.WithLabelValues(language, "busy").Inc()
		writeJSON(w, http.StatusServiceUnavailable, map[string]any{
			"accepted":       false,
			"error":          "worker saturated",
			"maxConcurrency": cap(s.slots),
		})
		return
	}
	slog.Info("judge accepted", "submitId", task.SubmitID, "lang", task.Language)

	// Return ACCEPTED quickly; the runner completes asynchronously and posts the callback.
	go func() {
		defer func() { <-s.slots }()
		s.runner.Run(context.WithoutCancel(r.Context()), task)
	}()

	writeJSON(w, http.StatusAccepted, map[string]any{
		"accepted":       true,
		"submitId":       task.SubmitID,
		"workerId":       s.cfg.WorkerID,
		"workerVer":      s.version,
		"inflight":       len(s.slots),
		"maxConcurrency": cap(s.slots),
	})
}

func writeJSON(w http.ResponseWriter, status int, v any) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	_ = json.NewEncoder(w).Encode(v)
}

func sandboxHealth(cfg config.Config) (bool, string) {
	if cfg.AllowUnsandboxed {
		return true, ""
	}
	if _, err := exec.LookPath("isolate"); err != nil {
		return false, "isolate not found: " + err.Error()
	}

	boxID := int(time.Now().UnixNano() % 1000000)
	boxArg := fmt.Sprintf("--box-id=%d", boxID)
	cmdCtx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	initCmd := exec.CommandContext(cmdCtx, "isolate", boxArg, "--init")
	var output bytes.Buffer
	initCmd.Stdout = &output
	initCmd.Stderr = &output
	if err := initCmd.Run(); err != nil {
		return false, strings.TrimSpace("isolate init failed: " + output.String())
	}

	cleanupCtx, cleanupCancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cleanupCancel()
	cleanupCmd := exec.CommandContext(cleanupCtx, "isolate", boxArg, "--cleanup")
	cleanupCmd.Stdout = &output
	cleanupCmd.Stderr = &output
	if err := cleanupCmd.Run(); err != nil {
		slog.Warn("sandbox cleanup failed during health preflight", "workerId", cfg.WorkerID, "err", err)
	}
	return true, ""
}
