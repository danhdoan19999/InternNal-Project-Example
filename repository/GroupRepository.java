package com.nals.rw360.repository;

import com.nals.rw360.domain.Group;
import com.nals.rw360.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface GroupRepository
    extends JpaRepository<Group, Long> {

    boolean existsByNameAndIdIsNot(String name, Long id);

    @Query("SELECT DISTINCT g"
        + " FROM Group g"
        + " INNER JOIN UserSubGroup usg ON g.id = usg.groupId"
        + " WHERE usg.userId = :userId AND (:keyword IS NULL OR g.name LIKE %:keyword%)")
    Page<Group> findGroupByUserIdAndName(@Param("userId") Long userId,
                                         @Param("keyword") String keyword,
                                         Pageable pageable);

    @Query("SELECT g FROM Group g WHERE :keyword IS NULL OR g.name LIKE %:keyword%")
    Page<Group> findGroupByName(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByName(String name);

    Optional<Group> findOneById(Long groupId);

    List<Group> findAllById(Long groupId);

    @Query("SELECT u"
        + " FROM User u"
        + " INNER JOIN UserRole ur ON u.id = ur.userId"
        + " INNER JOIN Role r ON ur.roleId = r.id"
        + " WHERE r.name IN (:roles)")
    List<User> fetchUsersByRole(@Param("roles") Set<String> roles);

    @Query("SELECT DISTINCT g"
        + " FROM Group g"
        + " INNER JOIN UserSubGroup usg ON usg.groupId = g.id"
        + " WHERE usg.userId = :userId AND g.id = :id")
    Optional<Group> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT g.managerId"
        + " FROM Group g"
        + " WHERE g.id = :id")
    Optional<Long> findManagerIdById(@Param("id") Long id);
}
