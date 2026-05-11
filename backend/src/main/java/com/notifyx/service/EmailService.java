package com.notifyx.service;

import com.notifyx.dto.EmailConfigDTO;
import com.notifyx.entity.EmailConfig;
import com.notifyx.repository.EmailConfigRepository;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final EmailConfigRepository emailConfigRepository;

    /**
     * 发送邮件
     * @param to 收件人
     * @param subject 主题
     * @param content 内容（纯文本）
     */
    public void sendEmail(String to, String subject, String content) {
        EmailConfig config = emailConfigRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("邮件配置不存在，请先配置邮件服务"));

        if (!Boolean.TRUE.equals(config.getEnabled())) {
            throw new RuntimeException("邮件服务未启用");
        }

        JavaMailSender mailSender = createMailSender(config);
        sendMimeEmail(mailSender, config, to, subject, content);
        log.info("邮件发送成功: to={}, subject={}", to, subject);
    }

    /**
     * 测试邮件发送
     * @param configDTO 邮件配置
     * @param testTo 测试收件人
     */
    public void sendTestEmail(EmailConfigDTO configDTO, String testTo) {
        EmailConfig config = new EmailConfig();
        config.setSmtpHost(configDTO.getSmtpHost());
        config.setSmtpPort(configDTO.getSmtpPort());
        config.setUsername(configDTO.getUsername());
        config.setPassword(configDTO.getPassword());
        config.setFromName(configDTO.getFromName());

        JavaMailSender mailSender = createMailSender(config);
        sendMimeEmail(mailSender, config, testTo, "NotifyX 测试邮件", "这是一封来自 NotifyX 的测试邮件，如果您收到此邮件说明邮件配置正确。");
        log.info("测试邮件发送成功: to={}", testTo);
    }

    /**
     * 使用 MimeMessage 发送邮件，正确编码中文发件人名称
     */
    private void sendMimeEmail(JavaMailSender mailSender, EmailConfig config, String to, String subject, String content) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(new InternetAddress(config.getUsername(), config.getFromName(), "UTF-8"));
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据数据库配置创建 JavaMailSender
     */
    private JavaMailSender createMailSender(EmailConfig config) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(config.getSmtpHost());
        mailSender.setPort(config.getSmtpPort());
        mailSender.setUsername(config.getUsername());
        mailSender.setPassword(config.getPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");

        return mailSender;
    }
}
