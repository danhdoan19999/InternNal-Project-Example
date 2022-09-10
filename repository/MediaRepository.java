package com.nals.rw360.repository;

import com.nals.rw360.domain.Media;
import com.nals.rw360.enums.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MediaRepository
    extends JpaRepository<Media, Long> {

    Optional<Media> findBySourceIdAndType(Long sourceId, MediaType mediaType);

    void deleteBySourceIdAndType(Long sourceId, MediaType mediaType);
}
