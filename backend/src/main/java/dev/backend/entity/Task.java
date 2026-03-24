package dev.backend.entity;

import dev.backend.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tasks")
@Data
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private User customer;

    @ManyToOne
    @JoinColumn(name = "executor_id")
    private User executor;

    private String title;
    private String description;
    private Long cost;
    
    @Enumerated(EnumType.STRING)
    private TaskStatus status;
}
