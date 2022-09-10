package com.nals.rw360.domain;

import com.nals.rw360.enums.FormType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rw_forms")
public class Form
    extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 55)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "assessment_id")
    private Long assessmentId;

    @Enumerated(STRING)
    @Column(name = "form_type", length = 20)
    private FormType formType;

    @Column(name = "form_template_id")
    private Long formTemplateId;

    public Form(final Long id, final Long assessmentId) {
        this.id = id;
        this.assessmentId = assessmentId;
    }
}
