package ro.rentalplatform.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "false", matchIfMissing = true)
public class LoggingEmailService implements EmailService {
    private static final Logger log = LoggerFactory.getLogger(LoggingEmailService.class);

    @Override
    public void send(String to, String subject, String templateName, Map<String, Object> variables) {
        log.info("[DEV EMAIL] To: {} | Subject: {} | Template: {} | Variables: {}", to, subject, templateName, variables);
    }
}
