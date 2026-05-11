package com.notifyx.dto;

import lombok.Data;

@Data
public class ContactDTO {
    private Long id;
    private String name;
    private String relationship;
    private String birthdayType; // "SOLAR" or "LUNAR"
    private String birthdayDate; // MM-DD 格式（公历时使用）
    private Integer lunarMonth;
    private Integer lunarDay;
    private Boolean isLeapMonth;
    private Boolean enabled;
    private String notifyEmail;
    private String remark;
}
