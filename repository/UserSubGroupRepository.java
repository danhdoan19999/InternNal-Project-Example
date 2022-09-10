package com.nals.rw360.repository;

import com.nals.rw360.domain.UserSubGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserSubGroupRepository
    extends JpaRepository<UserSubGroup, Long> {

    boolean existsByGroupIdAndSubGroupIdAndUserId(Long groupId, Long subGroupId, Long userId);

    @Query("SELECT usg.userId"
        + " FROM UserSubGroup usg"
        + " WHERE usg.subGroupId = :subGroupId")
    Set<Long> findAllUserIdsBySubGroupId(@Param("subGroupId") Long subGroupId);

    boolean existsBySubGroupIdAndUserId(Long subGroupId, Long userId);

    Optional<UserSubGroup> findByUserIdAndSubGroupId(Long userId, Long subGroupId);

    List<UserSubGroup> findAllBySubGroupId(Long subGroupId);

    @Query("SELECT usg.userId"
        + " FROM UserSubGroup usg"
        + " WHERE usg.groupId = :groupId AND usg.subGroupId = :subGroupId")
    Set<Long> findAllUserIdByGroupIdAndSubGroupId(@Param("groupId") Long groupId,
                                                  @Param("subGroupId") Long subGroupId);

    @Query("SELECT usg.userId"
        + " FROM UserSubGroup usg"
        + " WHERE usg.groupId = :groupId"
        + " AND usg.subGroupId IN (SELECT us.subGroupId"
        + "                        FROM UserSubGroup us"
        + "                        WHERE us.userId = :userId)")
    Set<Long> findAllUserIdsByGroupIdAndCurrentUserId(@Param("groupId") Long groupId,
                                                      @Param("userId") Long userId);

    @Query("SELECT usg.userId"
        + " FROM UserSubGroup usg"
        + " WHERE usg.groupId = :groupId")
    Set<Long> findAllUserIdByGroupId(@Param("groupId") Long groupId);
}
