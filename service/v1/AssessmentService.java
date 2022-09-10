package com.nals.rw360.service.v1;

import com.nals.rw360.domain.Assessment;
import com.nals.rw360.repository.AssessmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class AssessmentService
    extends BaseService<Assessment, AssessmentRepository> {

    public AssessmentService(final AssessmentRepository repository) {
        super(repository);
    }

    public boolean existsByName(final String name) {
        log.info("Check exists assessment by name #{}", name);
        return getRepository().existsByName(name);
    }

    public Page<Assessment> searchByName(final String keyword, final Pageable pageable) {
        log.info("Search assessment by name #{}", keyword);
        return getRepository().findByName(keyword, pageable);
    }
}
