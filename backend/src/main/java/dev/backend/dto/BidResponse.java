package dev.backend.dto;

import lombok.Data;

@Data
public class BidResponse {
    private Long bidId;
    private Long taskId;
    private String executorUsername;
}
