package com.notifyx.config;

import com.notifyx.entity.Holiday;
import com.notifyx.entity.SchedulerConfig;
import com.notifyx.enums.HolidayType;
import com.notifyx.repository.HolidayRepository;
import com.notifyx.repository.SchedulerConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据初始化：仅在数据库无数据时插入默认节日和定时任务配置
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final HolidayRepository holidayRepository;
    private final SchedulerConfigRepository schedulerConfigRepository;

    @Override
    public void run(String... args) {
        initHolidays();
        initSchedulerConfig();
    }

    private void initHolidays() {
        if (holidayRepository.count() > 0) {
            log.info("节日数据已存在，跳过初始化");
            return;
        }

        log.info("初始化默认节日数据...");

        // 固定公历节日
        List.of(
                holiday("元旦", HolidayType.FIXED_SOLAR, "01-01", null, null, null, null, null),
                holiday("情人节", HolidayType.FIXED_SOLAR, "02-14", null, null, null, null, null),
                holiday("妇女节", HolidayType.FIXED_SOLAR, "03-08", null, null, null, null, null),
                holiday("劳动节", HolidayType.FIXED_SOLAR, "05-01", null, null, null, null, null),
                holiday("儿童节", HolidayType.FIXED_SOLAR, "06-01", null, null, null, null, null),
                holiday("国庆节", HolidayType.FIXED_SOLAR, "10-01", null, null, null, null, null),
                holiday("圣诞节", HolidayType.FIXED_SOLAR, "12-25", null, null, null, null, null),
                // 农历节日
                holiday("春节", HolidayType.FIXED_LUNAR, null, 1, 1, null, null, null),
                holiday("元宵节", HolidayType.FIXED_LUNAR, null, 1, 15, null, null, null),
                holiday("端午节", HolidayType.FIXED_LUNAR, null, 5, 5, null, null, null),
                holiday("七夕节", HolidayType.FIXED_LUNAR, null, 7, 7, null, null, null),
                holiday("中秋节", HolidayType.FIXED_LUNAR, null, 8, 15, null, null, null),
                holiday("重阳节", HolidayType.FIXED_LUNAR, null, 9, 9, null, null, null),
                // 动态规则节日
                holiday("母亲节", HolidayType.DYNAMIC_RULE, null, null, null, "5月第2个星期日", 5, 2, 7),
                holiday("父亲节", HolidayType.DYNAMIC_RULE, null, null, null, "6月第3个星期日", 6, 3, 7),
                holiday("感恩节", HolidayType.DYNAMIC_RULE, null, null, null, "11月第4个星期四", 11, 4, 4)
        ).forEach(holidayRepository::save);

        log.info("默认节日数据初始化完成，共 {} 条", holidayRepository.count());
    }

    private void initSchedulerConfig() {
        if (schedulerConfigRepository.findByTaskName("DAILY_REMINDER").isPresent()) {
            log.info("定时任务配置已存在，跳过初始化");
            return;
        }

        log.info("初始化默认定时任务配置...");
        SchedulerConfig config = new SchedulerConfig();
        config.setTaskName("DAILY_REMINDER");
        config.setCronExpression("0 0 8 * * ?");
        config.setDescription("每日提醒检查任务");
        config.setEnabled(true);
        schedulerConfigRepository.save(config);
        log.info("默认定时任务配置初始化完成");
    }

    private Holiday holiday(String name, HolidayType type, String fixedDate,
                            Integer lunarMonth, Integer lunarDay,
                            String dynamicRule, Integer dynamicMonth,
                            Integer dynamicWeekOrdinal, Integer dynamicDayOfWeek) {
        Holiday h = new Holiday();
        h.setName(name);
        h.setType(type);
        h.setFixedDate(fixedDate);
        h.setLunarMonth(lunarMonth);
        h.setLunarDay(lunarDay);
        h.setDynamicRule(dynamicRule);
        h.setDynamicMonth(dynamicMonth);
        h.setDynamicWeekOrdinal(dynamicWeekOrdinal);
        h.setDynamicDayOfWeek(dynamicDayOfWeek);
        h.setEnabled(true);
        return h;
    }

    // 固定公历/农历节日简化构造
    private Holiday holiday(String name, HolidayType type, String fixedDate,
                            Integer lunarMonth, Integer lunarDay,
                            String dynamicRule, Integer dynamicMonth,
                            Integer dynamicWeekOrdinal) {
        return holiday(name, type, fixedDate, lunarMonth, lunarDay, dynamicRule, dynamicMonth, dynamicWeekOrdinal, null);
    }
}
