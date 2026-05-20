package com.judgemesh.dispatcher.web;

import com.judgemesh.dispatcher.service.DispatcherService;
import com.judgemesh.dispatcher.service.LeaderElectionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping
public class DispatcherAdminController {
    private final DispatcherService dispatcherService;
    private final LeaderElectionService leaderElectionService;

    public DispatcherAdminController(DispatcherService dispatcherService, LeaderElectionService leaderElectionService) {
        this.dispatcherService = dispatcherService;
        this.leaderElectionService = leaderElectionService;
    }

    @GetMapping({"/admin/dispatcher/status", "/api/admin/dispatcher/status"})
    public Map<String, Object> status() {
        return dispatcherService.status();
    }

    @PostMapping({"/admin/dispatcher/chaos/kill-self", "/api/admin/dispatcher/chaos/kill-self"})
    public Map<String, Object> killSelf() {
        leaderElectionService.relinquishForChaos();
        return leaderElectionService.status();
    }

    @PostMapping({"/admin/dispatcher/chaos/become-leader", "/api/admin/dispatcher/chaos/become-leader"})
    public Map<String, Object> becomeLeader() {
        leaderElectionService.becomeLeader();
        return leaderElectionService.status();
    }
}
