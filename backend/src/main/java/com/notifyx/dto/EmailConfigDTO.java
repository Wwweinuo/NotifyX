package com.notifyx.dto;

import lombok.Data;

@Data
public class EmailConfigDTO {
    private Long id;
    private String smtpHost;
    private Integer smtpPort;
    private String username;
    private String password;
    private String fromName;
    private String defaultToEmail;
    private Boolean enabled;
}
