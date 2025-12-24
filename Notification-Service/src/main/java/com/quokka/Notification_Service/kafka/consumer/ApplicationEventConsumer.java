package com.quokka.Notification_Service.kafka.consumer;

import com.quokka.Notification_Service.dto.request.Recipient;
import com.quokka.Notification_Service.dto.request.SendEmailRequest;
import com.quokka.Notification_Service.kafka.dto.ApplicationCreatedEvent;
import com.quokka.Notification_Service.kafka.dto.ApplicationStatusUpdatedEvent;
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
public class ApplicationEventConsumer {
    EmailService emailService;

    @KafkaListener(topics = "application-created-event", groupId = "notification-group-v2", containerFactory = "applicationCreatedEventKafkaListenerContainerFactory")
    public void handleApplicationCreatedEvent(ApplicationCreatedEvent event) {
        log.info("Received ApplicationCreatedEvent: applicationId={}, candidateName={}, jobTitle={}",
                event.getApplicationId(), event.getCandidateFullName(), event.getJobTitle());

        try {
            sendApplicationEmailToEmployer(event);
        } catch (Exception e) {
            log.error("Failed to send email for ApplicationCreatedEvent: applicationId={}",
                    event.getApplicationId(), e);
        }
    }

    private void sendApplicationEmailToEmployer(ApplicationCreatedEvent event) {
        String emailSubject = "Đơn ứng tuyển mới - " + event.getJobTitle();

        // Format match score
        String matchScoreDisplay = String.format("%.1f", event.getMatchScore());
        String matchScoreColor = event.getMatchScore() >= 70 ? "#27ae60"
                : event.getMatchScore() >= 50 ? "#f39c12" : "#e74c3c";
        String matchScoreLabel = event.getMatchScore() >= 70 ? "Rất phù hợp"
                : event.getMatchScore() >= 50 ? "Khá phù hợp" : "Cần xem xét";

        String emailContent = """
                <div style='font-family:Arial,sans-serif;max-width:600px;margin:auto;padding:20px;border:1px solid #eee;border-radius:10px;background:#ffffff;'>
                    <div style='text-align:center;margin-bottom:30px;'>
                        <h1 style='color:#2c3e50;margin:0;'>📋 Đơn Ứng Tuyển Mới</h1>
                    </div>

                    <p style='color:#34495e;font-size:16px;line-height:1.6;'>Xin chào <strong>%s</strong>,</p>

                    <p style='color:#34495e;font-size:16px;line-height:1.6;'>
                        Bạn vừa nhận được một đơn ứng tuyển mới cho vị trí:
                    </p>

                    <div style='background:#e3f2fd;padding:20px;border-radius:8px;margin:20px 0;border-left:4px solid #2196f3;'>
                        <h2 style='color:#2c3e50;margin-top:0;'>%s</h2>
                    </div>

                    <div style='background:#f8f9fa;padding:20px;border-radius:8px;margin:20px 0;'>
                        <h3 style='color:#2c3e50;margin-top:0;border-bottom:2px solid #3498db;padding-bottom:10px;'>👤 Thông tin ứng viên</h3>
                        <p style='color:#34495e;margin:10px 0;'><strong>Tên:</strong> %s</p>
                        <p style='color:#34495e;margin:10px 0;'><strong>Email:</strong> %s</p>
                        <div style='margin:15px 0;padding:15px;background:#ffffff;border-radius:5px;border:1px solid #ddd;'>
                            <p style='color:#34495e;margin:0 0 10px 0;'><strong>Điểm khớp:</strong></p>
                            <div style='text-align:center;'>
                                <span style='font-size:32px;font-weight:bold;color:%s;'>%s%%</span>
                                <p style='color:%s;margin:5px 0 0 0;font-weight:bold;'>%s</p>
                            </div>
                        </div>
                        %s
                    </div>

                    %s

                    <div style='background:#fff3cd;padding:15px;border-radius:8px;margin:20px 0;'>
                        <p style='color:#856404;margin:0;font-weight:bold;'>📌 Bước tiếp theo:</p>
                        <p style='color:#856404;margin:5px 0 0 0;'>Vui lòng đăng nhập vào JobMate Connect để xem chi tiết đơn ứng tuyển và phản hồi ứng viên.</p>
                    </div>

                    <div style='text-align:center;margin:30px 0;'>
                        <a href='https://jobmate.fun/applications?jobId=%s'
                           style='background:#3498db;color:#ffffff;padding:12px 30px;text-decoration:none;border-radius:5px;display:inline-block;font-weight:bold;'>
                            Xem Đơn Ứng Tuyển
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
                        event.getJobTitle(),
                        event.getCandidateFullName(),
                        event.getCandidateEmail(),
                        matchScoreColor,
                        matchScoreDisplay,
                        matchScoreColor,
                        matchScoreLabel,
                        event.isHasResume()
                                ? "<p style='color:#27ae60;margin:10px 0;'><strong>✅ CV:</strong> Ứng viên đã đính kèm CV</p>"
                                : "<p style='color:#7f8c8d;margin:10px 0;'><strong>📄 CV:</strong> Ứng viên chưa đính kèm CV</p>",
                        event.getCoverLetter() != null && !event.getCoverLetter().trim().isEmpty()
                                ? "<div style='background:#e8f5e9;padding:15px;border-radius:8px;margin:20px 0;'><p style='color:#2e7d32;margin:0 0 10px 0;font-weight:bold;'>💬 Thư xin việc:</p><p style='color:#2e7d32;margin:0;white-space:pre-wrap;'>"
                                        + event.getCoverLetter() + "</p></div>"
                                : "",
                        event.getJobId());

        emailService.sendEmail(SendEmailRequest.builder()
                .to(Recipient.builder()
                        .email(event.getEmployerEmail())
                        .build())
                .subject(emailSubject)
                .htmlContent(emailContent)
                .build());

        log.info("Application email sent successfully to employer: {}", event.getEmployerEmail());
    }

    @KafkaListener(topics = "application-status-updated-event", groupId = "notification-group-v2", containerFactory = "applicationStatusUpdatedEventKafkaListenerContainerFactory")
    public void handleApplicationStatusUpdatedEvent(ApplicationStatusUpdatedEvent event) {
        log.info("Received ApplicationStatusUpdatedEvent: applicationId={}, status={}, candidateEmail={}",
                event.getApplicationId(), event.getStatus(), event.getCandidateEmail());

        try {
            switch (event.getStatus()) {
                case "ACCEPTED" -> sendAcceptedEmailToCandidate(event);
                case "REJECTED" -> sendRejectedEmailToCandidate(event);
                case "CANCELLED" -> sendCancelledEmailToCandidate(event);
                default -> log.warn("Unknown application status: {}", event.getStatus());
            }
        } catch (Exception e) {
            log.error("Failed to send email for ApplicationStatusUpdatedEvent: applicationId={}, status={}",
                    event.getApplicationId(), event.getStatus(), e);
        }
    }

    private void sendAcceptedEmailToCandidate(ApplicationStatusUpdatedEvent event) {
        String emailSubject = "Chúc mừng! Đơn ứng tuyển của bạn đã được chấp nhận - " + event.getJobTitle();
        String emailContent = """
                <div style='font-family:Arial,sans-serif;max-width:600px;margin:auto;padding:20px;border:1px solid #eee;border-radius:10px;background:#ffffff;'>
                    <div style='text-align:center;margin-bottom:30px;'>
                        <h1 style='color:#27ae60;margin:0;'>🎉 Chúc Mừng!</h1>
                    </div>

                    <p style='color:#34495e;font-size:16px;line-height:1.6;'>Xin chào <strong>%s</strong>,</p>

                    <p style='color:#34495e;font-size:16px;line-height:1.6;'>
                        Chúng tôi rất vui mừng thông báo rằng đơn ứng tuyển của bạn đã được <strong style='color:#27ae60;'>chấp nhận</strong>!
                    </p>

                    <div style='background:#e8f5e9;padding:20px;border-radius:8px;margin:20px 0;border-left:4px solid #27ae60;'>
                        <h2 style='color:#2c3e50;margin-top:0;'>%s</h2>
                        <p style='color:#2e7d32;margin:5px 0;'><strong>Nhà tuyển dụng:</strong> %s</p>
                    </div>

                    <div style='background:#e3f2fd;padding:15px;border-radius:8px;margin:20px 0;'>
                        <p style='color:#1565c0;margin:0;font-weight:bold;'>📌 Bước tiếp theo:</p>
                        <p style='color:#1565c0;margin:5px 0 0 0;'>Nhà tuyển dụng sẽ liên hệ với bạn trong thời gian sớm nhất để trao đổi về công việc và các bước tiếp theo.</p>
                    </div>

                    <div style='text-align:center;margin:30px 0;'>
                        <a href='https://jobmate.fun/applications'
                           style='background:#27ae60;color:#ffffff;padding:12px 30px;text-decoration:none;border-radius:5px;display:inline-block;font-weight:bold;'>
                            Xem Chi Tiết
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
                        event.getEmployerFullName());

        emailService.sendEmail(SendEmailRequest.builder()
                .to(Recipient.builder()
                        .email(event.getCandidateEmail())
                        .build())
                .subject(emailSubject)
                .htmlContent(emailContent)
                .build());

        log.info("Accepted email sent successfully to candidate: {}", event.getCandidateEmail());
    }

    private void sendRejectedEmailToCandidate(ApplicationStatusUpdatedEvent event) {
        String emailSubject = "Thông báo về đơn ứng tuyển - " + event.getJobTitle();
        String emailContent = """
                <div style='font-family:Arial,sans-serif;max-width:600px;margin:auto;padding:20px;border:1px solid #eee;border-radius:10px;background:#ffffff;'>
                    <div style='text-align:center;margin-bottom:30px;'>
                        <h1 style='color:#e74c3c;margin:0;'>Thông Báo</h1>
                    </div>

                    <p style='color:#34495e;font-size:16px;line-height:1.6;'>Xin chào <strong>%s</strong>,</p>

                    <p style='color:#34495e;font-size:16px;line-height:1.6;'>
                        Chúng tôi rất tiếc phải thông báo rằng đơn ứng tuyển của bạn cho vị trí sau đã không được chấp nhận:
                    </p>

                    <div style='background:#ffebee;padding:20px;border-radius:8px;margin:20px 0;border-left:4px solid #e74c3c;'>
                        <h2 style='color:#2c3e50;margin-top:0;'>%s</h2>
                        <p style='color:#c62828;margin:5px 0;'><strong>Nhà tuyển dụng:</strong> %s</p>
                    </div>

                    %s

                    <div style='background:#fff3e0;padding:15px;border-radius:8px;margin:20px 0;'>
                        <p style='color:#e65100;margin:0;font-weight:bold;'>💡 Lời khuyên:</p>
                        <p style='color:#e65100;margin:5px 0 0 0;'>Đừng nản lòng! Hãy tiếp tục tìm kiếm và ứng tuyển các vị trí phù hợp khác. Cơ hội việc làm tốt đang chờ bạn!</p>
                    </div>

                    <div style='text-align:center;margin:30px 0;'>
                        <a href='https://jobmate.fun/jobs'
                           style='background:#ff9800;color:#ffffff;padding:12px 30px;text-decoration:none;border-radius:5px;display:inline-block;font-weight:bold;'>
                            Tìm Việc Khác
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
                        event.getReason() != null && !event.getReason().trim().isEmpty()
                                ? "<div style='background:#fff3cd;padding:15px;border-radius:8px;margin:20px 0;'><p style='color:#856404;margin:0 0 10px 0;font-weight:bold;'>📝 Lý do:</p><p style='color:#856404;margin:0;'>"
                                        + event.getReason() + "</p></div>"
                                : "");

        emailService.sendEmail(SendEmailRequest.builder()
                .to(Recipient.builder()
                        .email(event.getCandidateEmail())
                        .build())
                .subject(emailSubject)
                .htmlContent(emailContent)
                .build());

        log.info("Rejected email sent successfully to candidate: {}", event.getCandidateEmail());
    }

    private void sendCancelledEmailToCandidate(ApplicationStatusUpdatedEvent event) {
        String emailSubject = "Đơn ứng tuyển đã bị hủy - " + event.getJobTitle();
        String emailContent = """
                <div style='font-family:Arial,sans-serif;max-width:600px;margin:auto;padding:20px;border:1px solid #eee;border-radius:10px;background:#ffffff;'>
                    <div style='text-align:center;margin-bottom:30px;'>
                        <h1 style='color:#f39c12;margin:0;'>Thông Báo</h1>
                    </div>

                    <p style='color:#34495e;font-size:16px;line-height:1.6;'>Xin chào <strong>%s</strong>,</p>

                    <p style='color:#34495e;font-size:16px;line-height:1.6;'>
                        Chúng tôi xin thông báo rằng đơn ứng tuyển của bạn cho vị trí sau đã bị hủy bởi nhà tuyển dụng:
                    </p>

                    <div style='background:#fff3cd;padding:20px;border-radius:8px;margin:20px 0;border-left:4px solid #f39c12;'>
                        <h2 style='color:#2c3e50;margin-top:0;'>%s</h2>
                        <p style='color:#856404;margin:5px 0;'><strong>Nhà tuyển dụng:</strong> %s</p>
                    </div>

                    <div style='background:#fff3e0;padding:15px;border-radius:8px;margin:20px 0;'>
                        <p style='color:#e65100;margin:0;font-weight:bold;'>💡 Lời khuyên:</p>
                        <p style='color:#e65100;margin:5px 0 0 0;'>Hãy tiếp tục tìm kiếm các cơ hội việc làm khác phù hợp với bạn.</p>
                    </div>

                    <div style='text-align:center;margin:30px 0;'>
                        <a href='https://jobmate.fun/jobs'
                           style='background:#f39c12;color:#ffffff;padding:12px 30px;text-decoration:none;border-radius:5px;display:inline-block;font-weight:bold;'>
                            Tìm Việc Khác
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
                        event.getEmployerFullName());

        emailService.sendEmail(SendEmailRequest.builder()
                .to(Recipient.builder()
                        .email(event.getCandidateEmail())
                        .build())
                .subject(emailSubject)
                .htmlContent(emailContent)
                .build());

        log.info("Cancelled email sent successfully to candidate: {}", event.getCandidateEmail());
    }
}
