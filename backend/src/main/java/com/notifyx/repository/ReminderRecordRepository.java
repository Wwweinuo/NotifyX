package com.notifyx.repository;

import com.notifyx.entity.ReminderRecord;
import com.notifyx.enums.ReminderStatus;
import com.notifyx.enums.ReminderType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public interface ReminderRecordRepository extends JpaRepository<ReminderRecord, Long> {

    Page<ReminderRecord> findByStatus(ReminderStatus status, Pageable pageable);

    boolean existsByTargetNameAndTargetDateAndReminderType(String targetName, LocalDate targetDate, ReminderType reminderType);

    boolean existsByTargetNameAndTargetDateAndReminderTypeAndStatus(String targetName, LocalDate targetDate, ReminderType reminderType, ReminderStatus status);

    long countByStatusAndSentAtBetween(ReminderStatus status, LocalDateTime start, LocalDateTime end);
}
