package dev.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTaskRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    @NotNull
    @Min(1)
    private Long cost;
}
