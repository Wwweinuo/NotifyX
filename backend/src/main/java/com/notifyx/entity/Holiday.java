package com.notifyx.entity;

import com.notifyx.enums.HolidayType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "holidays")
public class Holiday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private HolidayType type;

    @Column(length = 10)
    private String fixedDate;

    private Integer lunarMonth;

    private Integer lunarDay;

    @Column(length = 100)
    private String dynamicRule;

    private Integer dynamicMonth;

    private Integer dynamicWeekOrdinal;

    private Integer dynamicDayOfWeek;

    @Column(columnDefinition = "boolean default true")
    private Boolean enabled = true;

    @Column(length = 100)
    private String notifyEmail;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
