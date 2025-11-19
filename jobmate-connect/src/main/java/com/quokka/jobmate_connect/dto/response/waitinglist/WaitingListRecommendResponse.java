package com.quokka.jobmate_connect.dto.response.waitinglist;

import com.quokka.jobmate_connect.constant.SalaryUnitType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WaitingListRecommendResponse {

    String waitingListId;
    String userId;

    String fullName;
    String skills;

    Double expectedMinSalary;
    SalaryUnitType expectedSalaryUnit;

    Double distance;
    Integer radius;

    String availableDays;
    String availableTime;

    Double score;
}
