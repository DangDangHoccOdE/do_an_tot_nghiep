package com.management_system.service.impl;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.management_system.dto.response.RevenueProjectResponse;
import com.management_system.entity.Project;
import com.management_system.entity.User;
import com.management_system.entity.enums.ProjectStatus;
import com.management_system.repository.ProjectRepository;
import com.management_system.repository.UserRepository;
import com.management_system.service.inter.IRevenueService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RevenueServiceImpl implements IRevenueService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Override
    public Map<String, Object> getStats() {
        List<Project> projects = projectRepository.findAllByDeleteFlagFalse();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal thisMonthRevenue = BigDecimal.ZERO;
        int completedProjects = 0;

        LocalDate today = LocalDate.now();

        for (Project p : projects) {
            if (p.getStatus() == ProjectStatus.DONE && p.getBudgetEstimated() != null) {
                totalRevenue = totalRevenue.add(p.getBudgetEstimated());
                completedProjects++;

                if (p.getEndDate() != null
                        && YearMonth.from(p.getEndDate()).equals(YearMonth.from(today))) {
                    thisMonthRevenue = thisMonthRevenue.add(p.getBudgetEstimated());
                }
            }
        }

        BigDecimal avgProjectValue = completedProjects > 0
                ? totalRevenue.divide(BigDecimal.valueOf(completedProjects), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return Map.of(
                "totalRevenue", totalRevenue,
                "thisMonthRevenue", thisMonthRevenue,
                "completedProjects", completedProjects,
                "avgProjectValue", avgProjectValue);
    }

    @Override
    public Map<Integer, BigDecimal> getMonthlyRevenue(int year) {
        Map<Integer, BigDecimal> result = new HashMap<>();
        for (int i = 1; i <= 12; i++) {
            result.put(i, BigDecimal.ZERO);
        }

        projectRepository.findAllByDeleteFlagFalse().forEach(p -> {
            if (p.getStatus() == ProjectStatus.DONE
                    && p.getEndDate() != null
                    && p.getEndDate().getYear() == year
                    && p.getBudgetEstimated() != null) {

                int month = p.getEndDate().getMonthValue();
                result.put(month, result.get(month).add(p.getBudgetEstimated()));
            }
        });

        return result;
    }

    @Override
    public List<RevenueProjectResponse> getTopProjectsByRevenue(Integer limit) {
        int topLimit = limit != null && limit > 0 ? limit : 5;

        return projectRepository.findAllByDeleteFlagFalse().stream()
                .filter(p -> p.getStatus() == ProjectStatus.DONE && p.getBudgetEstimated() != null)
                .sorted(Comparator.comparing(Project::getBudgetEstimated).reversed())
                .limit(topLimit)
                .map(this::toRevenueResponse)
                .toList();
    }

    @Override
    public List<RevenueProjectResponse> getTopProjectsByCompletionDate(Integer limit) {
        int topLimit = limit != null && limit > 0 ? limit : 5;

        return projectRepository.findAllByDeleteFlagFalse().stream()
                .filter(p -> p.getStatus() == ProjectStatus.DONE && p.getEndDate() != null)
                .sorted(Comparator.comparing(Project::getEndDate))
                .limit(topLimit)
                .map(this::toRevenueResponse)
                .toList();
    }

    @Override
    public byte[] exportRevenue(int year) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Revenue Report");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Revenue Report - " + year);

            List<RevenueProjectResponse> projects = getTopProjectsByRevenue(100);

            int rowIdx = 2;
            for (RevenueProjectResponse p : projects) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(p.getProjectName());
                row.createCell(1).setCellValue(p.getClientName());
                row.createCell(2).setCellValue(p.getBudgetEstimated().doubleValue());
                row.createCell(3).setCellValue(p.getCurrencyUnit());
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Export revenue failed", e);
        }
    }

    private RevenueProjectResponse toRevenueResponse(Project project) {
        User client = project.getClientId() != null
                ? userRepository.findById(project.getClientId()).orElse(null)
                : null;

        String clientName = client != null
                ? (client.getFirstName() + " " + client.getLastName()).trim()
                : null;

        return RevenueProjectResponse.builder()
                .id(project.getId())
                .projectName(project.getProjectName())
                .clientName(clientName)
                .budgetEstimated(
                        project.getBudgetEstimated() != null
                                ? project.getBudgetEstimated()
                                : BigDecimal.ZERO)
                .currencyUnit(project.getCurrencyUnit())
                .status(project.getStatus().name())
                .endDate(project.getEndDate())
                .build();
    }
}
