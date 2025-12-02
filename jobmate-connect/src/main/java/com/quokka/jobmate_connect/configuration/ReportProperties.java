package com.quokka.jobmate_connect.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "report")
@Data
public class ReportProperties {

    private Job job = new Job();
    private Employer employer = new Employer();
    private Map<String, List<String>> badKeywords = new HashMap<>();

    @Data
    public static class Job {
        private int threshold = 3;
        private int windowDays = 7;
    }

    @Data
    public static class Employer {
        private int violationLimit = 5;
    }
}
