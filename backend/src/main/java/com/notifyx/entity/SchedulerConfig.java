package com.notifyx.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "scheduler_config")
public class SchedulerConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, unique = true)
    private String taskName;

    @Column(length = 50)
    private String cronExpression;

    @Column(length = 200)
    private String description;

    @Column(columnDefinition = "boolean default true")
    private Boolean enabled = true;

    private LocalDateTime lastExecutionTime;

    private LocalDateTime nextExecutionTime;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
