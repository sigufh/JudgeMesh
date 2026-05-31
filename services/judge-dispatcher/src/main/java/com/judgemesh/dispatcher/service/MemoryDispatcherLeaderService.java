package com.judgemesh.dispatcher.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@Service
@ConditionalOnProperty(name = "judgemesh.dispatcher.mode", havingValue = "memory", matchIfMissing = true)
public class MemoryDispatcherLeaderService implements DispatcherLeaderService {

    private final String leaderId = resolveLeaderId();

    @Override
    public boolean isLeader() {
        return true;
    }

    @Override
    public String leaderId() {
        return leaderId;
    }

    @Override
    public String mode() {
        return "memory";
    }

    private String resolveLeaderId() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            log.debug("failed to resolve hostname", ex);
            return "local-memory-leader";
        }
    }
}
