package judge

// Task 与 services/api/.../message/JudgeTask.java 的 JSON 字段一一对应。
// ⚠️ 任何字段改动必须 A 与 D 双方 PR 双签。
type Task struct {
	SubmitID            int64       `json:"submit_id"`
	ProblemID           int64       `json:"problem_id"`
	Source              string      `json:"source"`
	Language            string      `json:"language"`
	TimeLimitMs         int         `json:"time_limit_ms"`
	MemoryLimitMb       int         `json:"memory_limit_mb"`
	TestcaseManifestURL string      `json:"testcase_manifest_url"`
	Testcases           []TestCase  `json:"testcases"`
	CallbackURL         string      `json:"callback_url"`
	AttemptID           string      `json:"attempt_id"`
	RetryCount          int         `json:"retry_count"`
}

type TestCase struct {
	Name              string `json:"name"`
	InputURL          string `json:"input_url"`
	ExpectedOutputURL string `json:"expected_output_url"`
}

// Result 对应 services/api/.../message/JudgeResult.java。
type Result struct {
	SubmitID      int64        `json:"submit_id"`
	Status        string       `json:"status"`
	Message       string       `json:"message,omitempty"`
	Cases         []CaseResult `json:"cases,omitempty"`
	TimeUsedMs    int          `json:"time_used_ms"`
	MemoryUsedKb  int          `json:"memory_used_kb"`
	WorkerID      string       `json:"worker_id"`
	WorkerVersion string       `json:"worker_version"`
	AttemptID     string       `json:"attempt_id,omitempty"`
}

type CaseResult struct {
	Name     string `json:"name"`
	Status   string `json:"status"`
	TimeMs   int    `json:"time_ms"`
	MemoryKb int    `json:"memory_kb"`
	Stderr   string `json:"stderr,omitempty"`
}
