package com.quokka.jobmate_connect.repository.ESRepository;

import com.quokka.jobmate_connect.entity.eslasticsearch.WaitingRequestES;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WaitingRequestESRepository extends ElasticsearchRepository<WaitingRequestES, String> {
}
