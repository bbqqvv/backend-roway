package org.bbqqvv.backendecommerce.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.bbqqvv.backendecommerce.entity.Order;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @org.springframework.scheduling.annotation.Async
    public void sendOrderConfirmationEmail(Order order, String toEmail) {
        log.info("Sending order confirmation email to: {}", toEmail);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Xác nhận đơn hàng " + order.getOrderCode());
            
            Context context = new Context();
            context.setVariable("order", order);
            context.setVariable("orderItems", order.getOrderItems());

            String htmlContent = templateEngine.process("order-confirmation", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send order confirmation email to: {}", toEmail, e);
        }
    }
}
