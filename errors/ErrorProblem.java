package com.nals.rw360.errors;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ErrorProblem {
    private String field;
    private String message;
    private String errorCode;
    private Integer limitTryNumber;
    private Integer remainRetryNumber;
    private Long unlockTime;
}
