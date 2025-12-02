package com.quokka.jobmate_connect.entity.eslasticsearch;

import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Document(indexName = "jobs_index")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobES {

    @Id
    @Field(type = FieldType.Keyword)
    String id;

    @Field(type = FieldType.Keyword)
    String employerId;

    @Field(type = FieldType.Text)
    String title;

    @Field(type = FieldType.Text)
    String description;

    @Field(type = FieldType.Text)
    String skills;

    @Field(type = FieldType.Keyword)
    String jobType;

    @Field(type = FieldType.Double)
    Double salary;

    @Field(type = FieldType.Keyword)
    String salaryUnit;

    @Field(type = FieldType.Double)
    Double salaryPerHour;

    @GeoPointField
    GeoPoint location;


    @Field(type = FieldType.Text)
    String scheduleDays;

    @Field(type = FieldType.Text)
    String scheduleTime;

    @Field(type = FieldType.Keyword)
    String status;

    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt;

}
