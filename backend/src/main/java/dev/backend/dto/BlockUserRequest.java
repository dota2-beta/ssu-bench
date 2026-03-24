package dev.backend.dto;

import lombok.Data;

@Data
public class BlockUserRequest {
    private String username;
    private boolean blocked;
}
