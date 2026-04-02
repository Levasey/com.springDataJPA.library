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
 * Отправка читателю временного пароля и номера билета при создании учётной записи каталога.
 */
@Service
public class ReaderWelcomeMailService {

    private static final Logger log = LoggerFactory.getLogger(ReaderWelcomeMailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final boolean enabled;
    private final String fromAddress;

    public ReaderWelcomeMailService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${library.mail.welcome-reader.enabled:false}") boolean enabled,
            @Value("${library.mail.welcome-reader.from:}") String fromOverride) {
        this.mailSenderProvider = mailSenderProvider;
        this.enabled = enabled;
        this.fromAddress = fromOverride == null ? "" : fromOverride;
    }

    public void sendWelcomeIfConfigured(
            String recipientEmail, String catalogLogin, String plainPassword, String readerCardNumber) {
        if (!enabled) {
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
                            Пароль: %s
                            Номер читательского билета: %s

                            Рекомендуем сменить пароль после первого входа, если в каталоге появится такая возможность.
                            """,
                            catalogLogin, plainPassword, readerCardNumber),
                    false);

            mailSender.send(message);
        } catch (Exception e) {
            log.error("Не удалось отправить приветственное письмо на {}", recipientEmail, e);
        }
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
