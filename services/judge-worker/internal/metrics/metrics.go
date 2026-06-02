package metrics

import (
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"
)

var (
	WorkerUp = promauto.NewGauge(prometheus.GaugeOpts{
		Name: "oj_worker_up",
		Help: "1 if the judge worker process is alive.",
	})

	JudgeTasksTotal = promauto.NewCounterVec(prometheus.CounterOpts{
		Name: "oj_judge_total",
		Help: "Judge tasks handled by language and final verdict.",
	}, []string{"language", "verdict"})

	JudgeDurationSeconds = promauto.NewHistogramVec(prometheus.HistogramOpts{
		Name:    "oj_judge_duration_seconds",
		Help:    "End-to-end judge task duration in seconds.",
		Buckets: []float64{0.1, 0.5, 1, 2, 5, 10, 30},
	}, []string{"language"})

	JudgeInflight = promauto.NewGauge(prometheus.GaugeOpts{
		Name: "oj_judge_inflight",
		Help: "Judge tasks currently executing on this worker.",
	})
)
