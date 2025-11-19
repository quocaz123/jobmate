package com.quokka.jobmate_connect.controller;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EsConnectionTest {
    private final ElasticsearchClient client;

    @PostConstruct
    public void test() throws Exception {
        var info = client.info();
        log.info("Connected to ES: " + info.version().number());
    }
}
