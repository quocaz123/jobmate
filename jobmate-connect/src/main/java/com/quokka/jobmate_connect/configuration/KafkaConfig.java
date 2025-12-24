package com.quokka.jobmate_connect.configuration;

import com.quokka.jobmate_connect.kafka.dto.ApplicationCreatedEvent;
import com.quokka.jobmate_connect.kafka.dto.ApplicationStatusUpdatedEvent;
import com.quokka.jobmate_connect.kafka.dto.JobInvitationEvent;
import com.quokka.jobmate_connect.kafka.dto.SendOtpEvent;
import com.quokka.jobmate_connect.kafka.dto.UserStatusChangeEvent;
import com.quokka.jobmate_connect.kafka.dto.VerificationRequestEvent;
import com.quokka.jobmate_connect.kafka.dto.VerificationResultEvent;
import lombok.experimental.NonFinal;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @NonFinal
    @Value("${spring.kafka.bootstrap-servers}")
    protected String bootstrapServers;

    private Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return props;
    }

    @Bean
    public ProducerFactory<String, SendOtpEvent> sendOtpEventProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, SendOtpEvent> sendOtpEventKafkaTemplate() {
        return new KafkaTemplate<>(sendOtpEventProducerFactory());
    }

    @Bean
    public ProducerFactory<String, VerificationRequestEvent> verificationRequestEventProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, VerificationRequestEvent> verificationRequestEventKafkaTemplate() {
        return new KafkaTemplate<>(verificationRequestEventProducerFactory());
    }

    @Bean
    public ProducerFactory<String, VerificationResultEvent> verificationResultEventProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, VerificationResultEvent> verificationResultEventKafkaTemplate() {
        return new KafkaTemplate<>(verificationResultEventProducerFactory());
    }

    @Bean
    public ProducerFactory<String, UserStatusChangeEvent> userStatusChangeEventProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, UserStatusChangeEvent> userStatusChangeEventKafkaTemplate() {
        return new KafkaTemplate<>(userStatusChangeEventProducerFactory());
    }

    @Bean
    public ProducerFactory<String, JobInvitationEvent> jobInvitationEventProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, JobInvitationEvent> jobInvitationEventKafkaTemplate() {
        return new KafkaTemplate<>(jobInvitationEventProducerFactory());
    }

    @Bean
    public ProducerFactory<String, ApplicationCreatedEvent> applicationCreatedEventProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, ApplicationCreatedEvent> applicationCreatedEventKafkaTemplate() {
        return new KafkaTemplate<>(applicationCreatedEventProducerFactory());
    }

    @Bean
    public ProducerFactory<String, ApplicationStatusUpdatedEvent> applicationStatusUpdatedEventProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, ApplicationStatusUpdatedEvent> applicationStatusUpdatedEventKafkaTemplate() {
        return new KafkaTemplate<>(applicationStatusUpdatedEventProducerFactory());
    }

}
