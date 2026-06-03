package com.judgemesh.dispatcher.web;

import com.judgemesh.api.message.JudgeTask;
import com.judgemesh.dispatcher.service.DispatcherService;
import com.judgemesh.dispatcher.service.DispatcherService.DispatchResult;
import com.judgemesh.dispatcher.service.LeaderElectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
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

    @PostMapping({
            "/admin/dispatcher/chaos/kill-self",
            "/api/admin/dispatcher/chaos/kill-self",
            "/admin/dispatcher/leader/relinquish",
            "/api/admin/dispatcher/leader/relinquish"
    })
    public Map<String, Object> killSelf() {
        leaderElectionService.relinquishForChaos();
        return leaderElectionService.status();
    }

    @PostMapping({
            "/admin/dispatcher/chaos/become-leader",
            "/api/admin/dispatcher/chaos/become-leader",
            "/admin/dispatcher/leader/reacquire",
            "/api/admin/dispatcher/leader/reacquire"
    })
    public Map<String, Object> becomeLeader() {
        leaderElectionService.becomeLeader();
        return leaderElectionService.status();
    }

    @PostMapping({"/internal/dispatcher/dispatch", "/api/internal/dispatcher/dispatch"})
    public ResponseEntity<Map<String, Object>> dispatchDirect(@RequestBody JudgeTask task) {
        DispatchResult result = dispatcherService.dispatchEmergency(task);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("accepted", result.ok());
        body.put("message", result.message());
        if (result.worker() != null) {
            body.put("worker", result.worker());
        }
        return result.ok()
                ? ResponseEntity.status(HttpStatus.ACCEPTED).body(body)
                : ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }
}
