package com.notifyx.service;

import com.nlf.calendar.Lunar;
import com.nlf.calendar.Solar;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 农历转换服务，基于 cn.6tail:lunar 库实现农历公历互转。
 */
@Service
public class LunarCalendarService {

    private static final String[] MONTH_NAMES = {
            "正月", "二月", "三月", "四月", "五月", "六月",
            "七月", "八月", "九月", "十月", "十一月", "腊月"
    };

    private static final String[] DAY_NAMES = {
            "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
            "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
            "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    };

    /**
     * 将农历日期转换为指定年份的公历日期
     *
     * @param year        公历年份
     * @param lunarMonth  农历月份 (1-12)
     * @param lunarDay    农历日 (1-30)
     * @param isLeapMonth 是否闰月
     * @return 对应的公历 LocalDate，如果该年无此农历日期则返回 null
     */
    public LocalDate lunarToSolar(int year, int lunarMonth, int lunarDay, boolean isLeapMonth) {
        int month = isLeapMonth ? -lunarMonth : lunarMonth;

        // 尝试农历年 = 公历年
        LocalDate result = tryConvert(year, month, lunarDay);
        if (result != null && result.getYear() == year) {
            return result;
        }

        // 尝试农历年 = 公历年 - 1（处理腊月等跨年情况）
        result = tryConvert(year - 1, month, lunarDay);
        if (result != null && result.getYear() == year) {
            return result;
        }

        return null;
    }

    /**
     * 获取指定年份某个农历日期对应的公历日期。
     * 如果该年没有该闰月，自动回退到非闰月；
     * 如果农历日不存在（如某月无三十），回退到二十九。
     *
     * @param year        公历年份
     * @param lunarMonth  农历月份
     * @param lunarDay    农历日
     * @param isLeapMonth 是否闰月
     * @return 对应的公历 LocalDate
     */
    public LocalDate getLunarBirthdayInYear(int year, int lunarMonth, int lunarDay, boolean isLeapMonth) {
        // 如果指定了闰月，先尝试闰月
        if (isLeapMonth) {
            LocalDate result = lunarToSolar(year, lunarMonth, lunarDay, true);
            if (result != null) {
                return result;
            }
        }

        // 尝试非闰月
        LocalDate result = lunarToSolar(year, lunarMonth, lunarDay, false);
        if (result != null) {
            return result;
        }

        // 如果三十不存在，回退到二十九
        if (lunarDay == 30) {
            result = lunarToSolar(year, lunarMonth, 29, false);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    /**
     * 获取农历日期的中文描述
     *
     * @param lunarMonth  农历月份
     * @param lunarDay    农历日
     * @param isLeapMonth 是否闰月
     * @return 中文描述，如"正月十五"、"闰四月初六"
     */
    public String getLunarDateDescription(int lunarMonth, int lunarDay, boolean isLeapMonth) {
        StringBuilder sb = new StringBuilder();
        if (isLeapMonth) {
            sb.append("闰");
        }
        if (lunarMonth >= 1 && lunarMonth <= 12) {
            sb.append(MONTH_NAMES[lunarMonth - 1]);
        }
        if (lunarDay >= 1 && lunarDay <= 30) {
            sb.append(DAY_NAMES[lunarDay - 1]);
        }
        return sb.toString();
    }

    /**
     * 尝试转换农历日期，捕获异常返回 null
     */
    private LocalDate tryConvert(int lunarYear, int month, int day) {
        try {
            Lunar lunar = new Lunar(lunarYear, month, day);
            Solar solar = lunar.getSolar();
            return LocalDate.of(solar.getYear(), solar.getMonth(), solar.getDay());
        } catch (Exception e) {
            return null;
        }
    }
}
