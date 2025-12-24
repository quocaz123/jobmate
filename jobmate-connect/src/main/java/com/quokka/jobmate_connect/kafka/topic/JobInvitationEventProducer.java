package com.quokka.jobmate_connect.kafka.topic;

import com.quokka.jobmate_connect.kafka.dto.JobInvitationEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JobInvitationEventProducer {
    KafkaTemplate<String, JobInvitationEvent> kafkaTemplate;
    static final String TOPIC = "job-invitation-event";

    public void publishInvitationEvent(JobInvitationEvent event) {
        log.info("Publishing JobInvitationEvent: type={}, invitationId={}, candidateEmail={}",
                event.getEventType(), event.getInvitationId(), event.getCandidateEmail());
        try {
            kafkaTemplate.send(TOPIC, event);
            log.info("JobInvitationEvent published successfully");
        } catch (Exception e) {
            log.error("Failed to publish JobInvitationEvent", e);
        }
    }
}
