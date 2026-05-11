package com.notifyx.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "email_config")
public class EmailConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String smtpHost;

    private Integer smtpPort;

    @Column(length = 100)
    private String username;

    @JsonIgnore
    @Column(length = 200)
    private String password;

    @Column(length = 50)
    private String fromName;

    @Column(length = 100)
    private String defaultToEmail;

    @Column(columnDefinition = "boolean default true")
    private Boolean enabled = true;
}
