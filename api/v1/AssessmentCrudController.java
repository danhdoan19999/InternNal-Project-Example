package com.nals.rw360.api.v1;

import com.nals.rw360.bloc.v1.AssessmentCrudBloc;
import com.nals.rw360.dto.v1.request.assessment.AssessmentCreateReq;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.Validator;

@RestController
@RequestMapping("/api/v1/assessments")
public class AssessmentCrudController
    extends BaseController {

    private final AssessmentCrudBloc assessmentCrudBloc;

    public AssessmentCrudController(final Validator validator, final AssessmentCrudBloc assessmentCrudBloc) {
        super(validator);
        this.assessmentCrudBloc = assessmentCrudBloc;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')")
    public ResponseEntity<?> createAssessment(@Valid @RequestBody final AssessmentCreateReq req) {
        return created(assessmentCrudBloc.createAssessment(req));
    }
}
