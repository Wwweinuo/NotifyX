package com.notifyx.entity;

import com.notifyx.enums.BirthdayType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "contacts")
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String name;

    @Column(length = 30)
    private String relationship;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private BirthdayType birthdayType;

    @Column(length = 10)
    private String birthdayDate;

    private Integer lunarMonth;

    private Integer lunarDay;

    @Column(columnDefinition = "boolean default false")
    private Boolean isLeapMonth = false;

    @Column(columnDefinition = "boolean default true")
    private Boolean enabled = true;

    @Column(length = 100)
    private String notifyEmail;

    @Column(length = 200)
    private String remark;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
