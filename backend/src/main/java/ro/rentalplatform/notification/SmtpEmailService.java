package ro.rentalplatform.notification;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@Profile("prod")
public class SmtpEmailService implements EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public SmtpEmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Override
    public void send(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);

            Context context = new Context();
            variables.forEach(context::setVariable);
            String html = templateEngine.process(templateName, context);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException ex) {
            throw new IllegalStateException("Email-ul nu a putut fi trimis.", ex);
        }
    }
}
