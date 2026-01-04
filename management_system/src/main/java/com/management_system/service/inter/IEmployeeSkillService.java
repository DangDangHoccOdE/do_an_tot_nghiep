package com.management_system.service.inter;

import java.util.List;
import java.util.UUID;

import com.management_system.dto.request.EmployeeSkillRequest;
import com.management_system.dto.response.EmployeeSkillResponse;

public interface IEmployeeSkillService {
    EmployeeSkillResponse addSkillToEmployee(UUID employeeId, EmployeeSkillRequest request);

    void removeSkillFromEmployee(UUID employeeId, UUID skillId);

    List<EmployeeSkillResponse> getEmployeeSkills(UUID employeeId);

    void deleteEmployeeSkills(UUID employeeId);
}
