package com.notifyx.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LunarCalendarService 单元测试
 */
class LunarCalendarServiceTest {

    private LunarCalendarService lunarCalendarService;

    @BeforeEach
    void setUp() {
        lunarCalendarService = new LunarCalendarService();
    }

    /**
     * 测试1：正常农历日期转换
     * 农历2026年正月十五 -> 公历 2026-03-03
     */
    @Test
    void testLunarToSolar_NormalDate() {
        LocalDate result = lunarCalendarService.lunarToSolar(2026, 1, 15, false);
        assertNotNull(result);
        assertEquals(LocalDate.of(2026, 3, 3), result);
    }

    /**
     * 测试2：农历八月十五（中秋节）2026年
     */
    @Test
    void testLunarToSolar_MidAutumn() {
        LocalDate result = lunarCalendarService.lunarToSolar(2026, 8, 15, false);
        assertNotNull(result);
        // 2026年中秋节应在9月或10月
        assertTrue(result.getYear() == 2026);
        assertTrue(result.getMonthValue() >= 9 && result.getMonthValue() <= 10,
                "中秋节应在9月或10月，实际: " + result);
    }

    /**
     * 测试3：闰月处理 - 2025年有闰六月
     */
    @Test
    void testLunarToSolar_LeapMonth() {
        // 2025年有闰六月，闰六月十五应该能正常转换
        LocalDate result = lunarCalendarService.lunarToSolar(2025, 6, 15, true);
        assertNotNull(result, "2025年有闰六月，应能正常转换");
        assertEquals(2025, result.getYear());
        // 闰六月十五应在公历7月或8月
        assertTrue(result.getMonthValue() >= 7 && result.getMonthValue() <= 8,
                "闰六月十五应在7-8月，实际: " + result);
    }

    /**
     * 测试4：闰月回退 - 2026年无闰六月时自动回退到非闰六月
     */
    @Test
    void testGetLunarBirthdayInYear_NoLeapMonthFallback() {
        // 2026年没有闰六月，getLunarBirthdayInYear 应回退到非闰六月十五
        LocalDate result = lunarCalendarService.getLunarBirthdayInYear(2026, 6, 15, true);
        assertNotNull(result, "应回退到非闰六月");
        assertEquals(2026, result.getYear());

        // 回退结果应等于直接查非闰六月十五
        LocalDate nonLeapResult = lunarCalendarService.lunarToSolar(2026, 6, 15, false);
        assertEquals(nonLeapResult, result);
    }

    /**
     * 测试5：获取中文描述
     */
    @Test
    void testGetLunarDateDescription() {
        assertEquals("正月十五", lunarCalendarService.getLunarDateDescription(1, 15, false));
        assertEquals("八月十五", lunarCalendarService.getLunarDateDescription(8, 15, false));
        assertEquals("闰四月初六", lunarCalendarService.getLunarDateDescription(4, 6, true));
        assertEquals("腊月三十", lunarCalendarService.getLunarDateDescription(12, 30, false));
        assertEquals("十一月初一", lunarCalendarService.getLunarDateDescription(11, 1, false));
    }

    /**
     * 测试6：边界情况 - 农历大月三十
     * 某些月份有三十天（大月），某些只有二十九天（小月）
     */
    @Test
    void testLunarToSolar_DayThirty() {
        // 尝试转换三十日，如果该月是大月则应成功
        // 使用 getLunarBirthdayInYear 测试：如果三十不存在，应回退到二十九
        LocalDate result = lunarCalendarService.getLunarBirthdayInYear(2026, 1, 30, false);
        assertNotNull(result, "应返回有效日期（三十或回退到二十九）");
        assertEquals(2026, result.getYear());
    }
}
