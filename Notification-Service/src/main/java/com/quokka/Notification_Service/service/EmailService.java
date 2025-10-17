package com.quokka.Notification_Service.service;

import com.quokka.Notification_Service.dto.request.EmailRequest;
import com.quokka.Notification_Service.dto.request.SendEmailRequest;
import com.quokka.Notification_Service.dto.request.Sender;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailService {

    JavaMailSender mailSender;

    public String sendEmail(SendEmailRequest request) {
        EmailRequest emailRequest = EmailRequest.builder()
                .sender(Sender.builder()
                        .name("JobMate Connect")
                        .email("quocthangbinh234@gmail.com")
                        .build())
                .to(List.of(request.getTo()))
                .subject(request.getSubject())
                .htmlContent(request.getHtmlContent())
                .build();

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailRequest.getSender().getEmail(), emailRequest.getSender().getName());
            helper.setTo(emailRequest.getTo().get(0).getEmail());
            helper.setSubject(emailRequest.getSubject());
            helper.setText(emailRequest.getHtmlContent(), true);

            mailSender.send(message);

            return "Email sent successfully to " + emailRequest.getTo().get(0).getEmail();
        } catch (Exception e) {
            return "Error while sending email: " + e.getMessage();
        }
    }
}
