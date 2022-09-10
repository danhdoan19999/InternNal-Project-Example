package com.nals.rw360.repository;

import com.nals.rw360.domain.GroupType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupTypeRepository
    extends JpaRepository<GroupType, Long> {
}
