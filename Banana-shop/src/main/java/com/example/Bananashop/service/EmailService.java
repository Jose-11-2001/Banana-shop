package com.example.Bananashop.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;  // ✅ Add this import

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private TemplateEngine templateEngine;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;
    
    // Send simple email
    public void sendEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, false);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    // Send HTML email using template
    public void sendHtmlEmail(String to, String subject, String templateName, Context context) {
        try {
            String htmlContent = templateEngine.process(templateName, context);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }
    
    // Order confirmation email
    public void sendOrderConfirmation(String to, String customerName, Long orderId, Double totalAmount) {
        Context context = new Context();
        context.setVariable("customerName", customerName);
        context.setVariable("orderId", orderId);
        context.setVariable("totalAmount", totalAmount);
        context.setVariable("orderDate", java.time.LocalDateTime.now());
        
        sendHtmlEmail(to, "Order Confirmation #" + orderId, "order-confirmation", context);
    }
    
    // Order status update email
    public void sendOrderStatusUpdate(String to, String customerName, Long orderId, 
                                      String status, String rejectionReason) {
        Context context = new Context();
        context.setVariable("customerName", customerName);
        context.setVariable("orderId", orderId);
        context.setVariable("status", status);
        context.setVariable("rejectionReason", rejectionReason);
        
        sendHtmlEmail(to, "Order #" + orderId + " Status Update", "order-status-update", context);
    }
    
    // Welcome email
    public void sendWelcomeEmail(String to, String name) {
        Context context = new Context();
        context.setVariable("name", name);
        
        sendHtmlEmail(to, "Welcome to Banana Shop!", "welcome", context);
    }
    
    // ✅ Password reset email - FIXED version
    public void sendPasswordResetEmail(String to, String resetToken) {
        String resetLink = frontendUrl + "/auth/reset-password?token=" + resetToken;
        String subject = "Reset Your Password - Banana Shop";
        
        Context context = new Context();
        context.setVariable("resetLink", resetLink);
        context.setVariable("name", to.split("@")[0]);
        
        sendHtmlEmail(to, subject, "password-reset", context);
    }
}