package com.management_system.dto.request;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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

    private MultipartFile avatar;

    private boolean keepOldImage;

}
