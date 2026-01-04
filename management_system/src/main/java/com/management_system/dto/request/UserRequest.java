package com.management_system.dto.request;

import java.util.List;
import java.util.UUID;

import com.management_system.entity.enums.ITRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserRequest {
    private UUID userId;

    @Size(max = 50)
    @NotBlank
    private String firstName;

    @Size(max = 50)
    @NotBlank
    private String lastName;

    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}")
    private String dateOfBirth;

    @Pattern(regexp = "^0\\d{9}$")
    private String phone;

    @Pattern(regexp = "^(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,}$")
    private String password;

    private String gender;

    @Pattern(regexp = "^((?!\\.)[\\w-_.]*[^.])(@\\w+)(\\.\\w+(\\.\\w+)?[^.\\W])$", message = "Định dạng email không hợp lệ!")
    private String email;

    @NotBlank
    private String authProvider;

    private boolean keepOldImage;

    private UUID roleId;

    private String avatar;

    private String skill; // For staff: "Middle Java, Senior Python" (deprecated)

    private String cv; // For staff: CV file URL

    private List<EmployeeSkillRequest> skills; // For staff: List of employee skills

    private ITRole itRole; // IT Role: FRONTEND_DEVELOPER, BACKEND_DEVELOPER, QA, BA, PM, etc.

}
