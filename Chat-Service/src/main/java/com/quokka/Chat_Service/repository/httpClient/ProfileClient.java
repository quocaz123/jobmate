package com.quokka.Chat_Service.repository.httpClient;

import com.quokka.Chat_Service.dto.ApiResponse;
import com.quokka.Chat_Service.dto.response.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "jobmate-connect", url = "${feign.client.config.jobmate-connect.url}")
public interface ProfileClient {
    @GetMapping("/internal/users/{userId}")
    ApiResponse<UserProfileResponse> getProfile(@PathVariable("userId") UUID userId);
}
