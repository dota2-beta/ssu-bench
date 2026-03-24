package dev.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "payments")
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "payer_id")
    private User payer;

    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @OneToOne(targetEntity = Task.class)
    @JoinColumn(name = "task_id")
    private Task task;

    private Long points;
}
