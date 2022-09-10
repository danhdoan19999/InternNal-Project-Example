package com.nals.rw360.domain;

import com.nals.rw360.enums.Status;
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
@Table(name = "rw_sub_groups")
public class SubGroup
    extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 55)
    private String name;

    @Column(length = 5000)
    private String description;

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "image_name", length = 500)
    private String imageName;

    @Enumerated(STRING)
    @Column(length = 20)
    private Status status;

    @Column(name = "group_type_id")
    private Long groupTypeId;

    @Column(name = "manager_id")
    private Long managerId;
}
