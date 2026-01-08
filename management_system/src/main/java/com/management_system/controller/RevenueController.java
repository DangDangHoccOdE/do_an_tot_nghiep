package com.management_system.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.management_system.dto.response.RevenueProjectResponse;
import com.management_system.service.inter.IRevenueService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/projects/revenue")
@RequiredArgsConstructor
public class RevenueController {

    private final IRevenueService revenueService;

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Map<String, Object> getStats() {
        return revenueService.getStats();
    }

    @GetMapping("/monthly/{year}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Map<Integer, BigDecimal> getMonthlyRevenue(@PathVariable int year) {
        return revenueService.getMonthlyRevenue(year);
    }

    @GetMapping("/top/revenue")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<RevenueProjectResponse> getTopProjectsByRevenue(Integer limit) {
        return revenueService.getTopProjectsByRevenue(limit);
    }

    @GetMapping("/top/completed")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<RevenueProjectResponse> getTopProjectsByCompletionDate(Integer limit) {
        return revenueService.getTopProjectsByCompletionDate(limit);
    }

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<byte[]> exportRevenue(@RequestBody Map<String, Integer> request) {
        int year = request.getOrDefault("year", LocalDate.now().getYear());

        byte[] data = revenueService.exportRevenue(year);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=revenue_" + year + ".xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }
}
