package com.judgemesh.dispatcher.controller;

import com.judgemesh.api.error.ApiResponse;
import com.judgemesh.dispatcher.model.DispatcherStatusDTO;
import com.judgemesh.dispatcher.service.DispatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dispatcher")
@RequiredArgsConstructor
public class DispatcherAdminController {

    private final DispatchService dispatchService;

    @GetMapping("/status")
    public ApiResponse<DispatcherStatusDTO> status() {
        return ApiResponse.ok(dispatchService.status());
    }

    @PostMapping("/chaos/kill-self")
    public ApiResponse<String> killSelf() {
        Thread killer = new Thread(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            System.exit(137);
        });
        killer.setDaemon(true);
        killer.start();
        return ApiResponse.ok("terminating");
    }
}
