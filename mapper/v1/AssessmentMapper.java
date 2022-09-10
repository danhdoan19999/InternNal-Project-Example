package com.nals.rw360.mapper.v1;

import com.nals.rw360.domain.Assessment;
import com.nals.rw360.dto.v1.request.assessment.AssessmentCreateReq;
import com.nals.rw360.dto.v1.response.assessment.AssessmentRes;
import com.nals.rw360.helpers.DateHelper;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.time.Instant;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AssessmentMapper {

    AssessmentMapper INSTANCE = Mappers.getMapper(AssessmentMapper.class);

    Assessment toAssessment(AssessmentCreateReq assessmentCreateReq);

    default Instant toInstant(Long millis) {
        return DateHelper.toInstant(millis);
    }

    AssessmentRes toAssessmentRes(Assessment assessment);

    default Long fromInstant(Instant instant) {
        return DateHelper.toMillis(instant);
    }
}
