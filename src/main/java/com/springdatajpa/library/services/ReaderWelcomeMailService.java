package com.springdatajpa.library.services;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Приветственное письмо читателю: логин, номер билета и ссылка на установку пароля.
 */
@Service
public class ReaderWelcomeMailService {

    private static final Logger log = LoggerFactory.getLogger(ReaderWelcomeMailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final boolean enabled;
    private final String fromAddress;
    private final String publicBaseUrl;

    public ReaderWelcomeMailService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${library.mail.welcome-reader.enabled:false}") boolean enabled,
            @Value("${library.mail.welcome-reader.from:}") String fromOverride,
            @Value("${library.app.public-base-url:}") String publicBaseUrl) {
        this.mailSenderProvider = mailSenderProvider;
        this.enabled = enabled;
        this.fromAddress = fromOverride == null ? "" : fromOverride;
        this.publicBaseUrl = publicBaseUrl == null ? "" : publicBaseUrl.trim();
    }

    /** Письмо реально будет отправлено после коммита (все предпосылки выполнены). */
    public boolean willSendWelcomeEmail() {
        if (!enabled || publicBaseUrl.isEmpty()) {
            return false;
        }
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            return false;
        }
        String from = resolveFrom(mailSender);
        return from != null && !from.isBlank();
    }

    /**
     * Полная или относительная ссылка на форму установки пароля (для письма и для передачи читателю вручную).
     */
    public String buildSetupLinkForHandoff(String rawSetupToken) {
        String pathAndQuery = UriComponentsBuilder.fromPath("/catalog/setup-password")
                .queryParam("token", rawSetupToken)
                .build()
                .toUriString();
        if (publicBaseUrl.isEmpty()) {
            return pathAndQuery;
        }
        return stripTrailingSlash(publicBaseUrl) + pathAndQuery;
    }

    public void sendWelcomeIfConfigured(
            String recipientEmail, String catalogLogin, String rawSetupToken, String readerCardNumber) {
        if (!enabled) {
            return;
        }
        if (publicBaseUrl.isEmpty()) {
            log.warn(
                    "library.mail.welcome-reader.enabled=true, но не задан library.app.public-base-url — письмо не отправлено.");
            return;
        }
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn(
                    "library.mail.welcome-reader.enabled=true, но нет JavaMailSender (задайте spring.mail.host и прочие параметры SMTP)");
            return;
        }
        String from = resolveFrom(mailSender);
        if (from == null || from.isBlank()) {
            log.warn(
                    "Не задан отправитель письма: library.mail.welcome-reader.from или spring.mail.username");
            return;
        }

        String setupUrl = buildSetupLinkForHandoff(rawSetupToken);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(recipientEmail);
            helper.setSubject("Доступ к каталогу библиотеки");
            helper.setText(
                    String.format(
                            """
                            Здравствуйте!

                            Вам создана учётная запись читателя в электронном каталоге.

                            Логин (email): %s
                            Номер читательского билета: %s

                            Установите пароль для входа по ссылке (однократно):
                            %s

                            Если ссылка не открывается, скопируйте её целиком в браузер.
                            """,
                            catalogLogin, readerCardNumber, setupUrl),
                    false);

            mailSender.send(message);
        } catch (Exception e) {
            log.error("Не удалось отправить приветственное письмо на {}", recipientEmail, e);
        }
    }

    private static String stripTrailingSlash(String base) {
        if (base.endsWith("/")) {
            return base.substring(0, base.length() - 1);
        }
        return base;
    }

    private String resolveFrom(JavaMailSender mailSender) {
        if (fromAddress != null && !fromAddress.isBlank()) {
            return fromAddress.trim();
        }
        if (mailSender instanceof org.springframework.mail.javamail.JavaMailSenderImpl impl) {
            String user = impl.getUsername();
            return user != null ? user.trim() : null;
        }
        return null;
    }
}
