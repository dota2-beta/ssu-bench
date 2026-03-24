package dev.backend.dto;

import dev.backend.enums.Role;
import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private Long points;
    private Role role;
}
