package com.quokka.Notification_Service.kafka.consumer;

import com.quokka.Notification_Service.dto.request.Recipient;
import com.quokka.Notification_Service.dto.request.SendEmailRequest;
import com.quokka.Notification_Service.service.EmailService;
import com.quokka.Notification_Service.kafka.dto.SendOtpEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpEventConsumer {

    private final EmailService emailService;
    @KafkaListener(topics = "send-otp", groupId = "notification-group")
    public void handleOtpEvent(SendOtpEvent event) {
        String subject = "Your JobMate Verification Code";

        String htmlContent = """
        <div style='font-family:Arial,sans-serif;max-width:600px;margin:auto;padding:20px;border:1px solid #eee;border-radius:10px;'>
            <h2 style='color:#2c3e50'>JobMate Verification</h2>
            <p>Dear user,</p>
            <p>Your one-time password (OTP) for account verification is:</p>
            <h1 style='color:#e74c3c;letter-spacing:4px;text-align:center;'>%s</h1>
            <p>This code will expire in <b>3 minutes</b>.</p>
            <hr>
            <p style='font-size:12px;color:#7f8c8d'>
                If you did not request this code, please ignore this email.<br>
                © 2025 JobMate Connect
            </p>
        </div>
        """.formatted(event.getOtp());

        emailService.sendEmail(SendEmailRequest.builder()
                .to(Recipient.builder()
                        .email(event.getEmail())
                        .build())
                .subject(subject)
                .htmlContent(htmlContent)
                .build());

        log.info("Received SendOtpEvent for email: {}", event.getEmail());
    }
}
