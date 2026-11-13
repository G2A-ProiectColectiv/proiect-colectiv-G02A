package ro.rentalplatform.notification;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
public class SmtpEmailService implements EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final String from;

    public SmtpEmailService(JavaMailSender mailSender,
                            TemplateEngine templateEngine,
                            @Value("${app.mail.from:}") String from) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.from = from;
    }

    @Override
    public void send(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            if (!from.isBlank()) {
                helper.setFrom(from);
            }
            helper.setTo(to);
            helper.setSubject(subject);

            Context context = new Context();
            variables.forEach(context::setVariable);
            String html = templateEngine.process(templateName, context);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException | MailException ex) {
            throw new IllegalStateException("Email-ul nu a putut fi trimis.", ex);
        }
    }
}
