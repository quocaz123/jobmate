package com.quokka.jobmate_connect.repository.httpClient;

import com.quokka.jobmate_connect.dto.request.user.ExchangeTokenRequest;
import com.quokka.jobmate_connect.dto.response.user.ExchangeTokenRespone;
import feign.QueryMap;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "outboundClient", url = "https://oauth2.googleapis.com")
public interface OutboundClient {

    @PostMapping(value = "/token", produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ExchangeTokenRespone exchangeToken(@QueryMap ExchangeTokenRequest request);
}
