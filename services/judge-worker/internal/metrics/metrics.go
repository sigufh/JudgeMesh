package metrics

import (
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"
)

var (
	WorkerUp = promauto.NewGauge(prometheus.GaugeOpts{
		Name: "judge_worker_up",
		Help: "1 if the judge worker process is alive.",
	})

	JudgeTasksTotal = promauto.NewCounterVec(prometheus.CounterOpts{
		Name: "judge_worker_tasks_total",
		Help: "Judge tasks handled by final status.",
	}, []string{"status"})

	JudgeDurationSeconds = promauto.NewHistogram(prometheus.HistogramOpts{
		Name:    "judge_worker_task_duration_seconds",
		Help:    "End-to-end judge task duration in seconds.",
		Buckets: prometheus.DefBuckets,
	})
)
