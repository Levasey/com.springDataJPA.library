package com.springdatajpa.library.services;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Письмо со ссылкой на сброс пароля каталога (та же форма установки пароля, что и при приглашении).
 */
@Service
public class PasswordResetMailService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetMailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final ReaderWelcomeMailService readerWelcomeMailService;
    private final boolean enabled;
    private final String fromAddress;
    private final String welcomeReaderFrom;
    private final String publicBaseUrl;

    public PasswordResetMailService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            ReaderWelcomeMailService readerWelcomeMailService,
            @Value("${library.mail.password-reset.enabled:false}") boolean enabled,
            @Value("${library.mail.password-reset.from:}") String fromOverride,
            @Value("${library.mail.welcome-reader.from:}") String welcomeReaderFrom,
            @Value("${library.app.public-base-url:}") String publicBaseUrl) {
        this.mailSenderProvider = mailSenderProvider;
        this.readerWelcomeMailService = readerWelcomeMailService;
        this.enabled = enabled;
        this.fromAddress = fromOverride == null ? "" : fromOverride;
        this.welcomeReaderFrom = welcomeReaderFrom == null ? "" : welcomeReaderFrom;
        this.publicBaseUrl = publicBaseUrl == null ? "" : publicBaseUrl.trim();
    }

    public void sendPasswordResetIfConfigured(String recipientEmail, String rawToken) {
        if (!enabled) {
            return;
        }
        if (publicBaseUrl.isEmpty()) {
            log.warn(
                    "library.mail.password-reset.enabled=true, но не задан library.app.public-base-url — письмо не отправлено.");
            return;
        }
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn(
                    "library.mail.password-reset.enabled=true, но нет JavaMailSender (задайте spring.mail.host и параметры SMTP)");
            return;
        }
        String from = resolveFrom(mailSender);
        if (from == null || from.isBlank()) {
            log.warn(
                    "Не задан отправитель: library.mail.password-reset.from, library.mail.welcome-reader.from или spring.mail.username");
            return;
        }

        String resetUrl = readerWelcomeMailService.buildSetupLinkForHandoff(rawToken);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(recipientEmail);
            helper.setSubject("Восстановление пароля каталога библиотеки");
            helper.setText(
                    String.format(
                            """
                            Здравствуйте!

                            Вы запросили восстановление пароля для входа в электронный каталог библиотеки.

                            Установите новый пароль по ссылке (однократно):
                            %s

                            Если вы не запрашивали сброс, проигнорируйте это письмо.

                            Если ссылка не открывается, скопируйте её целиком в браузер.
                            """,
                            resetUrl),
                    false);

            mailSender.send(message);
        } catch (Exception e) {
            log.error("Не удалось отправить письмо восстановления пароля на {}", recipientEmail, e);
        }
    }

    private String resolveFrom(JavaMailSender mailSender) {
        if (fromAddress != null && !fromAddress.isBlank()) {
            return fromAddress.trim();
        }
        if (welcomeReaderFrom != null && !welcomeReaderFrom.isBlank()) {
            return welcomeReaderFrom.trim();
        }
        if (mailSender instanceof org.springframework.mail.javamail.JavaMailSenderImpl impl) {
            String user = impl.getUsername();
            if (user != null && !user.trim().isBlank()) {
                return user.trim();
            }
        }
        return null;
    }
}
