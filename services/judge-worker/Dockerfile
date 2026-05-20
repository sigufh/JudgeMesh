# ─── Stage 1: build Go binary ─────────────────────────────────────
FROM golang:1.23-bookworm AS builder
WORKDIR /src
COPY go.mod ./
RUN go mod download
COPY . .
RUN CGO_ENABLED=0 GOOS=linux go build -o /out/worker ./cmd/worker

# ─── Stage 2: runtime with isolate + 4 toolchains ─────────────────
FROM ubuntu:22.04
ENV DEBIAN_FRONTEND=noninteractive
ENV JUDGE_ALLOW_UNSANDBOXED=false

RUN apt-get update && apt-get install -y --no-install-recommends \
        ca-certificates \
        curl \
        gcc \
        g++ \
        git \
        make \
        openjdk-17-jdk-headless \
        python3 \
        libcap-dev \
        pkg-config \
    && git clone --depth=1 https://github.com/ioi/isolate /tmp/isolate \
    && make -C /tmp/isolate \
    && make -C /tmp/isolate install \
    && rm -rf /tmp/isolate /var/lib/apt/lists/*

COPY --from=builder /out/worker /usr/local/bin/worker

EXPOSE 8090
USER root
ENTRYPOINT ["/usr/local/bin/worker"]
