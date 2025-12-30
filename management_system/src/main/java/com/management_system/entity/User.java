package com.management_system.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.management_system.entity.enums.AuthProvider;
import com.management_system.entity.enums.Gender;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends BaseEntity {
    @Column(length = 150, unique = true)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "role_id")
    private UUID roleId;

    @Column(name = "gender")
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "activation_code")
    private String activationCode;

    @Column(name = "activation_expiry")
    private LocalDateTime activationExpiry;

    @Column(name = "forgot_password_code")
    private String forgotPasswordCode;

    @Column(name = "forgot_password_expiry")
    private LocalDateTime forgotPasswordExpiry;

    @Column(name = "email_code")
    private String emailCode;

    @Column(name = "email_expiry")
    private LocalDateTime emailExpiry;

    @Column(name = "provider")
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "refreshToken")
    private String refreshToken;
}
