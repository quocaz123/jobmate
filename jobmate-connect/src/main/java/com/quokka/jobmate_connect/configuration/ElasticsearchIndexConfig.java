package com.quokka.jobmate_connect.configuration;

import com.quokka.jobmate_connect.entity.eslasticsearch.JobES;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchIndexConfig {
    private final ElasticsearchTemplate elasticsearchTemplate;

    @PostConstruct
    public void init() {
        IndexOperations indexOps = elasticsearchTemplate.indexOps(JobES.class);

        if (!indexOps.exists()) {
            log.info("=== Creating jobs_index ===");

            // Tạo index từ JobES + mapping
            indexOps.create();
            indexOps.putMapping(indexOps.createMapping());

            log.info("=== jobs_index created successfully ===");
        } else {
            log.info("=== jobs_index already exists ===");
        }
    }
}
