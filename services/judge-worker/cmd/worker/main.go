// Package main is the judge-worker entrypoint.
//
// 见 docs/design/05-判题流水线.md 与 docs/design/12-五人分工.md(A 主写)。
package main

import (
	"context"
	"errors"
	"log/slog"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/judgemesh/judge-worker/internal/config"
	"github.com/judgemesh/judge-worker/internal/metrics"
	"github.com/judgemesh/judge-worker/internal/server"
)

var (
	version = "0.0.1-SNAPSHOT"
)

func main() {
	logger := slog.New(slog.NewJSONHandler(os.Stdout, nil))
	slog.SetDefault(logger)

	cfg := config.Load()
	metrics.WorkerUp.Set(1)
	logger.Info("judge-worker starting",
		"version", version,
		"listen", cfg.ListenAddr,
		"workerID", cfg.WorkerID)

	srv := server.New(cfg, version)

	httpServer := &http.Server{
		Addr:              cfg.ListenAddr,
		Handler:           srv.Handler(),
		ReadHeaderTimeout: 10 * time.Second,
	}

	go func() {
		if err := httpServer.ListenAndServe(); err != nil && !errors.Is(err, http.ErrServerClosed) {
			logger.Error("http server failed", "err", err)
			os.Exit(1)
		}
	}()

	stop := make(chan os.Signal, 1)
	signal.Notify(stop, syscall.SIGINT, syscall.SIGTERM)
	<-stop
	logger.Info("shutting down")

	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()
	if err := httpServer.Shutdown(ctx); err != nil {
		logger.Error("graceful shutdown failed", "err", err)
		os.Exit(1)
	}
	logger.Info("bye")
}
