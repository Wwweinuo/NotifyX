package com.notifyx.service;

import com.notifyx.dto.SchedulerConfigDTO;
import com.notifyx.entity.SchedulerConfig;
import com.notifyx.repository.SchedulerConfigRepository;
import com.notifyx.scheduler.DynamicScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class SchedulerConfigService {

    private final SchedulerConfigRepository schedulerConfigRepository;
    private final DynamicScheduler dynamicScheduler;

    public SchedulerConfigService(SchedulerConfigRepository schedulerConfigRepository,
                                  @Lazy DynamicScheduler dynamicScheduler) {
        this.schedulerConfigRepository = schedulerConfigRepository;
        this.dynamicScheduler = dynamicScheduler;
    }

    /**
     * 获取所有定时任务配置
     */
    public List<SchedulerConfig> getAllConfigs() {
        return schedulerConfigRepository.findAll();
    }

    /**
     * 根据任务名称获取配置
     */
    public SchedulerConfig getByTaskName(String taskName) {
        return schedulerConfigRepository.findByTaskName(taskName).orElse(null);
    }

    /**
     * 更新定时任务配置（Cron 表达式、启用/禁用）
     * 更新后调度器会在下次触发时自动读取新配置
     */
    public SchedulerConfig updateConfig(Long id, SchedulerConfigDTO dto) {
        SchedulerConfig config = schedulerConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("定时任务配置不存在: " + id));

        if (dto.getCronExpression() != null) {
            // 验证 Cron 表达式是否合法
            if (!CronExpression.isValidExpression(dto.getCronExpression())) {
                throw new RuntimeException("无效的 Cron 表达式: " + dto.getCronExpression());
            }
            config.setCronExpression(dto.getCronExpression());
        }

        if (dto.getEnabled() != null) {
            config.setEnabled(dto.getEnabled());
        }

        if (dto.getDescription() != null) {
            config.setDescription(dto.getDescription());
        }

        // 重新计算下次执行时间
        LocalDateTime nextTime = calculateNextExecutionTime(config.getCronExpression());
        config.setNextExecutionTime(nextTime);

        SchedulerConfig saved = schedulerConfigRepository.save(config);
        log.info("定时任务配置已更新: taskName={}, cron={}, enabled={}",
                saved.getTaskName(), saved.getCronExpression(), saved.getEnabled());

        // 重新调度任务，使新配置立即生效
        dynamicScheduler.reschedule();

        return saved;
    }

    /**
     * 更新上次执行时间和下次执行时间
     */
    public void updateExecutionTime(String taskName, LocalDateTime lastTime, LocalDateTime nextTime) {
        SchedulerConfig config = schedulerConfigRepository.findByTaskName(taskName).orElse(null);
        if (config != null) {
            config.setLastExecutionTime(lastTime);
            config.setNextExecutionTime(nextTime);
            schedulerConfigRepository.save(config);
        }
    }

    /**
     * 计算下次执行时间（根据 Cron 表达式）
     */
    public LocalDateTime calculateNextExecutionTime(String cronExpression) {
        try {
            CronExpression expression = CronExpression.parse(cronExpression);
            return expression.next(LocalDateTime.now());
        } catch (Exception e) {
            log.error("计算下次执行时间失败, cron={}, error={}", cronExpression, e.getMessage());
            return null;
        }
    }
}
