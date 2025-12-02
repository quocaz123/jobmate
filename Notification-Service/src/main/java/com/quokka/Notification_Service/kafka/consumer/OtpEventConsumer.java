package com.quokka.Notification_Service.kafka.consumer;

import com.quokka.Notification_Service.dto.request.Recipient;
import com.quokka.Notification_Service.dto.request.SendEmailRequest;
import com.quokka.Notification_Service.kafka.dto.SendOtpEvent;
import com.quokka.Notification_Service.service.EmailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OtpEventConsumer {
    EmailService emailService;

    @KafkaListener(topics = "send-otp", groupId = "notification-group", containerFactory = "sendOtpEventKafkaListenerContainerFactory")
    public void handleSendOtpEvent(SendOtpEvent event) {
        log.info("Received SendOtpEvent for email: {}", event.getEmail());

        // Gửi email OTP
        String emailSubject = "Mã OTP xác thực tài khoản";
        String emailContent = """
                <div style='font-family:Arial,sans-serif;max-width:600px;margin:auto;padding:20px;border:1px solid #eee;border-radius:10px;'>
                    <h2 style='color:#2c3e50'>Mã OTP xác thực</h2>
                    <p>Xin chào,</p>
                    <p>Mã OTP của bạn là: <strong style='font-size:24px;color:#e74c3c;background:#f8f9fa;padding:10px;border-radius:5px;display:inline-block;'>%s</strong></p>
                    <p>Mã này có hiệu lực trong 3 phút. Vui lòng không chia sẻ mã này với bất kỳ ai.</p>
                    <p>Cảm ơn bạn đã sử dụng JobMate Connect!</p>
                    <hr>
                    <p style='font-size:12px;color:#7f8c8d'>© 2025 JobMate Connect</p>
                </div>
                """
                .formatted(event.getOtp());

        try {
            emailService.sendEmail(SendEmailRequest.builder()
                    .to(Recipient.builder()
                            .email(event.getEmail())
                            .build())
                    .subject(emailSubject)
                    .htmlContent(emailContent)
                    .build());
            log.info("OTP email sent successfully to: {}", event.getEmail());
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", event.getEmail(), e);
        }
    }
}