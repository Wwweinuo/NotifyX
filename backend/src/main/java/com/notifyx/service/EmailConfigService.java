package com.notifyx.service;

import com.notifyx.dto.EmailConfigDTO;
import com.notifyx.entity.EmailConfig;
import com.notifyx.repository.EmailConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailConfigService {

    private final EmailConfigRepository emailConfigRepository;

    /**
     * 获取邮件配置（系统只有一条配置记录）
     */
    public EmailConfig getEmailConfig() {
        return emailConfigRepository.findAll().stream()
                .findFirst()
                .orElse(null);
    }

    /**
     * 更新邮件配置（存在则更新，不存在则创建）
     */
    public EmailConfig updateEmailConfig(EmailConfigDTO dto) {
        EmailConfig config = emailConfigRepository.findAll().stream()
                .findFirst()
                .orElse(new EmailConfig());

        config.setSmtpHost(dto.getSmtpHost());
        config.setSmtpPort(dto.getSmtpPort());
        config.setUsername(dto.getUsername());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            config.setPassword(dto.getPassword());
        }
        config.setFromName(dto.getFromName());
        config.setDefaultToEmail(dto.getDefaultToEmail());
        config.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);

        return emailConfigRepository.save(config);
    }
}
