package dev.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "bids")
@Data
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "executor_id")
    private User executor;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;
}
