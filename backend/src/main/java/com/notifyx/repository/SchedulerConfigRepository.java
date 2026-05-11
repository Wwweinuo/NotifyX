package com.notifyx.repository;

import com.notifyx.entity.SchedulerConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchedulerConfigRepository extends JpaRepository<SchedulerConfig, Long> {

    Optional<SchedulerConfig> findByTaskName(String taskName);
}
