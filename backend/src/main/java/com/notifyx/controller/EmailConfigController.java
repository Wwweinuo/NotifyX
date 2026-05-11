package com.notifyx.controller;

import com.notifyx.dto.ApiResponse;
import com.notifyx.dto.EmailConfigDTO;
import com.notifyx.entity.EmailConfig;
import com.notifyx.service.EmailConfigService;
import com.notifyx.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/email-config")
@RequiredArgsConstructor
public class EmailConfigController {

    private final EmailConfigService emailConfigService;
    private final EmailService emailService;

    @GetMapping
    public ApiResponse<EmailConfig> getEmailConfig() {
        EmailConfig config = emailConfigService.getEmailConfig();
        return ApiResponse.success(config);
    }

    @PutMapping
    public ApiResponse<EmailConfig> updateEmailConfig(@RequestBody EmailConfigDTO dto) {
        EmailConfig config = emailConfigService.updateEmailConfig(dto);
        return ApiResponse.success(config);
    }

    @PostMapping("/test")
    public ApiResponse<String> testSendEmail(@RequestBody Map<String, String> request) {
        String testTo = request.get("testTo");
        EmailConfig config = emailConfigService.getEmailConfig();
        if (config == null) {
            return ApiResponse.error(400, "邮件配置不存在，请先配置邮件服务");
        }
        EmailConfigDTO configDTO = new EmailConfigDTO();
        configDTO.setSmtpHost(config.getSmtpHost());
        configDTO.setSmtpPort(config.getSmtpPort());
        configDTO.setUsername(config.getUsername());
        configDTO.setPassword(config.getPassword());
        configDTO.setFromName(config.getFromName());
        configDTO.setDefaultToEmail(config.getDefaultToEmail());
        configDTO.setEnabled(config.getEnabled());
        emailService.sendTestEmail(configDTO, testTo);
        return ApiResponse.success("测试邮件发送成功");
    }
}
