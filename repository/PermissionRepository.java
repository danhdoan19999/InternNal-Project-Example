package com.nals.rw360.repository;

import com.nals.rw360.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface PermissionRepository
    extends JpaRepository<Permission, Long> {

    Optional<Permission> findByName(String name);

    @Query("SELECT DISTINCT p"
        + " FROM Permission p"
        + " JOIN RolePermission rp ON rp.permissionId = p.id"
        + " JOIN UserRole ur ON ur.roleId = rp.roleId"
        + " WHERE ur.userId = :userId")
    Set<Permission> fetchByUserId(@Param("userId") Long userId);
}
