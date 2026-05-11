package com.notifyx.service;

import com.notifyx.dto.HolidayDTO;
import com.notifyx.entity.Holiday;
import com.notifyx.enums.HolidayType;
import com.notifyx.repository.HolidayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HolidayService {

    private final HolidayRepository holidayRepository;
    private final LunarCalendarService lunarCalendarService;

    /**
     * 查询所有节日
     */
    public List<Holiday> getAllHolidays() {
        return holidayRepository.findAll();
    }

    /**
     * 创建节日
     */
    public Holiday createHoliday(HolidayDTO dto) {
        Holiday holiday = new Holiday();
        mapDtoToEntity(dto, holiday);
        return holidayRepository.save(holiday);
    }

    /**
     * 更新节日
     */
    public Holiday updateHoliday(Long id, HolidayDTO dto) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("节日不存在，ID: " + id));
        mapDtoToEntity(dto, holiday);
        return holidayRepository.save(holiday);
    }

    /**
     * 删除节日
     */
    public void deleteHoliday(Long id) {
        holidayRepository.deleteById(id);
    }

    /**
     * 查询所有启用的节日
     */
    public List<Holiday> getEnabledHolidays() {
        return holidayRepository.findByEnabledTrue();
    }

    /**
     * 计算指定年份某个节日的公历日期
     * - FIXED_SOLAR: 直接用 fixedDate
     * - FIXED_LUNAR: 用 LunarCalendarService 转换
     * - DYNAMIC_RULE: 用动态规则计算（如5月第2个星期日）
     */
    public LocalDate calculateHolidayDate(Holiday holiday, int year) {
        switch (holiday.getType()) {
            case FIXED_SOLAR:
                MonthDay monthDay = MonthDay.parse("--" + holiday.getFixedDate());
                return monthDay.atYear(year);
            case FIXED_LUNAR:
                return lunarCalendarService.getLunarBirthdayInYear(
                        year,
                        holiday.getLunarMonth(),
                        holiday.getLunarDay(),
                        false
                );
            case DYNAMIC_RULE:
                return calculateDynamicDate(
                        holiday.getDynamicMonth(),
                        holiday.getDynamicWeekOrdinal(),
                        holiday.getDynamicDayOfWeek(),
                        year
                );
            default:
                return null;
        }
    }

    /**
     * 计算动态规则节日日期
     * @param month 月份
     * @param weekOrdinal 第几个星期（1-5）
     * @param dayOfWeek 星期几（1=周一, 7=周日）
     * @param year 年份
     */
    private LocalDate calculateDynamicDate(int month, int weekOrdinal, int dayOfWeek, int year) {
        DayOfWeek targetDayOfWeek = DayOfWeek.of(dayOfWeek);
        // 获取该月第一天
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
        // 找到该月第一个目标星期几
        LocalDate firstTarget = firstDayOfMonth.with(TemporalAdjusters.firstInMonth(targetDayOfWeek));
        // 加上 (weekOrdinal - 1) * 7 天
        return firstTarget.plusWeeks(weekOrdinal - 1);
    }

    private void mapDtoToEntity(HolidayDTO dto, Holiday holiday) {
        holiday.setName(dto.getName());
        holiday.setType(HolidayType.valueOf(dto.getType()));
        holiday.setFixedDate(dto.getFixedDate());
        holiday.setLunarMonth(dto.getLunarMonth());
        holiday.setLunarDay(dto.getLunarDay());
        holiday.setDynamicRule(dto.getDynamicRule());
        holiday.setDynamicMonth(dto.getDynamicMonth());
        holiday.setDynamicWeekOrdinal(dto.getDynamicWeekOrdinal());
        holiday.setDynamicDayOfWeek(dto.getDynamicDayOfWeek());
        holiday.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);
        holiday.setNotifyEmail(dto.getNotifyEmail());
    }
}
