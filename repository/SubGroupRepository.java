package com.nals.rw360.repository;

import com.nals.rw360.domain.SubGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubGroupRepository
    extends JpaRepository<SubGroup, Long> {

    List<SubGroup> findAllByGroupId(Long groupId);

    boolean existsByName(String name);

    boolean existsByGroupId(Long groupId);

    Optional<SubGroup> findOneById(Long subGroupId);

    boolean existsById(Long id);

    @Query("SELECT DISTINCT sg"
        + " FROM SubGroup sg"
        + " INNER JOIN UserSubGroup usg ON usg.subGroupId = sg.id"
        + " WHERE usg.userId = :userId AND sg.id = :id")
    Optional<SubGroup> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT sg"
        + " FROM SubGroup sg"
        + " WHERE sg.groupId = :groupId AND (:keyword IS NULL OR sg.name LIKE %:keyword%)")
    Page<SubGroup> findAllByGroupIdAndName(@Param("groupId") Long groupId,
                                           @Param("keyword") String keyword,
                                           Pageable pageable);

    @Query("SELECT sg"
        + " FROM SubGroup sg"
        + " INNER JOIN UserSubGroup usg ON usg.subGroupId = sg.id"
        + " WHERE sg.groupId = :groupId AND usg.userId = :userId AND (:keyword IS NULL OR sg.name LIKE %:keyword%)")
    Page<SubGroup> findAllByGroupIdAndUserIdAndName(@Param("groupId") Long groupId,
                                                    @Param("userId") Long userId,
                                                    @Param("keyword") String keyword,
                                                    Pageable pageable);

    @Query("SELECT sg.groupId"
        + " FROM SubGroup sg"
        + " WHERE sg.id = :id")
    Optional<Long> findGroupIdById(@Param("id") Long id);

    boolean existsByNameAndIdIsNot(String name, Long id);

    boolean existsByIdAndManagerId(Long managerId, Long id);
}
