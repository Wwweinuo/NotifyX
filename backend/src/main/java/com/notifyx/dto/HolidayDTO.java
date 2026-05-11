package com.notifyx.dto;

import lombok.Data;

@Data
public class HolidayDTO {
    private Long id;
    private String name;
    private String type; // "FIXED_SOLAR", "FIXED_LUNAR", "DYNAMIC_RULE"
    private String fixedDate;
    private Integer lunarMonth;
    private Integer lunarDay;
    private String dynamicRule;
    private Integer dynamicMonth;
    private Integer dynamicWeekOrdinal;
    private Integer dynamicDayOfWeek;
    private Boolean enabled;
    private String notifyEmail;
}
