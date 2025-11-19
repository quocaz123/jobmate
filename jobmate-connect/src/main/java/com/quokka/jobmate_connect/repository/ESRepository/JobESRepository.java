package com.quokka.jobmate_connect.repository.ESRepository;

import com.quokka.jobmate_connect.entity.eslasticsearch.JobES;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobESRepository extends ElasticsearchRepository<JobES, String> {
}
