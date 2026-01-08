package com.management_system.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueProjectResponse {
    private UUID id;
    private String projectName;
    private String clientName;
    private BigDecimal budgetEstimated;
    private String currencyUnit;
    private String status;
    private LocalDate endDate;
}
