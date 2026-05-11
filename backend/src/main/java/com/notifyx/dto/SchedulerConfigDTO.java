package com.notifyx.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SchedulerConfigDTO {
    private Long id;
    private String taskName;
    private String cronExpression;
    private String description;
    private Boolean enabled;
    private LocalDateTime lastExecutionTime;
    private LocalDateTime nextExecutionTime;
}
