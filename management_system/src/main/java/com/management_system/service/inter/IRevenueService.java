package com.management_system.service.inter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.management_system.dto.response.RevenueProjectResponse;

public interface IRevenueService {
    Map<String, Object> getStats();

    Map<Integer, BigDecimal> getMonthlyRevenue(int year);

    List<RevenueProjectResponse> getTopProjectsByRevenue(Integer limit);

    List<RevenueProjectResponse> getTopProjectsByCompletionDate(Integer limit);

    byte[] exportRevenue(int year);
}
