package com.nals.rw360.repository;

import com.nals.rw360.domain.UserRole;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRoleRepository
    extends JpaRepository<UserRole, Long> {

    void deleteByUserId(Long userId);

    Set<UserRole> findAllByUserId(Long userId);

    Optional<UserRole> findUserRoleByUserId(Long userId);

    @Query("SELECT COUNT (ur.id) > 0"
        + " FROM UserRole ur"
        + " INNER JOIN Role r  ON r.id = ur.roleId"
        + " WHERE r.name = :roleName AND ur.userId = :userId")
    boolean existsByRoleNameAndUserId(@Param("roleName") String roleName, @Param("userId") Long userId);
}
