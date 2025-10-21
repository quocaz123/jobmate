package com.quokka.jobmate_connect.kafka.topic;

import com.quokka.jobmate_connect.kafka.dto.VerificationRequestEvent;
import com.quokka.jobmate_connect.kafka.dto.VerificationResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationEventProducer {
    private final KafkaTemplate<String, VerificationRequestEvent> kafkaTemplate;

    public void sendVerificationRequestEvent(VerificationRequestEvent event) {
        try {
            kafkaTemplate.send("verification-request", event);
            log.info("Sent VerificationRequestEvent for userId: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to send VerificationRequestEvent for userId: {}", event.getUserId(), e);
        }
    }


}
