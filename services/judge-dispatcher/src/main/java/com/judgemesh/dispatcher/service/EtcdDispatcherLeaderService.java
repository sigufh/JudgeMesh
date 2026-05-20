package com.judgemesh.dispatcher.service;

import com.judgemesh.dispatcher.config.DispatcherProperties;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.Election;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.election.CampaignResponse;
import io.etcd.jetcd.election.LeaderKey;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@ConditionalOnProperty(name = "judgemesh.dispatcher.mode", havingValue = "etcd")
@RequiredArgsConstructor
public class EtcdDispatcherLeaderService implements DispatcherLeaderService {

    private static final Duration LEASE_ACQUIRE_TIMEOUT = Duration.ofSeconds(15);

    private final Client etcdClient;
    private final DispatcherProperties properties;
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean leader = new AtomicBoolean(false);
    private final String instanceId = resolveInstanceId();

    private volatile long leaseId = -1L;
    private volatile LeaderKey leaderKey;
    private volatile ScheduledFuture<?> keepAliveFuture;
    private volatile Thread electionThread;

    @PostConstruct
    void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        electionThread = new Thread(this::electionLoop, "dispatcher-etcd-election");
        electionThread.setDaemon(true);
        electionThread.start();
    }

    @PreDestroy
    void stop() {
        running.set(false);
        cleanupLeadership("shutdown", null);
        Thread thread = electionThread;
        if (thread != null) {
            thread.interrupt();
            try {
                thread.join(2_000L);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public boolean isLeader() {
        return leader.get();
    }

    @Override
    public String leaderId() {
        return instanceId;
    }

    @Override
    public String mode() {
        return "etcd";
    }

    @Override
    public void stepDown() {
        cleanupLeadership("manual step down", null);
    }

    private void electionLoop() {
        while (running.get()) {
            try {
                electAndHold();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception ex) {
                log.warn("dispatcher etcd election cycle failed, retrying", ex);
            } finally {
                cleanupLeadership("cycle exit", null);
            }

            if (running.get()) {
                sleepQuietly(2_000L);
            }
        }
    }

    private void electAndHold() throws Exception {
        Lease leaseClient = etcdClient.getLeaseClient();
        Election electionClient = etcdClient.getElectionClient();
        long ttlSeconds = Math.max(3, properties.getEtcd().getLeaseTtlSeconds());

        LeaseGrantResponse leaseGrant = leaseClient.grant(ttlSeconds).get(LEASE_ACQUIRE_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
        leaseId = leaseGrant.getID();

        ByteSequence electionName = ByteSequence.from(properties.getEtcd().getLeaderKey(), StandardCharsets.UTF_8);
        ByteSequence candidateValue = ByteSequence.from(instanceId, StandardCharsets.UTF_8);
        CampaignResponse response = electionClient
                .campaign(electionName, leaseId, candidateValue)
                .get(LEASE_ACQUIRE_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
        leaderKey = response.getLeader();
        leader.set(true);
        log.info("dispatcher became etcd leader instanceId={} leaseId={}", instanceId, leaseId);

        startKeepAlive(ttlSeconds);
        waitUntilStepDown();
    }

    private void startKeepAlive(long ttlSeconds) {
        cancelKeepAlive();
        long delaySeconds = Math.max(1L, ttlSeconds / 3L);
        long leaseForKeepAlive = leaseId;
        keepAliveFuture = scheduler.scheduleWithFixedDelay(() -> {
            if (!running.get() || !leader.get() || leaseForKeepAlive <= 0) {
                return;
            }
            try {
                etcdClient.getLeaseClient().keepAliveOnce(leaseForKeepAlive).get(5, TimeUnit.SECONDS);
            } catch (Exception ex) {
                log.warn("dispatcher etcd lease keepalive failed instanceId={} leaseId={}", instanceId, leaseForKeepAlive, ex);
                cleanupLeadership("keepalive failed", ex);
            }
        }, delaySeconds, delaySeconds, TimeUnit.SECONDS);
    }

    private void waitUntilStepDown() throws InterruptedException {
        while (running.get() && leader.get()) {
            Thread.sleep(1_000L);
        }
    }

    private synchronized void cleanupLeadership(String reason, Throwable cause) {
        boolean hadLeader = leader.getAndSet(false);
        if (!hadLeader && leaderKey == null && leaseId <= 0) {
            return;
        }

        cancelKeepAlive();

        if (leaderKey != null) {
            try {
                etcdClient.getElectionClient().resign(leaderKey).get(5, TimeUnit.SECONDS);
            } catch (Exception ex) {
                log.debug("dispatcher etcd resign failed instanceId={} reason={}", instanceId, reason, ex);
            } finally {
                leaderKey = null;
            }
        }

        if (leaseId > 0) {
            try {
                etcdClient.getLeaseClient().revoke(leaseId).get(5, TimeUnit.SECONDS);
            } catch (Exception ex) {
                log.debug("dispatcher etcd revoke failed instanceId={} reason={}", instanceId, reason, ex);
            } finally {
                leaseId = -1L;
            }
        }

        if (hadLeader) {
            if (cause == null) {
                log.info("dispatcher stepped down instanceId={} reason={}", instanceId, reason);
            } else {
                log.info("dispatcher stepped down instanceId={} reason={}", instanceId, reason, cause);
            }
        }
    }

    private synchronized void cancelKeepAlive() {
        ScheduledFuture<?> future = keepAliveFuture;
        keepAliveFuture = null;
        if (future != null) {
            future.cancel(true);
        }
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private String resolveInstanceId() {
        try {
            return InetAddress.getLocalHost().getHostName() + "-" + UUID.randomUUID().toString().substring(0, 8);
        } catch (UnknownHostException ex) {
            return "dispatcher-" + UUID.randomUUID().toString().substring(0, 8);
        }
    }
}
