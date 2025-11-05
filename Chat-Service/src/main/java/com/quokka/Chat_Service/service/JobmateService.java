package com.quokka.Chat_Service.service;

import com.quokka.Chat_Service.dto.request.IntrospectRequest;
import com.quokka.Chat_Service.dto.response.IntrospectResponse;
import com.quokka.Chat_Service.repository.httpClient.ProfileClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class JobmateService {
    ProfileClient profileClient;

    public IntrospectResponse introspect(IntrospectRequest request) {
        try {
            var result = profileClient.introspect(request).getData();
            if(Objects.isNull(result)) {
                return IntrospectResponse.builder()
                        .valid(false)
                        .build();
            }
            return result;
        } catch (FeignException e) {
            return IntrospectResponse.builder()
                    .valid(false)
                    .build();
        }
    }
}
