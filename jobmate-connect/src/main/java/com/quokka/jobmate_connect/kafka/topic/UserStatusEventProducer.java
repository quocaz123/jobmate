package com.quokka.jobmate_connect.kafka.topic;

import com.quokka.jobmate_connect.kafka.dto.UserStatusChangeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserStatusEventProducer {

    private final KafkaTemplate<String, UserStatusChangeEvent> kafkaTemplate;

    public void sendUserStatusChangeEvent(UserStatusChangeEvent event) {
        try {
            kafkaTemplate.send("user-status-change", event);
            log.info("Sent UserStatusChangeEvent for userId: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to send UserStatusChangeEvent for userId: {}", event.getUserId(), e);
        }
    }
}
