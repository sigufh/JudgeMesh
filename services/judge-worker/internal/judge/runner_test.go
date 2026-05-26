package judge

import (
	"context"
	"net/http"
	"testing"
)

func TestSameOutputIgnoresTrailingWhitespaceAndCRLF(t *testing.T) {
	if !sameOutput([]byte("42\r\n\n"), []byte("42\n")) {
		t.Fatalf("expected normalized output to match")
	}
	if sameOutput([]byte("41\n"), []byte("42\n")) {
		t.Fatalf("expected different output to fail")
	}
}

func TestFetchBytesSupportsBase64DataURL(t *testing.T) {
	got, err := fetchBytes(context.Background(), http.DefaultClient, "data:text/plain;base64,SnVkZ2VNZXNoCg==")
	if err != nil {
		t.Fatalf("fetchBytes returned error: %v", err)
	}
	if string(got) != "JudgeMesh\n" {
		t.Fatalf("unexpected payload %q", string(got))
	}
}
