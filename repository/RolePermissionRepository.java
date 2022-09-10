package com.nals.rw360.repository;

import com.nals.rw360.domain.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolePermissionRepository
    extends JpaRepository<RolePermission, Long> {
}
