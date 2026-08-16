package com.binuwara.AssetsFlow.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(max = 40) String employeeId,
        @NotBlank @Size(max = 80) String username,
        @NotBlank @Size(min = 8, max = 128) String password,
        @NotBlank String confirmPassword,
        @NotBlank @Size(max = 50) String role
) {
}
