package com.judgemesh.dispatcher.service;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class WorkerRegistryTest {
    private final List<HttpServer> servers = new ArrayList<>();

    @AfterEach
    void stopServers() {
        servers.forEach(server -> server.stop(0));
        servers.clear();
    }

    @Test
    void acquireDistributesAcrossHealthyWorkersWhenInflightTies() throws IOException {
        String workerA = startWorker(200);
        String workerB = startWorker(200);
        WorkerRegistry registry = new WorkerRegistry(workerA + "," + workerB, 5, new RestTemplate());

        Map<URI, Integer> selected = new HashMap<>();
        for (int i = 0; i < 6; i++) {
            URI worker = registry.acquire();
            selected.merge(worker, 1, Integer::sum);
            registry.release(worker);
        }

        assertThat(selected).containsEntry(URI.create(workerA), 3);
        assertThat(selected).containsEntry(URI.create(workerB), 3);
    }

    @Test
    void acquireSkipsUnhealthyWorkers() throws IOException {
        String unhealthy = startWorker(500);
        String healthy = startWorker(200);
        WorkerRegistry registry = new WorkerRegistry(unhealthy + "," + healthy, 5, new RestTemplate());

        URI worker = registry.acquire();

        assertThat(worker).isEqualTo(URI.create(healthy));
    }

    private String startWorker(int healthStatus) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/health", exchange -> {
            byte[] body = "{\"status\":\"UP\"}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(healthStatus, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        servers.add(server);
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }
}
