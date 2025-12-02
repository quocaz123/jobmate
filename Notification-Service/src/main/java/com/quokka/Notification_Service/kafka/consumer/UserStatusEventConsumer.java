package com.quokka.Notification_Service.kafka.consumer;

import com.quokka.Notification_Service.dto.request.Recipient;
import com.quokka.Notification_Service.dto.request.SendEmailRequest;
import com.quokka.Notification_Service.kafka.dto.UserStatusChangeEvent;
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
public class UserStatusEventConsumer {
    EmailService emailService;

    @KafkaListener(topics = "user-status-change", groupId = "notification-group",
                   containerFactory = "userStatusChangeEventKafkaListenerContainerFactory")
    public void handleUserStatusChangeEvent(UserStatusChangeEvent event) {
        log.info("Received UserStatusChangeEvent for userId: {}", event.getUserId());

        if ("BANNED".equalsIgnoreCase(event.getStatus())) {
            handleBannedUser(event);
        } else if ("ACTIVE".equalsIgnoreCase(event.getStatus())) {
            handleActivatedUser(event);
        }
    }

    private void handleBannedUser(UserStatusChangeEvent event) {
        String emailSubject = "Tài khoản đã bị khóa";
        String reason = event.getReason() != null && !event.getReason().isEmpty()
                ? event.getReason()
                : "Vi phạm quy định cộng đồng";

        String emailContent = """
                <div style='font-family:Arial,sans-serif;max-width:600px;margin:auto;padding:20px;border:1px solid #eee;border-radius:10px;'>
                    <h2 style='color:#e74c3c'>⛔ Tài khoản đã bị khóa</h2>
                    <p>Xin chào %s,</p>
                    <p>Tài khoản của bạn đã bị khóa do vi phạm quy định của JobMate Connect.</p>
                    <div style='background:#fdf2f2;padding:15px;border-radius:5px;margin:20px 0;border-left:4px solid #e74c3c;'>
                        <h3>Lý do khóa tài khoản:</h3>
                        <p><strong>%s</strong></p>
                    </div>
                    <div style='background:#f8f9fa;padding:15px;border-radius:5px;margin:20px 0;'>
                        <h3>Bạn có thể:</h3>
                        <ul>
                            <li>Liên hệ với bộ phận hỗ trợ nếu bạn cho rằng đây là nhầm lẫn</li>
                            <li>Gửi email đến support@jobmateconnect.com để được hỗ trợ</li>
                            <li>Kiểm tra lại các hoạt động của bạn và tuân thủ quy định cộng đồng</li>
                        </ul>
                    </div>
                    <p>Chúng tôi rất tiếc về sự bất tiện này. Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi.</p>
                    <hr>
                    <p style='font-size:12px;color:#7f8c8d'>© 2025 JobMate Connect</p>
                </div>
                """
                .formatted(event.getFullName() != null ? event.getFullName() : "Người dùng", reason);

        try {
            emailService.sendEmail(SendEmailRequest.builder()
                    .to(Recipient.builder()
                            .email(event.getEmail())
                            .build())
                    .subject(emailSubject)
                    .htmlContent(emailContent)
                    .build());
            log.info("Banned user email sent successfully to: {}", event.getEmail());
        } catch (Exception e) {
            log.error("Failed to send banned user email to: {}", event.getEmail(), e);
        }
    }

    private void handleActivatedUser(UserStatusChangeEvent event) {
        String emailSubject = "Tài khoản đã được mở khóa";
        String emailContent = """
                <div style='font-family:Arial,sans-serif;max-width:600px;margin:auto;padding:20px;border:1px solid #eee;border-radius:10px;'>
                    <h2 style='color:#27ae60'>✅ Tài khoản đã được mở khóa</h2>
                    <p>Xin chào %s,</p>
                    <p>Tài khoản của bạn đã được mở khóa thành công!</p>
                    <p>Bây giờ bạn có thể sử dụng đầy đủ các tính năng của JobMate Connect.</p>
                    <div style='background:#f0f9ff;padding:15px;border-radius:5px;margin:20px 0;border-left:4px solid #27ae60;'>
                        <h3>Những gì bạn có thể làm:</h3>
                        <ul>
                            <li>Đăng nhập và sử dụng hệ thống bình thường</li>
                            <li>Tìm kiếm việc làm hoặc đăng tin tuyển dụng</li>
                            <li>Kết nối với cộng đồng JobMate Connect</li>
                        </ul>
                    </div>
                    <p>Cảm ơn bạn đã kiên nhẫn. Chúng tôi rất vui được chào đón bạn trở lại!</p>
                    <hr>
                    <p style='font-size:12px;color:#7f8c8d'>© 2025 JobMate Connect</p>
                </div>
                """
                .formatted(event.getFullName() != null ? event.getFullName() : "Người dùng");

        try {
            emailService.sendEmail(SendEmailRequest.builder()
                    .to(Recipient.builder()
                            .email(event.getEmail())
                            .build())
                    .subject(emailSubject)
                    .htmlContent(emailContent)
                    .build());
            log.info("Activated user email sent successfully to: {}", event.getEmail());
        } catch (Exception e) {
            log.error("Failed to send activated user email to: {}", event.getEmail(), e);
        }
    }
}

