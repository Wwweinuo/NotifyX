package com.notifyx.scheduler;

import com.notifyx.entity.SchedulerConfig;
import com.notifyx.service.ReminderService;
import com.notifyx.service.SchedulerConfigService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.ScheduledFuture;

/**
 * 动态定时任务调度器
 * 支持运行时修改 Cron 表达式，修改后立即生效（取消旧任务，重新调度）
 */
@Component
@Slf4j
public class DynamicScheduler {

    private static final String TASK_NAME = "DAILY_REMINDER";

    @Autowired
    private SchedulerConfigService schedulerConfigService;

    @Autowired
    private ReminderService reminderService;

    private ThreadPoolTaskScheduler taskScheduler;
    private ScheduledFuture<?> scheduledFuture;

    @PostConstruct
    public void init() {
        // 初始化线程池调度器
        taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(2);
        taskScheduler.setThreadNamePrefix("notifyx-scheduler-");
        taskScheduler.setErrorHandler(t -> log.error("定时任务执行异常", t));
        taskScheduler.setDaemon(false);
        taskScheduler.initialize();

        // 启动时根据数据库配置调度任务
        scheduleTask();
    }

    @PreDestroy
    public void destroy() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
        if (taskScheduler != null) {
            taskScheduler.shutdown();
        }
    }

    /**
     * 重新调度任务（配置变更时调用）
     */
    public void reschedule() {
        log.info("收到重新调度请求，取消旧任务并重新调度");
        cancelCurrentTask();
        scheduleTask();
    }

    /**
     * 根据数据库配置调度任务
     */
    private void scheduleTask() {
        SchedulerConfig config = schedulerConfigService.getByTaskName(TASK_NAME);

        if (config == null) {
            log.warn("定时任务 {} 配置不存在，跳过调度", TASK_NAME);
            return;
        }

        if (!Boolean.TRUE.equals(config.getEnabled())) {
            log.info("定时任务 {} 未启用，跳过调度", TASK_NAME);
            return;
        }

        String cronExpression = config.getCronExpression();
        CronTrigger cronTrigger = new CronTrigger(cronExpression);

        scheduledFuture = taskScheduler.schedule(this::executeTask, cronTrigger);

        // 计算并更新下次执行时间
        LocalDateTime nextTime = schedulerConfigService.calculateNextExecutionTime(cronExpression);
        if (nextTime != null) {
            schedulerConfigService.updateExecutionTime(TASK_NAME, null, nextTime);
            log.info("定时任务 {} 已调度，cron={}，下次执行时间={}", TASK_NAME, cronExpression, nextTime);
        }
    }

    /**
     * 取消当前调度
     */
    private void cancelCurrentTask() {
        if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(false);
            scheduledFuture = null;
            log.info("已取消当前调度任务");
        }
    }

    /**
     * 执行定时任务
     */
    private void executeTask() {
        SchedulerConfig config = schedulerConfigService.getByTaskName(TASK_NAME);
        if (config == null || !Boolean.TRUE.equals(config.getEnabled())) {
            log.info("定时任务 {} 未启用，跳过执行", TASK_NAME);
            return;
        }

        log.info("开始执行定时任务: {}", TASK_NAME);
        try {
            reminderService.checkAndSendReminders();

            // 更新执行时间
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextTime = schedulerConfigService.calculateNextExecutionTime(config.getCronExpression());
            schedulerConfigService.updateExecutionTime(TASK_NAME, now, nextTime);

            log.info("定时任务 {} 执行完毕，下次执行时间: {}", TASK_NAME, nextTime);
        } catch (Exception e) {
            log.error("定时任务 {} 执行异常: {}", TASK_NAME, e.getMessage(), e);
        }
    }
}
