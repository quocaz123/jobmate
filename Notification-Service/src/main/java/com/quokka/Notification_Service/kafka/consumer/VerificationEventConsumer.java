package com.quokka.Notification_Service.kafka.consumer;

import com.quokka.Notification_Service.dto.request.Recipient;
import com.quokka.Notification_Service.dto.request.SendEmailRequest;
import com.quokka.Notification_Service.kafka.dto.VerificationRequestEvent;
import com.quokka.Notification_Service.kafka.dto.VerificationResultEvent;
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
public class VerificationEventConsumer {
    EmailService emailService;

    @KafkaListener(topics = "verification-request", groupId = "notification-group",
                   containerFactory = "verificationRequestEventKafkaListenerContainerFactory")
    public void handleVerificationRequestEvent(VerificationRequestEvent event) {
        log.info("Received VerificationRequestEvent for userId: {}", event.getUserId());

        String emailSubject = "Yêu cầu xác thực tài khoản đã được gửi";
        String emailContent = """
                <div style='font-family:Arial,sans-serif;max-width:600px;margin:auto;padding:20px;border:1px solid #eee;border-radius:10px;'>
                    <h2 style='color:#2c3e50'>Xác thực tài khoản</h2>
                    <p>Xin chào %s,</p>
                    <p>Yêu cầu xác thực tài khoản của bạn đã được gửi thành công.</p>
                    <p>Chúng tôi sẽ xem xét và phản hồi trong thời gian sớm nhất.</p>
                    <p>Cảm ơn bạn đã sử dụng JobMate Connect!</p>
                    <hr>
                    <p style='font-size:12px;color:#7f8c8d'>© 2025 JobMate Connect</p>
                </div>
                """
                .formatted(event.getFullName());

        emailService.sendEmail(SendEmailRequest.builder()
                .to(Recipient.builder()
                        .email(event.getEmail())
                        .build())
                .subject(emailSubject)
                .htmlContent(emailContent)
                .build());
    }

    @KafkaListener(topics = "verification-result", groupId = "notification-group",
                   containerFactory = "verificationResultEventKafkaListenerContainerFactory")
    public void handleVerificationResult(VerificationResultEvent event) {
        log.info("Received VerificationResultEvent for userId: {}", event.getUserId());

        if (event.isApproved()) {
            handleApprovedVerification(event);
        } else {
            handleRejectedVerification(event);
        }
    }

    public void handleApprovedVerification(VerificationResultEvent event) {

        String emailSubject = "Tài khoản đã được xác thực thành công";
        String emailContent = """
                <div style='font-family:Arial,sans-serif;max-width:600px;margin:auto;padding:20px;border:1px solid #eee;border-radius:10px;'>
                    <h2 style='color:#27ae60'>🎉 Chúc mừng!</h2>
                    <p>Xin chào %s,</p>
                    <p>Tài khoản của bạn đã được xác thực thành công!</p>
                    <p>Bây giờ bạn có thể sử dụng đầy đủ các tính năng của JobMate Connect.</p>
                    <div style='background:#f8f9fa;padding:15px;border-radius:5px;margin:20px 0;'>
                        <h3>Những gì bạn có thể làm tiếp theo:</h3>
                        <ul>
                            <li>Đăng tin tuyển dụng (nếu là nhà tuyển dụng)</li>
                            <li>Tìm kiếm việc làm phù hợp</li>
                            <li>Kết nối với các ứng viên tiềm năng</li>
                        </ul>
                    </div>
                    <p>Cảm ơn bạn đã tin tưởng JobMate Connect!</p>
                    <hr>
                    <p style='font-size:12px;color:#7f8c8d'>© 2025 JobMate Connect</p>
                </div>
                """
                .formatted(event.getFullName());

        emailService.sendEmail(SendEmailRequest.builder()
                .to(Recipient.builder()
                        .email(event.getEmail())
                        .build())
                .subject(emailSubject)
                .htmlContent(emailContent)
                .build());
    }

    public void handleRejectedVerification(VerificationResultEvent event) {
        String emailSubject = "Yêu cầu xác thực tài khoản bị từ chối";
        String emailContent = """
                <div style='font-family:Arial,sans-serif;max-width:600px;margin:auto;padding:20px;border:1px solid #eee;border-radius:10px;'>
                    <h2 style='color:#e74c3c'>Yêu cầu xác thực bị từ chối</h2>
                    <p>Xin chào %s,</p>
                    <p>Rất tiếc, yêu cầu xác thực tài khoản của bạn đã bị từ chối.</p>
                    <div style='background:#fdf2f2;padding:15px;border-radius:5px;margin:20px 0;border-left:4px solid #e74c3c;'>
                        <h3>Lý do từ chối:</h3>
                        <p><strong>%s</strong></p>
                    </div>
                    <div style='background:#f8f9fa;padding:15px;border-radius:5px;margin:20px 0;'>
                        <h3>Bạn có thể:</h3>
                        <ul>
                            <li>Kiểm tra lại thông tin đã cung cấp</li>
                            <li>Upload lại giấy tờ tùy thân với chất lượng tốt hơn</li>
                            <li>Liên hệ hỗ trợ nếu cần trợ giúp</li>
                        </ul>
                    </div>
                    <p>Bạn có thể gửi lại yêu cầu xác thực sau khi khắc phục các vấn đề trên.</p>
                    <hr>
                    <p style='font-size:12px;color:#7f8c8d'>© 2025 JobMate Connect</p>
                </div>
                """
                .formatted(event.getFullName(), event.getReason());

        emailService.sendEmail(SendEmailRequest.builder()
                .to(Recipient.builder()
                        .email(event.getEmail())
                        .build())
                .subject(emailSubject)
                .htmlContent(emailContent)
                .build());
    }
}
