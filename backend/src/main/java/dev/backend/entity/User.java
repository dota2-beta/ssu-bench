package dev.backend.entity;

import dev.backend.enums.Role;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private Long points;
    @Enumerated(EnumType.STRING)
    private Role role;
    private boolean isBlocked;
}
