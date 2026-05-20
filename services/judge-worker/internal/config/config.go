package config

import (
	"os"
)

type Config struct {
	ListenAddr string
	WorkerID   string

	RabbitMQURL      string
	SubmitQueue      string
	CallbackBase     string
	IsolateBoxRoot   string
	WorkDir          string
	AllowUnsandboxed bool
	IsolateExtraArgs string
}

func Load() Config {
	return Config{
		ListenAddr:       getenv("LISTEN_ADDR", ":8090"),
		WorkerID:         getenv("WORKER_ID", "worker-local"),
		RabbitMQURL:      getenv("RABBITMQ_URL", "amqp://judgemesh:judgemesh@127.0.0.1:5672/"),
		SubmitQueue:      getenv("SUBMIT_QUEUE", "submit.queue"),
		CallbackBase:     getenv("CALLBACK_BASE", "http://submit-service:8083"),
		IsolateBoxRoot:   getenv("ISOLATE_BOX_ROOT", "/var/local/lib/isolate"),
		WorkDir:          getenv("JUDGE_WORK_DIR", ""),
		AllowUnsandboxed: getenv("JUDGE_ALLOW_UNSANDBOXED", "true") == "true",
		IsolateExtraArgs: getenv("ISOLATE_EXTRA_ARGS", ""),
	}
}

func getenv(k, def string) string {
	if v := os.Getenv(k); v != "" {
		return v
	}
	return def
}
