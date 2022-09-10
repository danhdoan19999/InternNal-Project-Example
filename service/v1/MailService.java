package com.nals.rw360.service.v1;

import com.nals.rw360.config.ApplicationProperties;
import com.nals.rw360.domain.User;
import com.nals.rw360.enums.RoleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import tech.jhipster.config.JHipsterProperties;

import javax.annotation.Nonnull;
import javax.mail.internet.MimeMessage;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MailService {
    private static final String CONTEXT_USER = "user";
    private static final String CONSOLE_URL = "consoleUrl";
    private static final String SERVICE_NAME = "serviceName";

    private final ApplicationProperties applicationProperties;
    private final JHipsterProperties jHipsterProperties;
    private final JavaMailSender javaMailSender;
    private final MessageSource messageSource;
    private final SpringTemplateEngine templateEngine;

    @Async
    public void sendRegisterEmail(final User user, @Nonnull final RoleType roleType) {
        String email = user.getEmail();
        Locale locale = Locale.forLanguageTag(user.getLangKey());

        log.info("Sending email register user to #{}", user.getEmail());

        Context context = new Context(locale);
        context.setVariable(CONTEXT_USER, user);
        context.setVariable(SERVICE_NAME, applicationProperties.getServiceName());
        context.setVariable(CONSOLE_URL, applicationProperties.getConsoleUrl());

        sendEmailFromTemplate(locale, context, email, "mail/registerEmail", "email.register.title");
    }

    @Async
    public void sendPasswordResetMail(final User user) {
        String email = user.getEmail();
        Locale locale = Locale.forLanguageTag(user.getLangKey());

        log.info("Sending password reset email to: #{} with locale: #{}", email, locale);

        Context context = new Context(locale);
        context.setVariable(CONTEXT_USER, user);
        context.setVariable(SERVICE_NAME, applicationProperties.getServiceName());
        context.setVariable(CONSOLE_URL, applicationProperties.getConsoleUrl());

        sendEmailFromTemplate(locale, context, email, "mail/passwordResetEmail", "email.password.reset.title");
    }

    private void sendEmailFromTemplate(final Locale locale,
                                       final Context context,
                                       final String toEmail,
                                       final String templateName,
                                       final String titleKey) {
        Object[] args = {applicationProperties.getServiceName()};
        String content = templateEngine.process(templateName, context);
        String subject = messageSource.getMessage(titleKey, args, locale);

        sendEmail(toEmail, subject, content, false, true);
    }

    private void sendEmail(final String to, final String subject, final String content,
                           final boolean isMultipart, final boolean isHtml) {
        log.debug("Send email[multipart '{}' and html '{}'] to '{}' with subject '{}' and content={}",
                  isMultipart, isHtml, to, subject, content);

        // Prepare message using a Spring helper
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart, StandardCharsets.UTF_8.name());
            message.setTo(to);
            message.setFrom(jHipsterProperties.getMail().getFrom());
            message.setSubject(subject);
            message.setText(content, isHtml);
            javaMailSender.send(mimeMessage);

            log.info("Sent email to user '#{}'", to);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Email could not be sent to user '{}'", to, e);
            } else {
                log.warn("Email could not be sent to user '{}': {}", to, e.getMessage());
            }
        }
    }
}
