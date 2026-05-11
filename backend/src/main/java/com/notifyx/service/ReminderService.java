package com.notifyx.service;

import com.notifyx.entity.Contact;
import com.notifyx.entity.EmailConfig;
import com.notifyx.entity.Holiday;
import com.notifyx.entity.ReminderRecord;
import com.notifyx.enums.BirthdayType;
import com.notifyx.enums.ReminderStatus;
import com.notifyx.enums.ReminderType;
import com.notifyx.repository.ContactRepository;
import com.notifyx.repository.EmailConfigRepository;
import com.notifyx.repository.HolidayRepository;
import com.notifyx.repository.ReminderRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderService {

    private final ContactRepository contactRepository;
    private final HolidayRepository holidayRepository;
    private final ReminderRecordRepository reminderRecordRepository;
    private final EmailConfigRepository emailConfigRepository;
    private final EmailService emailService;
    private final HolidayService holidayService;
    private final LunarCalendarService lunarCalendarService;

    private static final int MAX_RETRY_COUNT = 3;

    /**
     * 检查明天的提醒事件并发送邮件
     * 这是定时任务调用的主方法
     */
    public void checkAndSendReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        log.info("开始检查明天({})的提醒事件", tomorrow);

        List<ReminderRecord> birthdayReminders = checkBirthdayReminders(tomorrow);
        List<ReminderRecord> holidayReminders = checkHolidayReminders(tomorrow);

        List<ReminderRecord> allReminders = new ArrayList<>();
        allReminders.addAll(birthdayReminders);
        allReminders.addAll(holidayReminders);

        for (ReminderRecord record : allReminders) {
            sendAndRecord(record);
        }

        log.info("提醒检查完毕，共处理 {} 条提醒", allReminders.size());
    }

    /**
     * 检查明天的生日提醒
     */
    private List<ReminderRecord> checkBirthdayReminders(LocalDate tomorrow) {
        List<ReminderRecord> reminders = new ArrayList<>();
        List<Contact> contacts = contactRepository.findByEnabledTrue();

        for (Contact contact : contacts) {
            boolean isTomorrow = false;

            if (contact.getBirthdayType() == BirthdayType.SOLAR) {
                // 公历生日：比较 MM-DD
                String tomorrowMmDd = tomorrow.format(DateTimeFormatter.ofPattern("MM-dd"));
                isTomorrow = tomorrowMmDd.equals(contact.getBirthdayDate());
            } else if (contact.getBirthdayType() == BirthdayType.LUNAR) {
                // 农历生日：转换为今年公历日期再比较
                LocalDate lunarBirthdayInSolar = lunarCalendarService.getLunarBirthdayInYear(
                        tomorrow.getYear(),
                        contact.getLunarMonth(),
                        contact.getLunarDay(),
                        Boolean.TRUE.equals(contact.getIsLeapMonth())
                );
                isTomorrow = tomorrow.equals(lunarBirthdayInSolar);
            }

            if (isTomorrow) {
                ReminderRecord record = createBirthdayReminder(contact, tomorrow);
                reminders.add(record);
            }
        }

        return reminders;
    }

    /**
     * 检查明天的节日提醒
     */
    private List<ReminderRecord> checkHolidayReminders(LocalDate tomorrow) {
        List<ReminderRecord> reminders = new ArrayList<>();
        List<Holiday> holidays = holidayRepository.findByEnabledTrue();

        for (Holiday holiday : holidays) {
            LocalDate holidayDate = holidayService.calculateHolidayDate(holiday, tomorrow.getYear());
            if (tomorrow.equals(holidayDate)) {
                ReminderRecord record = createHolidayReminder(holiday, tomorrow);
                reminders.add(record);
            }
        }

        return reminders;
    }

    /**
     * 生成生日提醒邮件内容
     */
    private ReminderRecord createBirthdayReminder(Contact contact, LocalDate targetDate) {
        ReminderRecord record = new ReminderRecord();
        record.setReminderType(ReminderType.BIRTHDAY);
        record.setTargetName(contact.getName());
        record.setTargetDate(targetDate);
        record.setStatus(ReminderStatus.PENDING);
        record.setRetryCount(0);

        // 确定收件人
        String toEmail = determineEmail(contact.getNotifyEmail());
        record.setEmailTo(toEmail);

        // 生成邮件主题
        record.setEmailSubject("明天是" + contact.getName() + "的生日提醒");

        // 生成邮件内容
        StringBuilder content = new StringBuilder();
        content.append("提醒您：\n");
        content.append("明天（").append(targetDate).append("）是")
                .append(contact.getName()).append("的生日");

        if (contact.getBirthdayType() == BirthdayType.LUNAR) {
            String lunarDesc = lunarCalendarService.getLunarDateDescription(
                    contact.getLunarMonth(),
                    contact.getLunarDay(),
                    Boolean.TRUE.equals(contact.getIsLeapMonth())
            );
            content.append("（农历").append(lunarDesc).append("）");
        }
        content.append("。\n");
        content.append("记得送上祝福！");

        record.setEmailContent(content.toString());
        return record;
    }

    /**
     * 生成节日提醒邮件内容
     */
    private ReminderRecord createHolidayReminder(Holiday holiday, LocalDate targetDate) {
        ReminderRecord record = new ReminderRecord();
        record.setReminderType(ReminderType.HOLIDAY);
        record.setTargetName(holiday.getName());
        record.setTargetDate(targetDate);
        record.setStatus(ReminderStatus.PENDING);
        record.setRetryCount(0);

        // 确定收件人
        String toEmail = determineEmail(holiday.getNotifyEmail());
        record.setEmailTo(toEmail);

        // 生成邮件主题
        record.setEmailSubject("明天是" + holiday.getName() + "提醒");

        // 生成邮件内容
        StringBuilder content = new StringBuilder();
        content.append("提醒您：\n");
        content.append("明天（").append(targetDate).append("）是")
                .append(holiday.getName()).append("。\n");
        content.append("记得送上祝福！");

        record.setEmailContent(content.toString());
        return record;
    }

    /**
     * 发送提醒并记录结果
     */
    private void sendAndRecord(ReminderRecord record) {
        // 防重复：检查是否已成功发送过
        boolean alreadySent = reminderRecordRepository.existsByTargetNameAndTargetDateAndReminderTypeAndStatus(
                record.getTargetName(),
                record.getTargetDate(),
                record.getReminderType(),
                ReminderStatus.SUCCESS
        );

        if (alreadySent) {
            log.info("提醒已发送过，跳过: {} - {}", record.getTargetName(), record.getTargetDate());
            return;
        }

        // 尝试发送，最多重试3次
        for (int attempt = 0; attempt < MAX_RETRY_COUNT; attempt++) {
            try {
                emailService.sendEmail(record.getEmailTo(), record.getEmailSubject(), record.getEmailContent());
                record.setStatus(ReminderStatus.SUCCESS);
                record.setSentAt(LocalDateTime.now());
                record.setRetryCount(attempt);
                reminderRecordRepository.save(record);
                log.info("提醒发送成功: {} - {}", record.getTargetName(), record.getTargetDate());
                return;
            } catch (Exception e) {
                log.warn("提醒发送失败（第{}次）: {} - {}, 原因: {}",
                        attempt + 1, record.getTargetName(), record.getTargetDate(), e.getMessage());
                record.setRetryCount(attempt + 1);
                record.setFailureReason(e.getMessage());
            }
        }

        // 所有重试都失败
        record.setStatus(ReminderStatus.FAILED);
        reminderRecordRepository.save(record);
        log.error("提醒发送最终失败: {} - {}", record.getTargetName(), record.getTargetDate());
    }

    /**
     * 查询提醒记录（分页）
     */
    public Page<ReminderRecord> getReminders(String status, Pageable pageable) {
        if (status != null && !status.trim().isEmpty()) {
            ReminderStatus reminderStatus = ReminderStatus.valueOf(status);
            return reminderRecordRepository.findByStatus(reminderStatus, pageable);
        }
        return reminderRecordRepository.findAll(pageable);
    }

    /**
     * 获取近期（未来7天）提醒预览
     */
    public List<Map<String, Object>> getUpcomingReminders() {
        List<Map<String, Object>> upcoming = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 1; i <= 7; i++) {
            LocalDate checkDate = today.plusDays(i);

            // 检查生日
            List<Contact> contacts = contactRepository.findByEnabledTrue();
            for (Contact contact : contacts) {
                boolean isMatch = false;

                if (contact.getBirthdayType() == BirthdayType.SOLAR) {
                    String checkMmDd = checkDate.format(DateTimeFormatter.ofPattern("MM-dd"));
                    isMatch = checkMmDd.equals(contact.getBirthdayDate());
                } else if (contact.getBirthdayType() == BirthdayType.LUNAR) {
                    LocalDate lunarBirthdayInSolar = lunarCalendarService.getLunarBirthdayInYear(
                            checkDate.getYear(),
                            contact.getLunarMonth(),
                            contact.getLunarDay(),
                            Boolean.TRUE.equals(contact.getIsLeapMonth())
                    );
                    isMatch = checkDate.equals(lunarBirthdayInSolar);
                }

                if (isMatch) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("date", checkDate.toString());
                    item.put("type", "BIRTHDAY");
                    item.put("name", contact.getName());
                    item.put("description", contact.getName() + "的生日");
                    upcoming.add(item);
                }
            }

            // 检查节日
            List<Holiday> holidays = holidayRepository.findByEnabledTrue();
            for (Holiday holiday : holidays) {
                LocalDate holidayDate = holidayService.calculateHolidayDate(holiday, checkDate.getYear());
                if (checkDate.equals(holidayDate)) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("date", checkDate.toString());
                    item.put("type", "HOLIDAY");
                    item.put("name", holiday.getName());
                    item.put("description", holiday.getName());
                    upcoming.add(item);
                }
            }
        }

        return upcoming;
    }

    /**
     * 获取统计数据
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // 联系人总数
        stats.put("contactCount", contactRepository.count());

        // 节日总数
        stats.put("holidayCount", holidayRepository.count());

        // 本月已发送提醒数
        LocalDate now = LocalDate.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = now.plusMonths(1).withDayOfMonth(1).atStartOfDay();
        long sentCount = reminderRecordRepository.countByStatusAndSentAtBetween(
                ReminderStatus.SUCCESS, monthStart, monthEnd);
        stats.put("monthSentCount", sentCount);

        return stats;
    }

    /**
     * 确定邮件接收人
     * 优先使用联系人/节日自己配置的 notifyEmail
     * 如果为空，使用 EmailConfig 中的 defaultToEmail
     */
    private String determineEmail(String specificEmail) {
        if (specificEmail != null && !specificEmail.trim().isEmpty()) {
            return specificEmail;
        }
        EmailConfig emailConfig = emailConfigRepository.findAll().stream()
                .findFirst()
                .orElse(null);
        if (emailConfig != null && emailConfig.getDefaultToEmail() != null) {
            return emailConfig.getDefaultToEmail();
        }
        throw new RuntimeException("未配置收件人邮箱");
    }
}
