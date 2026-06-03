package com.judgemesh.dispatcher.service;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.support.CloseableClient;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class LeaderElectionService implements SmartInitializingSingleton, DisposableBean {
    private final AtomicBoolean leader = new AtomicBoolean(true);
    private final AtomicBoolean lockInFlight = new AtomicBoolean(false);
    private final String podName;
    private final boolean etcdEnabled;
    private final boolean failOpen;
    private final String[] endpoints;
    private final String leaderKey;
    private final String leaderHolderKey;
    private final long leaseTtlSeconds;
    private final ScheduledExecutorService executor;
    private final List<LeadershipListener> listeners = new CopyOnWriteArrayList<>();

    private volatile String mode = "local-single-leader";
    private volatile Instant lastChangedAt = Instant.now();
    private volatile Instant pausedUntil = Instant.EPOCH;
    private volatile Client client;
    private volatile CloseableClient keepAlive;
    private volatile long leaseId;
    private volatile ByteSequence lockKey;

    public LeaderElectionService(
            @Value("${HOSTNAME:dispatcher-local}") String podName,
            @Value("${judgemesh.etcd.enabled:false}") boolean etcdEnabled,
            @Value("${judgemesh.etcd.fail-open:true}") boolean failOpen,
            @Value("${judgemesh.etcd.endpoints:http://127.0.0.1:2379}") String endpoints,
            @Value("${judgemesh.etcd.leader-key:/judgemesh/dispatcher/leader}") String leaderKey,
            @Value("${judgemesh.etcd.lease-ttl-seconds:10}") long leaseTtlSeconds) {
        this.podName = podName;
        this.etcdEnabled = etcdEnabled;
        this.failOpen = failOpen;
        this.endpoints = Arrays.stream(endpoints.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toArray(String[]::new);
        this.leaderKey = leaderKey;
        this.leaderHolderKey = leaderKey + "/holder";
        this.leaseTtlSeconds = Math.max(5, leaseTtlSeconds);
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "dispatcher-leader-election");
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (!etcdEnabled) {
            leader.set(true);
            mode = "local-single-leader";
            notifyListeners();
            return;
        }
        leader.set(false);
        mode = failOpen ? "etcd-pending-fail-open" : "etcd-pending";
        notifyListeners();
        executor.scheduleWithFixedDelay(this::ensureElection, 0,
                Math.max(1, leaseTtlSeconds / 3), TimeUnit.SECONDS);
    }

    public boolean isLeader() {
        return leader.get();
    }

    public void addListener(LeadershipListener listener) {
        listeners.add(listener);
    }

    public Map<String, Object> status() {
        Map<String, Object> status = new LinkedHashMap<>();
        String currentLeader = currentLeader();
        status.put("leader", currentLeader);
        status.put("currentLeader", currentLeader);
        status.put("self", podName);
        status.put("isLeader", leader.get());
        status.put("mode", mode);
        status.put("leaseId", leaseId);
        status.put("leaderKey", leaderKey);
        status.put("lastChangedAt", lastChangedAt.toString());
        return status;
    }

    public void relinquishForChaos() {
        pausedUntil = Instant.now().plus(Duration.ofSeconds(leaseTtlSeconds));
        unlock();
        leader.set(false);
        mode = etcdEnabled ? "etcd-chaos-paused" : "local-chaos-paused";
        lastChangedAt = Instant.now();
        notifyListeners();
    }

    public void becomeLeader() {
        pausedUntil = Instant.EPOCH;
        if (!etcdEnabled) {
            leader.set(true);
            mode = "local-single-leader";
            lastChangedAt = Instant.now();
            notifyListeners();
            return;
        }
        ensureElection();
    }

    private void ensureElection() {
        if (Instant.now().isBefore(pausedUntil) || leader.get() || lockInFlight.get()) {
            return;
        }
        try {
            Client etcd = client();
            if (leaseId == 0) {
                leaseId = etcd.getLeaseClient().grant(leaseTtlSeconds)
                        .get(2, TimeUnit.SECONDS)
                        .getID();
                keepAlive = etcd.getLeaseClient().keepAlive(leaseId, new KeepAliveObserver());
            }
            lockInFlight.set(true);
            etcd.getLockClient()
                    .lock(bs(leaderKey), leaseId)
                    .orTimeout(leaseTtlSeconds, TimeUnit.SECONDS)
                    .whenComplete((response, error) -> {
                        lockInFlight.set(false);
                        if (error != null) {
                            handleEtcdUnavailable();
                            return;
                        }
                        lockKey = response.getKey();
                        try {
                            publishLeaderHolder(etcd);
                        } catch (Exception ex) {
                            unlock();
                            handleEtcdUnavailable();
                            return;
                        }
                        leader.set(true);
                        mode = "etcd-lease";
                        lastChangedAt = Instant.now();
                        notifyListeners();
                    });
        } catch (Exception ignored) {
            lockInFlight.set(false);
            handleEtcdUnavailable();
        }
    }

    private Client client() {
        Client current = client;
        if (current != null) {
            return current;
        }
        if (endpoints.length == 0) {
            throw new IllegalStateException("no etcd endpoints configured");
        }
        Client created = Client.builder().endpoints(endpoints).build();
        client = created;
        return created;
    }

    private void handleEtcdUnavailable() {
        if (failOpen) {
            leader.set(true);
            mode = "local-fallback-etcd-unavailable";
        } else {
            leader.set(false);
            mode = "etcd-unavailable-fail-closed";
        }
        resetLease();
        lastChangedAt = Instant.now();
        notifyListeners();
    }

    private String currentLeader() {
        if (!etcdEnabled || leader.get()) {
            return podName;
        }
        Client etcd = client;
        if (etcd == null) {
            return null;
        }
        try {
            var response = etcd.getKVClient().get(bs(leaderHolderKey)).get(2, TimeUnit.SECONDS);
            if (!response.getKvs().isEmpty()) {
                return response.getKvs().get(0).getValue().toString(StandardCharsets.UTF_8);
            }
        } catch (Exception ignored) {
            // Status should stay best-effort even when etcd is degraded.
        }
        return null;
    }

    private void publishLeaderHolder(Client etcd) throws Exception {
        if (leaseId == 0) {
            return;
        }
        etcd.getKVClient().put(
                bs(leaderHolderKey),
                bs(podName),
                PutOption.builder().withLeaseId(leaseId).build())
                .get(2, TimeUnit.SECONDS);
    }

    private void unlock() {
        Client etcd = client;
        ByteSequence key = lockKey;
        if (etcd != null && key != null) {
            try {
                etcd.getLockClient().unlock(key).get(2, TimeUnit.SECONDS);
            } catch (Exception ignored) {
                // Losing the lease is enough to release the distributed lock.
            }
        }
        resetLease();
    }

    private void resetLease() {
        CloseableClient stream = keepAlive;
        keepAlive = null;
        if (stream != null) {
            stream.close();
        }
        Client etcd = client;
        long currentLeaseId = leaseId;
        leaseId = 0;
        lockKey = null;
        if (etcd != null && currentLeaseId != 0) {
            try {
                etcd.getLeaseClient().revoke(currentLeaseId).get(2, TimeUnit.SECONDS);
            } catch (Exception ignored) {
                // The server may already have expired it.
            }
        }
    }

    @Override
    public void destroy() {
        executor.shutdownNow();
        unlock();
        Client etcd = client;
        if (etcd != null) {
            etcd.close();
        }
    }

    private void notifyListeners() {
        boolean currentLeader = leader.get();
        String currentMode = mode;
        for (LeadershipListener listener : listeners) {
            listener.onLeadershipChanged(currentLeader, currentMode);
        }
    }

    private ByteSequence bs(String value) {
        return ByteSequence.from(value, StandardCharsets.UTF_8);
    }

    private final class KeepAliveObserver implements StreamObserver<io.etcd.jetcd.lease.LeaseKeepAliveResponse> {
        @Override
        public void onNext(io.etcd.jetcd.lease.LeaseKeepAliveResponse response) {
            // The streaming keepalive itself is the lease health signal.
        }

        @Override
        public void onError(Throwable throwable) {
            leader.set(failOpen);
            mode = failOpen ? "local-fallback-etcd-keepalive-lost" : "etcd-keepalive-lost";
            resetLease();
            lastChangedAt = Instant.now();
            notifyListeners();
        }

        @Override
        public void onCompleted() {
            leader.set(failOpen);
            mode = failOpen ? "local-fallback-etcd-keepalive-completed" : "etcd-keepalive-completed";
            resetLease();
            lastChangedAt = Instant.now();
            notifyListeners();
        }
    }

    public interface LeadershipListener {
        void onLeadershipChanged(boolean leader, String mode);
    }
}
