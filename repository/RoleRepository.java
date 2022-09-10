package com.nals.rw360.repository;

import com.nals.rw360.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository
    extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    @Query("SELECT r"
        + " FROM Role r JOIN UserRole ur ON ur.roleId = r.id"
        + " WHERE ur.userId = :userId")
    Set<Role> fetchByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(r.id) > 0"
        + " FROM Role r"
        + " INNER JOIN UserRole ur ON ur.roleId = r.id"
        + " WHERE ur.userId = :userId AND r.name IN (:roles)")
    boolean existsByUserIdAndNames(@Param("userId") Long userId, @Param("roles") Set<String> roles);

    Set<Role> findAllByNameIn(Collection<String> names);

    boolean existsByNameAndId(String role, Long id);

    @Query("SELECT DISTINCT new Role (r.id, r.name, ur.userId)"
        + " FROM Role r"
        + " JOIN UserRole ur ON ur.roleId = r.id"
        + " WHERE ur.userId IN :userIds")
    List<Role> fetchByUserIds(@Param("userIds") Collection<Long> userIds);
}
