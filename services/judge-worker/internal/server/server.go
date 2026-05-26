package server

import (
	"context"
	"encoding/json"
	"log/slog"
	"net/http"

	"github.com/judgemesh/judge-worker/internal/config"
	"github.com/judgemesh/judge-worker/internal/judge"
	"github.com/judgemesh/judge-worker/internal/metrics"
	"github.com/prometheus/client_golang/prometheus/promhttp"
)

type Server struct {
	cfg     config.Config
	version string
	runner  *judge.Runner
}

func New(cfg config.Config, version string) *Server {
	return &Server{
		cfg:     cfg,
		version: version,
		runner:  judge.NewRunner(cfg, version),
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
	writeJSON(w, http.StatusOK, map[string]any{
		"status":   "UP",
		"workerId": s.cfg.WorkerID,
		"version":  s.version,
	})
}

func (s *Server) judge(w http.ResponseWriter, r *http.Request) {
	var task judge.Task
	if err := json.NewDecoder(r.Body).Decode(&task); err != nil {
		metrics.JudgeTasksTotal.WithLabelValues("BAD_REQUEST").Inc()
		writeJSON(w, http.StatusBadRequest, map[string]string{"error": "bad json: " + err.Error()})
		return
	}
	slog.Info("judge accepted", "submitId", task.SubmitID, "lang", task.Language)

	// Return ACCEPTED quickly; the runner completes asynchronously and posts the callback.
	go s.runner.Run(context.WithoutCancel(r.Context()), task)

	writeJSON(w, http.StatusAccepted, map[string]any{
		"accepted":  true,
		"submitId":  task.SubmitID,
		"workerId":  s.cfg.WorkerID,
		"workerVer": s.version,
	})
}

func writeJSON(w http.ResponseWriter, status int, v any) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	_ = json.NewEncoder(w).Encode(v)
}
