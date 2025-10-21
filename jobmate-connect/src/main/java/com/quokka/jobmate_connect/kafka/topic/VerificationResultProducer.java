package com.quokka.jobmate_connect.kafka.topic;

import com.quokka.jobmate_connect.kafka.dto.VerificationResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationResultProducer {

    private final KafkaTemplate<String, VerificationResultEvent> kafkaTemplate;

    public void sendVerificationResultEvent(VerificationResultEvent event) {
        try {
            kafkaTemplate.send("verification-result", event);
            log.info("Sent VerificationResultEvent for userId: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to send VerificationResultEvent for userId: {}", event.getUserId(), e);
        }
    }
}
