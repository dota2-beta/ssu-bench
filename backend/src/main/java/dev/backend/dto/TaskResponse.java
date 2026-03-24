package dev.backend.dto;

import dev.backend.enums.TaskStatus;
import lombok.Data;

@Data
public class TaskResponse {
    private Long id;
    private String title;
    private Long cost;
    private TaskStatus status;
    private String customerUsername;
    private String executorUsername;
    private String description;
}
