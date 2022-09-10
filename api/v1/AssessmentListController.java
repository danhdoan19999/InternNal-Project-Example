package com.nals.rw360.api.v1;

import com.nals.rw360.bloc.v1.AssessmentListBloc;
import com.nals.rw360.dto.v1.request.assessment.AssessmentSearchReq;
import com.nals.rw360.helpers.JsonHelper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Validator;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/assessments")
public class AssessmentListController
    extends BaseController {

    private final AssessmentListBloc assessmentListBloc;

    public AssessmentListController(final Validator validator, final AssessmentListBloc assessmentListBloc) {
        super(validator);
        this.assessmentListBloc = assessmentListBloc;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_ACCOUNT_LEADER')")
    public ResponseEntity<?> searchAssessments(@RequestParam final Map<String, Object> reqParams) {
        AssessmentSearchReq req = JsonHelper.MAPPER.convertValue(reqParams, AssessmentSearchReq.class);
        return ok(assessmentListBloc.searchAssessments(req));
    }
}
