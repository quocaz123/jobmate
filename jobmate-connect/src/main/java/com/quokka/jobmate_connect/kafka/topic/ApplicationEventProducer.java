package com.quokka.jobmate_connect.kafka.topic;

import com.quokka.jobmate_connect.kafka.dto.ApplicationCreatedEvent;
import com.quokka.jobmate_connect.kafka.dto.ApplicationStatusUpdatedEvent;
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
public class ApplicationEventProducer {
    KafkaTemplate<String, ApplicationCreatedEvent> applicationCreatedKafkaTemplate;
    KafkaTemplate<String, ApplicationStatusUpdatedEvent> applicationStatusUpdatedKafkaTemplate;

    static final String CREATED_TOPIC = "application-created-event";
    static final String STATUS_UPDATED_TOPIC = "application-status-updated-event";

    public void publishApplicationCreatedEvent(ApplicationCreatedEvent event) {
        log.info("Publishing ApplicationCreatedEvent: applicationId={}, candidateEmail={}, jobTitle={}",
                event.getApplicationId(), event.getCandidateEmail(), event.getJobTitle());
        try {
            applicationCreatedKafkaTemplate.send(CREATED_TOPIC, event);
            log.info("ApplicationCreatedEvent published successfully");
        } catch (Exception e) {
            log.error("Failed to publish ApplicationCreatedEvent", e);
        }
    }

    public void publishApplicationStatusUpdatedEvent(ApplicationStatusUpdatedEvent event) {
        log.info("Publishing ApplicationStatusUpdatedEvent: applicationId={}, status={}, candidateEmail={}",
                event.getApplicationId(), event.getStatus(), event.getCandidateEmail());
        try {
            applicationStatusUpdatedKafkaTemplate.send(STATUS_UPDATED_TOPIC, event);
            log.info("ApplicationStatusUpdatedEvent published successfully");
        } catch (Exception e) {
            log.error("Failed to publish ApplicationStatusUpdatedEvent", e);
        }
    }
}
