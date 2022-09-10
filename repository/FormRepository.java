package com.nals.rw360.repository;

import com.nals.rw360.domain.Form;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface FormRepository
    extends JpaRepository<Form, Long> {

    @Query("SELECT new Form(f.id, f.assessmentId)"
        + " FROM Form f"
        + " WHERE f.assessmentId IN :assessmentIds")
    List<Form> findFormsByAssessmentIds(Collection<Long> assessmentIds);

    @Query("SELECT DISTINCT new Form(f.id, f.assessmentId)"
        + " FROM Form f"
        + " JOIN FormUser fu ON f.id = fu.formId"
        + " WHERE f.assessmentId IN :assessmentIds")
    List<Form> findFormsUsedByAssessmentIds(Collection<Long> assessmentIds);
}
