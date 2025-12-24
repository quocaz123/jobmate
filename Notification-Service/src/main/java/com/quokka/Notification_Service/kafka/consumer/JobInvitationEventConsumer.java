package com.quokka.Notification_Service.kafka.consumer;

import com.quokka.Notification_Service.dto.request.Recipient;
import com.quokka.Notification_Service.dto.request.SendEmailRequest;
import com.quokka.Notification_Service.kafka.dto.JobInvitationEvent;
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
public class JobInvitationEventConsumer {
    EmailService emailService;

    @KafkaListener(topics = "job-invitation-event", groupId = "notification-group-v2", containerFactory = "jobInvitationEventKafkaListenerContainerFactory")
    public void handleJobInvitationEvent(JobInvitationEvent event) {
        log.info("Received JobInvitationEvent: type={}, invitationId={}", event.getEventType(),
                event.getInvitationId());

        try {
            switch (event.getEventType()) {
                case "SENT" -> sendInvitationEmail(event);
                case "ACCEPTED" -> sendAcceptanceEmail(event);
                case "REJECTED" -> sendRejectionEmail(event);
                default -> log.warn("Unknown event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Failed to send email for JobInvitationEvent: type={}, invitationId={}",
                    event.getEventType(), event.getInvitationId(), e);
        }
    }

    private void sendInvitationEmail(JobInvitationEvent event) {
        String emailSubject = "Bạn nhận được lời mời ứng tuyển - " + event.getJobTitle();
        String emailContent = """
                <div style='font-family:Arial,sans-serif;max-width:600px;margin:auto;padding:20px;border:1px solid #eee;border-radius:10px;background:#ffffff;'>
                    <div style='text-align:center;margin-bottom:30px;'>
                        <h1 style='color:#2c3e50;margin:0;'>🎉 Lời Mời Ứng Tuyển</h1>
                    </div>

                    <p style='color:#34495e;font-size:16px;line-height:1.6;'>Xin chào <strong>%s</strong>,</p>

                    <p style='color:#34495e;font-size:16px;line-height:1.6;'>
                        Chúng tôi rất vui mừng thông báo rằng bạn đã nhận được lời mời ứng tuyển cho vị trí:
                    </p>

                    <div style='background:#f8f9fa;padding:20px;border-radius:8px;margin:20px 0;border-left:4px solid #3498db;'>
                        <h2 style='color:#2c3e50;margin-top:0;'>%s</h2>
                        <p style='color:#7f8c8d;margin:5px 0;'><strong>Nhà tuyển dụng:</strong> %s</p>
                    </div>

                    %s

                    <div style='background:#e8f5e9;padding:15px;border-radius:8px;margin:20px 0;'>
                        <p style='color:#2e7d32;margin:0;font-weight:bold;'>📌 Bước tiếp theo:</p>
                        <p style='color:#2e7d32;margin:5px 0 0 0;'>Vui lòng đăng nhập vào JobMate Connect để xem chi tiết và phản hồi lời mời này.</p>
                    </div>

                    <div style='text-align:center;margin:30px 0;'>
                        <a href='https://jobmate.fun/invitations'
                           style='background:#3498db;color:#ffffff;padding:12px 30px;text-decoration:none;border-radius:5px;display:inline-block;font-weight:bold;'>
                            Xem Lời Mời
                        </a>
                    </div>

                    <hr style='border:none;border-top:1px solid #eee;margin:30px 0;'>

                    <p style='color:#7f8c8d;font-size:12px;text-align:center;margin:0;'>
                        © 2025 JobMate Connect - Kết nối việc làm tốt nhất
                    </p>
                </div>
                """
                .formatted(
                        event.getCandidateFullName(),
                        event.getJobTitle(),
                        event.getEmployerFullName(),
                        event.getMessage() != null && !event.getMessage().trim().isEmpty()
                                ? "<div style='background:#fff3cd;padding:15px;border-radius:8px;margin:20px 0;'><p style='color:#856404;margin:0;'><strong>💬 Lời nhắn từ nhà tuyển dụng:</strong></p><p style='color:#856404;margin:10px 0 0 0;'>"
                                        + event.getMessage() + "</p></div>"
                                : "");

        emailService.sendEmail(SendEmailRequest.builder()
                .to(Recipient.builder()
                        .email(event.getCandidateEmail())
                        .build())
                .subject(emailSubject)
                .htmlContent(emailContent)
                .build());

        log.info("Invitation email sent successfully to candidate: {}", event.getCandidateEmail());
    }

    private void sendAcceptanceEmail(JobInvitationEvent event) {
        String emailSubject = "Ứng viên đã chấp nhận lời mời - " + event.getJobTitle();
        String emailContent = """
                <div style='font-family:Arial,sans-serif;max-width:600px;margin:auto;padding:20px;border:1px solid #eee;border-radius:10px;background:#ffffff;'>
                    <div style='text-align:center;margin-bottom:30px;'>
                        <h1 style='color:#27ae60;margin:0;'>✅ Ứng Viên Đã Chấp Nhận</h1>
                    </div>

                    <p style='color:#34495e;font-size:16px;line-height:1.6;'>Xin chào <strong>%s</strong>,</p>

                    <p style='color:#34495e;font-size:16px;line-height:1.6;'>
                        Chúng tôi rất vui mừng thông báo rằng ứng viên đã chấp nhận lời mời của bạn:
                    </p>

                    <div style='background:#e8f5e9;padding:20px;border-radius:8px;margin:20px 0;border-left:4px solid #27ae60;'>
                        <h2 style='color:#2c3e50;margin-top:0;'>%s</h2>
                        <p style='color:#2e7d32;margin:5px 0;'><strong>Ứng viên:</strong> %s</p>
                        <p style='color:#2e7d32;margin:5px 0;'><strong>Vị trí:</strong> %s</p>
                    </div>

                    <div style='background:#e3f2fd;padding:15px;border-radius:8px;margin:20px 0;'>
                        <p style='color:#1565c0;margin:0;font-weight:bold;'>📌 Bước tiếp theo:</p>
                        <p style='color:#1565c0;margin:5px 0 0 0;'>Vui lòng đăng nhập vào JobMate Connect để liên hệ và trao đổi thêm với ứng viên.</p>
                    </div>

                    <div style='text-align:center;margin:30px 0;'>
                        <a href='https://jobmate.fun/applications'
                           style='background:#27ae60;color:#ffffff;padding:12px 30px;text-decoration:none;border-radius:5px;display:inline-block;font-weight:bold;'>
                            Xem Ứng Viên
                        </a>
                    </div>

                    <hr style='border:none;border-top:1px solid #eee;margin:30px 0;'>

                    <p style='color:#7f8c8d;font-size:12px;text-align:center;margin:0;'>
                        © 2025 JobMate Connect - Kết nối việc làm tốt nhất
                    </p>
                </div>
                """
                .formatted(
                        event.getEmployerFullName(),
                        event.getCandidateFullName(),
                        event.getCandidateFullName(),
                        event.getJobTitle());

        emailService.sendEmail(SendEmailRequest.builder()
                .to(Recipient.builder()
                        .email(event.getEmployerEmail())
                        .build())
                .subject(emailSubject)
                .htmlContent(emailContent)
                .build());

        log.info("Acceptance email sent successfully to employer: {}", event.getEmployerEmail());
    }

    private void sendRejectionEmail(JobInvitationEvent event) {
        String emailSubject = "Ứng viên đã từ chối lời mời - " + event.getJobTitle();
        String emailContent = """
                <div style='font-family:Arial,sans-serif;max-width:600px;margin:auto;padding:20px;border:1px solid #eee;border-radius:10px;background:#ffffff;'>
                    <div style='text-align:center;margin-bottom:30px;'>
                        <h1 style='color:#e74c3c;margin:0;'>❌ Ứng Viên Đã Từ Chối</h1>
                    </div>

                    <p style='color:#34495e;font-size:16px;line-height:1.6;'>Xin chào <strong>%s</strong>,</p>

                    <p style='color:#34495e;font-size:16px;line-height:1.6;'>
                        Chúng tôi xin thông báo rằng ứng viên đã từ chối lời mời của bạn:
                    </p>

                    <div style='background:#ffebee;padding:20px;border-radius:8px;margin:20px 0;border-left:4px solid #e74c3c;'>
                        <h2 style='color:#2c3e50;margin-top:0;'>%s</h2>
                        <p style='color:#c62828;margin:5px 0;'><strong>Ứng viên:</strong> %s</p>
                        <p style='color:#c62828;margin:5px 0;'><strong>Vị trí:</strong> %s</p>
                    </div>

                    <div style='background:#fff3e0;padding:15px;border-radius:8px;margin:20px 0;'>
                        <p style='color:#e65100;margin:0;font-weight:bold;'>💡 Gợi ý:</p>
                        <p style='color:#e65100;margin:5px 0 0 0;'>Bạn có thể tiếp tục tìm kiếm và mời các ứng viên khác phù hợp với vị trí này.</p>
                    </div>

                    <div style='text-align:center;margin:30px 0;'>
                        <a href='https://jobmate.fun/jobs'
                           style='background:#ff9800;color:#ffffff;padding:12px 30px;text-decoration:none;border-radius:5px;display:inline-block;font-weight:bold;'>
                            Tìm Ứng Viên Khác
                        </a>
                    </div>

                    <hr style='border:none;border-top:1px solid #eee;margin:30px 0;'>

                    <p style='color:#7f8c8d;font-size:12px;text-align:center;margin:0;'>
                        © 2025 JobMate Connect - Kết nối việc làm tốt nhất
                    </p>
                </div>
                """
                .formatted(
                        event.getEmployerFullName(),
                        event.getCandidateFullName(),
                        event.getCandidateFullName(),
                        event.getJobTitle());

        emailService.sendEmail(SendEmailRequest.builder()
                .to(Recipient.builder()
                        .email(event.getEmployerEmail())
                        .build())
                .subject(emailSubject)
                .htmlContent(emailContent)
                .build());

        log.info("Rejection email sent successfully to employer: {}", event.getEmployerEmail());
    }
}
