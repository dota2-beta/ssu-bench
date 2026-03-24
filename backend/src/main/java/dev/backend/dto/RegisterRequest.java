package dev.backend.dto;

import dev.backend.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String username;
    @Size(min = 6, max = 100)
    private String password;
    @NotNull
    private Role role;
}