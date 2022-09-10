package com.nals.rw360.service.v1;

import com.nals.rw360.domain.Media;
import com.nals.rw360.enums.MediaType;
import com.nals.rw360.repository.MediaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class MediaService
    extends BaseService<Media, MediaRepository> {

    public MediaService(final MediaRepository repository) {
        super(repository);
    }

    public Optional<Media> getBySourceIdAndMediaType(final Long sourceId, final MediaType mediaType) {
        log.info("Get media by source id #{} and type #{}", sourceId, mediaType);
        return getRepository().findBySourceIdAndType(sourceId, mediaType);
    }

    @Transactional
    public void deleteBySourceIdAndType(final Long sourceId, final MediaType mediaType) {
        log.info("Delete by source id #{} and type #{}", sourceId, mediaType);
        getRepository().deleteBySourceIdAndType(sourceId, mediaType);
    }
}
