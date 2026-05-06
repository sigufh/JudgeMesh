package com.judgemesh.dispatcher.service;

public interface DispatcherLeaderService {

    boolean isLeader();

    String leaderId();

    String mode();

    default void stepDown() {
        // no-op by default
    }
}
