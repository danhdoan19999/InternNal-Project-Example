package com.nals.rw360.bloc.v1;

import com.nals.rw360.dto.v1.request.assessment.AssessmentCreateReq;
import com.nals.rw360.errors.ValidatorException;
import com.nals.rw360.helpers.DateHelper;
import com.nals.rw360.helpers.StringHelper;
import com.nals.rw360.mapper.v1.AssessmentMapper;
import com.nals.rw360.service.v1.AssessmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

import static com.nals.rw360.enums.MediaType.ASSESSMENT_IMAGE;
import static com.nals.rw360.errors.ErrorCodes.ASSESSMENT_NAME_ALREADY_USED;
import static com.nals.rw360.errors.ErrorCodes.INVALID_END_DATE;
import static com.nals.rw360.errors.ErrorCodes.INVALID_START_DATE;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssessmentCrudBloc {

    private final AssessmentService assessmentService;

    private final MediaCrudBloc mediaCrudBloc;

    @Transactional
    public Long createAssessment(final AssessmentCreateReq req) {
        log.info("Create assessment with data #{}", req);

        req.setStartDate(DateHelper.toLongWithTimeAtStartOfDay(DateHelper.toInstant(req.getStartDate())));
        req.setEndDate(DateHelper.toMillis(DateHelper.toInstantWithTimeAtEndOfDay(req.getEndDate())));

        validateCreateAssessment(req);

        Long assessmentId = assessmentService.save(AssessmentMapper.INSTANCE.toAssessment(req)).getId();

        if (StringHelper.isNotBlank(req.getImageName())) {
            mediaCrudBloc.saveMedia(req.getImageName(), assessmentId, ASSESSMENT_IMAGE);
        }

        return assessmentId;
    }

    private void validateCreateAssessment(final AssessmentCreateReq req) {
        Instant currentDate = DateHelper.toInstant(DateHelper.toLongWithTimeAtStartOfDay(Instant.now()));

        if (assessmentService.existsByName(req.getName())) {
            throw new ValidatorException("Assessment name already used", "name", ASSESSMENT_NAME_ALREADY_USED);
        }

        if (currentDate.isAfter(Objects.requireNonNull(DateHelper.toInstant(req.getStartDate())))) {
            throw new ValidatorException("Start date must be greater than current date",
                                         "date", INVALID_START_DATE);
        }

        if (req.getStartDate().compareTo(req.getEndDate()) > 0) {
            throw new ValidatorException("End date must be greater than start date", "end_date", INVALID_END_DATE);
        }
    }
}
