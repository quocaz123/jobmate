package com.quokka.jobmate_connect.repository.httpClient;

import com.quokka.jobmate_connect.dto.response.user.OutboundResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "outboundUserClient", url = "https://www.googleapis.com")
public interface OutboundUserClient {
    @GetMapping("/oauth2/v2/userinfo")
    OutboundResponse getUserInfo(@RequestParam("alt") String alt,
                                 @RequestParam("access_token") String accessToken);
}
