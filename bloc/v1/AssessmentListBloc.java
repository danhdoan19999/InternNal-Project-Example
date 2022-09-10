package com.nals.rw360.bloc.v1;

import com.nals.rw360.domain.Assessment;
import com.nals.rw360.domain.Form;
import com.nals.rw360.dto.v1.request.assessment.AssessmentSearchReq;
import com.nals.rw360.dto.v1.response.assessment.AssessmentRes;
import com.nals.rw360.helpers.PaginationHelper;
import com.nals.rw360.mapper.v1.AssessmentMapper;
import com.nals.rw360.service.v1.AssessmentService;
import com.nals.rw360.service.v1.FileService;
import com.nals.rw360.service.v1.FormService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssessmentListBloc {

    private final AssessmentService assessmentService;

    private final FileService fileService;

    private final FormService formService;

    public Page<AssessmentRes> searchAssessments(final AssessmentSearchReq req) {
        log.info("Search assessment by name");

        Pageable pageable = PaginationHelper.generatePageRequest(req);
        Page<Assessment> assessments;

        assessments = assessmentService.searchByName(req.getKeyword(), pageable);

        Set<Long> assessmentIds = assessments.stream().map(Assessment::getId).collect(Collectors.toSet());

        Map<Long, List<Form>> formsMap = formService.fetchFormsByAssessmentId(assessmentIds)
                                                    .stream()
                                                    .collect(Collectors.groupingBy(Form::getAssessmentId));
        Map<Long, List<Form>> formsUsedMap = formService.fetchFormsUsedByAssessmentIds(assessmentIds)
                                                        .stream()
                                                        .collect(Collectors.groupingBy(Form::getAssessmentId));

        return assessments.map(assessment -> {
            AssessmentRes assessmentRes = AssessmentMapper.INSTANCE.toAssessmentRes(assessment);
            assessmentRes.setImageUrl(fileService.getFullFileUrl(assessment.getImageName()));
            assessmentRes.setFormsNumber(Objects.isNull(formsMap.get(assessment.getId()))
                                             ? 0 : (long) formsMap.get(assessment.getId()).size());
            assessmentRes.setFormsUsedNumber(Objects.isNull(formsUsedMap.get(assessment.getId()))
                                                 ? 0 : (long) formsMap.get(assessment.getId()).size());
            return assessmentRes;
        });
    }
}
