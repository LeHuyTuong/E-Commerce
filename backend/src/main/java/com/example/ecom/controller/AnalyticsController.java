package com.example.ecom.controller;

import com.example.ecom.payload.AnalyticsResponse;
import com.example.ecom.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnalyticsResponse> getAnalytics() {
        AnalyticsResponse response = analyticsService.getAnalyticsData();
        return ResponseEntity.ok(response);
    }
}
