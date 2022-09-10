package com.nals.rw360.service.v1;

import com.nals.rw360.domain.Form;
import com.nals.rw360.repository.FormRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class FormService
    extends BaseService<Form, FormRepository> {

    public FormService(final FormRepository repository) {
        super(repository);
    }

    public List<Form> fetchFormsByAssessmentId(final Collection<Long> assessmentIds) {
        log.info("Fetch all forms in assessment by list assessment id: #{}", assessmentIds);
        return getRepository().findFormsByAssessmentIds(assessmentIds);
    }

    public List<Form> fetchFormsUsedByAssessmentIds(final Collection<Long> assessmentIds) {
        log.info("Fetch all forms whose used in assessment by list assessment id: #{}", assessmentIds);
        return getRepository().findFormsUsedByAssessmentIds(assessmentIds);
    }
}
