package com.nals.rw360.repository;

import com.nals.rw360.domain.Assessment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentRepository
    extends JpaRepository<Assessment, Long> {

    boolean existsByName(String name);

    Optional<Assessment> findOneById(Long groupId);

    List<Assessment> findAllById(Long groupId);

    @Query("SELECT a FROM Assessment a WHERE :keyword IS NULL OR a.name LIKE %:keyword%")
    Page<Assessment> findByName(@Param("keyword") String keyword, Pageable pageable);
}
