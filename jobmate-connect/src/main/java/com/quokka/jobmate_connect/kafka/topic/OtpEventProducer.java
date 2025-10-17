package com.quokka.jobmate_connect.kafka.topic;

import com.quokka.jobmate_connect.kafka.dto.SendOtpEvent;
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
public class OtpEventProducer {
    KafkaTemplate<String, SendOtpEvent> kafkaTemplate;
    static final String SEND_OTP = "send-otp";

    public void sendOtpEvent(SendOtpEvent event) {
        log.info("Producing SendOtpEvent for email: {}", event.getEmail());
        kafkaTemplate.send(SEND_OTP, event);
    }
}
