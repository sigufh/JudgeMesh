package judge

import (
	"bytes"
	"context"
	"encoding/base64"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"log/slog"
	"net/http"
	"net/url"
	"os"
	"os/exec"
	"path/filepath"
	"strconv"
	"strings"
	"time"
	"unicode"

	"github.com/judgemesh/judge-worker/internal/config"
	"github.com/judgemesh/judge-worker/internal/metrics"
)

type Runner struct {
	cfg     config.Config
	version string
	client  *http.Client
}

func NewRunner(cfg config.Config, version string) *Runner {
	return &Runner{
		cfg:     cfg,
		version: version,
		client:  &http.Client{Timeout: 10 * time.Second},
	}
}

func (r *Runner) Run(ctx context.Context, t Task) {
	slog.Info("judge started",
		"submitId", t.SubmitID,
		"problemId", t.ProblemID,
		"language", t.Language,
		"cases", len(t.Testcases))

	start := time.Now()
	result := r.execute(ctx, t)
	metrics.JudgeTasksTotal.WithLabelValues(result.Status).Inc()
	metrics.JudgeDurationSeconds.Observe(time.Since(start).Seconds())
	if err := r.postResult(ctx, t.CallbackURL, result); err != nil {
		slog.Error("callback failed", "submitId", t.SubmitID, "err", err)
	}
}

func (r *Runner) execute(ctx context.Context, t Task) Result {
	result := Result{
		SubmitID:      t.SubmitID,
		Status:        "SE",
		WorkerID:      r.cfg.WorkerID,
		WorkerVersion: r.version,
	}
	if len(t.Testcases) == 0 {
		result.Message = "no testcases"
		return result
	}

	workDir, err := os.MkdirTemp(r.cfg.WorkDir, fmt.Sprintf("judgemesh-%d-*", t.SubmitID))
	if err != nil {
		result.Message = "create workdir: " + err.Error()
		return result
	}
	defer func() {
		if err := os.RemoveAll(workDir); err != nil {
			slog.Warn("cleanup failed", "workDir", workDir, "err", err)
		}
	}()

	run, err := r.prepareExecutable(ctx, workDir, t)
	if err != nil {
		result.Status = "CE"
		result.Message = err.Error()
		return result
	}

	cases := make([]CaseResult, 0, len(t.Testcases))
	finalStatus := "AC"
	for _, tc := range t.Testcases {
		caseResult := r.runCase(ctx, workDir, run, t, tc)
		cases = append(cases, caseResult)
		if caseResult.TimeMs > result.TimeUsedMs {
			result.TimeUsedMs = caseResult.TimeMs
		}
		if caseResult.MemoryKb > result.MemoryUsedKb {
			result.MemoryUsedKb = caseResult.MemoryKb
		}
		if caseResult.Status != "AC" && finalStatus == "AC" {
			finalStatus = caseResult.Status
			result.Message = caseResult.Stderr
		}
		if caseResult.Status != "AC" {
			break
		}
	}
	result.Status = finalStatus
	result.Cases = cases
	return result
}

func (r *Runner) prepareExecutable(ctx context.Context, workDir string, t Task) (runSpec, error) {
	lang := strings.ToLower(strings.TrimSpace(t.Language))
	switch lang {
	case "c":
		source := filepath.Join(workDir, "main.c")
		if err := os.WriteFile(source, []byte(t.Source), 0o600); err != nil {
			return runSpec{}, err
		}
		bin := filepath.Join(workDir, "main")
		if out, err := runCommand(ctx, workDir, 15*time.Second, "gcc", "-O2", "-std=c11", source, "-o", bin); err != nil {
			return runSpec{}, fmt.Errorf("%s", out)
		}
		return runSpec{Command: bin}, nil
	case "cpp", "c++":
		source := filepath.Join(workDir, "main.cpp")
		if err := os.WriteFile(source, []byte(t.Source), 0o600); err != nil {
			return runSpec{}, err
		}
		bin := filepath.Join(workDir, "main")
		if out, err := runCommand(ctx, workDir, 15*time.Second, "g++", "-O2", "-std=c++17", source, "-o", bin); err != nil {
			return runSpec{}, fmt.Errorf("%s", out)
		}
		return runSpec{Command: bin}, nil
	case "java":
		source := filepath.Join(workDir, "Main.java")
		if err := os.WriteFile(source, []byte(t.Source), 0o600); err != nil {
			return runSpec{}, err
		}
		if out, err := runCommand(ctx, workDir, 15*time.Second, "javac", source); err != nil {
			return runSpec{}, fmt.Errorf("%s", out)
		}
		return runSpec{Command: "java", Args: []string{"-Xmx" + fmt.Sprint(t.MemoryLimitMb) + "m", "Main"}}, nil
	case "py", "python", "python3":
		source := filepath.Join(workDir, "main.py")
		if err := os.WriteFile(source, []byte(t.Source), 0o600); err != nil {
			return runSpec{}, err
		}
		return runSpec{Command: "python3", Args: []string{source}}, nil
	default:
		return runSpec{}, fmt.Errorf("unsupported language %q", t.Language)
	}
}

func (r *Runner) runCase(ctx context.Context, workDir string, run runSpec, t Task, tc TestCase) CaseResult {
	input, err := fetchBytes(ctx, r.client, tc.InputURL)
	if err != nil {
		return CaseResult{Name: tc.Name, Status: "SE", Stderr: "fetch input: " + err.Error()}
	}
	expected, err := fetchBytes(ctx, r.client, tc.ExpectedOutputURL)
	if err != nil {
		return CaseResult{Name: tc.Name, Status: "SE", Stderr: "fetch expected output: " + err.Error()}
	}

	timeout := time.Duration(max(t.TimeLimitMs*2, 1000)) * time.Millisecond
	stdout, stderr, elapsed, timedOut, err := r.runPrepared(ctx, workDir, run, t, input, timeout)
	if timedOut {
		return CaseResult{Name: tc.Name, Status: "TLE", TimeMs: elapsed, Stderr: "time limit exceeded"}
	}
	if err != nil {
		stderrText := strings.TrimSpace(string(stderr))
		if stderrText == "" {
			stderrText = err.Error()
		}
		return CaseResult{Name: tc.Name, Status: "RE", TimeMs: elapsed, Stderr: stderrText}
	}
	if !sameOutput(stdout, expected) {
		return CaseResult{Name: tc.Name, Status: "WA", TimeMs: elapsed, Stderr: "wrong answer"}
	}
	return CaseResult{Name: tc.Name, Status: "AC", TimeMs: elapsed, MemoryKb: 0}
}

func (r *Runner) runPrepared(ctx context.Context, workDir string, run runSpec, t Task, input []byte, timeout time.Duration) ([]byte, []byte, int, bool, error) {
	if _, err := exec.LookPath("isolate"); err == nil {
		return r.runIsolated(ctx, workDir, run, t, input, timeout)
	}
	if !r.cfg.AllowUnsandboxed {
		return nil, []byte("isolate is required; set JUDGE_ALLOW_UNSANDBOXED=true only for local development"), 0, false, errors.New("sandbox unavailable")
	}
	return runLocal(ctx, workDir, run, input, timeout)
}

func runLocal(ctx context.Context, workDir string, run runSpec, input []byte, timeout time.Duration) ([]byte, []byte, int, bool, error) {
	start := time.Now()
	cmdCtx, cancel := context.WithTimeout(ctx, timeout)
	defer cancel()
	cmd := exec.CommandContext(cmdCtx, run.Command, run.Args...)
	cmd.Dir = workDir
	cmd.Stdin = bytes.NewReader(input)
	var stdout, stderr bytes.Buffer
	cmd.Stdout = &stdout
	cmd.Stderr = &stderr
	err := cmd.Run()
	elapsed := int(time.Since(start).Milliseconds())
	return stdout.Bytes(), stderr.Bytes(), elapsed, errors.Is(cmdCtx.Err(), context.DeadlineExceeded), err
}

func (r *Runner) runIsolated(ctx context.Context, workDir string, run runSpec, t Task, input []byte, timeout time.Duration) ([]byte, []byte, int, bool, error) {
	start := time.Now()
	boxID := int(time.Now().UnixNano() % 1000000)
	boxArg := "--box-id=" + strconv.Itoa(boxID)
	baseArgs := append([]string{boxArg}, strings.Fields(r.cfg.IsolateExtraArgs)...)
	if _, err := runCommand(ctx, workDir, 5*time.Second, "isolate", append(baseArgs, "--init")...); err != nil {
		if r.cfg.AllowUnsandboxed {
			return runLocal(ctx, workDir, run, input, timeout)
		}
		return nil, []byte("isolate init failed: " + err.Error()), 0, false, err
	}
	defer func() {
		if _, err := runCommand(context.Background(), workDir, 5*time.Second, "isolate", append(baseArgs, "--cleanup")...); err != nil {
			slog.Warn("isolate cleanup failed", "boxID", boxID, "err", err)
		}
	}()

	command, args, err := sandboxCommand(workDir, run)
	if err != nil {
		return nil, []byte(err.Error()), 0, false, err
	}
	runArgs := append([]string{}, baseArgs...)
	runArgs = append(runArgs,
		"--time="+fmt.Sprintf("%.3f", timeout.Seconds()),
		"--wall-time="+fmt.Sprintf("%.3f", timeout.Seconds()+1),
		"--mem="+strconv.Itoa(max(t.MemoryLimitMb*1024, 65536)),
		"--dir=/work="+workDir+":rw",
		"--run",
		"--",
		command,
	)
	runArgs = append(runArgs, args...)
	cmdCtx, cancel := context.WithTimeout(ctx, timeout+time.Second)
	defer cancel()
	cmd := exec.CommandContext(cmdCtx, "isolate", runArgs...)
	cmd.Stdin = bytes.NewReader(input)
	var stdout, stderr bytes.Buffer
	cmd.Stdout = &stdout
	cmd.Stderr = &stderr
	err = cmd.Run()
	elapsed := int(time.Since(start).Milliseconds())
	return stdout.Bytes(), stderr.Bytes(), elapsed, errors.Is(cmdCtx.Err(), context.DeadlineExceeded), err
}

func (r *Runner) postResult(ctx context.Context, callbackURL string, result Result) error {
	if callbackURL == "" {
		return nil
	}
	body, err := json.Marshal(result)
	if err != nil {
		return err
	}
	req, err := http.NewRequestWithContext(ctx, http.MethodPost, callbackURL, bytes.NewReader(body))
	if err != nil {
		return err
	}
	req.Header.Set("Content-Type", "application/json")
	resp, err := r.client.Do(req)
	if err != nil {
		return err
	}
	defer resp.Body.Close()
	if resp.StatusCode < 200 || resp.StatusCode >= 300 {
		return fmt.Errorf("callback status %d", resp.StatusCode)
	}
	return nil
}

func fetchBytes(ctx context.Context, client *http.Client, rawURL string) ([]byte, error) {
	if strings.HasPrefix(rawURL, "data:") {
		parts := strings.SplitN(rawURL, ",", 2)
		if len(parts) != 2 {
			return nil, fmt.Errorf("bad data url")
		}
		if strings.Contains(parts[0], ";base64") {
			return base64.StdEncoding.DecodeString(parts[1])
		}
		decoded, err := url.QueryUnescape(parts[1])
		if err != nil {
			return nil, err
		}
		return []byte(decoded), nil
	}
	if strings.HasPrefix(rawURL, "file://") {
		u, err := url.Parse(rawURL)
		if err != nil {
			return nil, err
		}
		return os.ReadFile(u.Path)
	}
	req, err := http.NewRequestWithContext(ctx, http.MethodGet, rawURL, nil)
	if err != nil {
		return nil, err
	}
	resp, err := client.Do(req)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()
	if resp.StatusCode < 200 || resp.StatusCode >= 300 {
		return nil, fmt.Errorf("status %d", resp.StatusCode)
	}
	return io.ReadAll(resp.Body)
}

func runCommand(ctx context.Context, dir string, timeout time.Duration, name string, args ...string) (string, error) {
	cmdCtx, cancel := context.WithTimeout(ctx, timeout)
	defer cancel()
	cmd := exec.CommandContext(cmdCtx, name, args...)
	cmd.Dir = dir
	var out bytes.Buffer
	cmd.Stdout = &out
	cmd.Stderr = &out
	err := cmd.Run()
	if errors.Is(cmdCtx.Err(), context.DeadlineExceeded) {
		return out.String(), context.DeadlineExceeded
	}
	return out.String(), err
}

func sameOutput(out, ans []byte) bool {
	normalize := func(in []byte) string {
		s := strings.ReplaceAll(string(in), "\r\n", "\n")
		return strings.TrimRightFunc(s, unicode.IsSpace)
	}
	return normalize(out) == normalize(ans)
}

type runSpec struct {
	Command string
	Args    []string
}

func sandboxCommand(workDir string, run runSpec) (string, []string, error) {
	command, err := sandboxPath(workDir, run.Command)
	if err != nil {
		return "", nil, err
	}
	args := make([]string, 0, len(run.Args))
	for _, arg := range run.Args {
		mapped, err := sandboxArg(workDir, arg)
		if err != nil {
			return "", nil, err
		}
		args = append(args, mapped)
	}
	return command, args, nil
}

func sandboxArg(workDir, value string) (string, error) {
	if filepath.IsAbs(value) {
		return sandboxPath(workDir, value)
	}
	return value, nil
}

func sandboxPath(workDir, value string) (string, error) {
	if filepath.IsAbs(value) {
		if rel, err := filepath.Rel(workDir, value); err == nil && rel != "." && !strings.HasPrefix(rel, "..") {
			return "/work/" + filepath.ToSlash(rel), nil
		}
		return value, nil
	}
	path, err := exec.LookPath(value)
	if err != nil {
		return "", err
	}
	return path, nil
}

func max(a, b int) int {
	if a > b {
		return a
	}
	return b
}
