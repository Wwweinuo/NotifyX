package com.notifyx.entity;

import com.notifyx.enums.ReminderStatus;
import com.notifyx.enums.ReminderType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "reminder_records")
public class ReminderRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ReminderType reminderType;

    @Column(length = 50)
    private String targetName;

    private LocalDate targetDate;

    @Column(length = 100)
    private String emailTo;

    @Column(length = 200)
    private String emailSubject;

    @Column(columnDefinition = "TEXT")
    private String emailContent;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ReminderStatus status;

    @Column(length = 500)
    private String failureReason;

    @Column(columnDefinition = "integer default 0")
    private Integer retryCount = 0;

    private LocalDateTime sentAt;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
