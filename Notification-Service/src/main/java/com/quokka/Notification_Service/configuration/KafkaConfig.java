package com.quokka.Notification_Service.configuration;

import com.quokka.Notification_Service.kafka.dto.ApplicationCreatedEvent;
import com.quokka.Notification_Service.kafka.dto.ApplicationStatusUpdatedEvent;
import com.quokka.Notification_Service.kafka.dto.JobInvitationEvent;
import com.quokka.Notification_Service.kafka.dto.SendOtpEvent;
import com.quokka.Notification_Service.kafka.dto.UserStatusChangeEvent;
import com.quokka.Notification_Service.kafka.dto.VerificationRequestEvent;
import com.quokka.Notification_Service.kafka.dto.VerificationResultEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    private Map<String, Object> baseConsumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        return props;
    }

    @Bean
    public ConsumerFactory<String, SendOtpEvent> sendOtpEventConsumerFactory() {
        Map<String, Object> props = baseConsumerConfigs();
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, SendOtpEvent.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SendOtpEvent> sendOtpEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, SendOtpEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(sendOtpEventConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, VerificationRequestEvent> verificationRequestEventConsumerFactory() {
        Map<String, Object> props = baseConsumerConfigs();
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, VerificationRequestEvent.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, VerificationRequestEvent> verificationRequestEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, VerificationRequestEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(verificationRequestEventConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, VerificationResultEvent> verificationResultEventConsumerFactory() {
        Map<String, Object> props = baseConsumerConfigs();
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, VerificationResultEvent.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, VerificationResultEvent> verificationResultEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, VerificationResultEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(verificationResultEventConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, UserStatusChangeEvent> userStatusChangeEventConsumerFactory() {
        Map<String, Object> props = baseConsumerConfigs();
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, UserStatusChangeEvent.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserStatusChangeEvent> userStatusChangeEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UserStatusChangeEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(userStatusChangeEventConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, JobInvitationEvent> jobInvitationEventConsumerFactory() {
        Map<String, Object> props = baseConsumerConfigs();
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, JobInvitationEvent.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, JobInvitationEvent> jobInvitationEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, JobInvitationEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(jobInvitationEventConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, ApplicationCreatedEvent> applicationCreatedEventConsumerFactory() {
        Map<String, Object> props = baseConsumerConfigs();
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ApplicationCreatedEvent.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ApplicationCreatedEvent> applicationCreatedEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ApplicationCreatedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(applicationCreatedEventConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, ApplicationStatusUpdatedEvent> applicationStatusUpdatedEventConsumerFactory() {
        Map<String, Object> props = baseConsumerConfigs();
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ApplicationStatusUpdatedEvent.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ApplicationStatusUpdatedEvent> applicationStatusUpdatedEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ApplicationStatusUpdatedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(applicationStatusUpdatedEventConsumerFactory());
        return factory;
    }
}
