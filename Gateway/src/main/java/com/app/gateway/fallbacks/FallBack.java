package com.app.gateway.fallbacks;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class FallBack {

    @RequestMapping("/fallback/{service}")
    public ResponseEntity<Map<String,Object>> breakerFallback(
            @PathVariable String service,
            ServerHttpRequest request)
    {
        Map<String,Object> map = new HashMap<>();
        map.put("status",HttpStatus.SERVICE_UNAVAILABLE);
        map.put("message","Service temporarily unavailable");
        map.put("service",service);
        map.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(map);
    }
}
