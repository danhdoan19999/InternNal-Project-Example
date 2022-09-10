package com.nals.rw360.bloc.v1;

import com.nals.rw360.domain.Media;
import com.nals.rw360.dto.v1.response.MediaRes;
import com.nals.rw360.enums.MediaType;
import com.nals.rw360.errors.ObjectNotFoundException;
import com.nals.rw360.helpers.StringHelper;
import com.nals.rw360.service.v1.FileService;
import com.nals.rw360.service.v1.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

import static com.nals.rw360.errors.ErrorCodes.MEDIA_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MediaCrudBloc {

    private final FileService fileService;
    private final MediaService mediaService;

    @Transactional
    public MediaRes uploadMedia(final MultipartFile file)
        throws Exception {
        log.info("Upload media");

        fileService.validateFile(file);

        String storeKey = fileService.uploadFile(file.getInputStream(), file.getOriginalFilename());

        return MediaRes.builder()
                       .storedKey(storeKey)
                       .imageUrl(fileService.getFullFileTempUrl(storeKey))
                       .build();
    }

    @Transactional
    public void saveMedia(final String fileName, final Long sourceId, final MediaType mediaType) {
        log.info("Save media with fileName #{}, sourceId #{} and type #{}", fileName, sourceId, mediaType);
        if (StringHelper.isBlank(fileName)) {
            return;
        }

        mediaService.save(Media.builder()
                               .sourceId(sourceId)
                               .storedKey(fileName)
                               .type(mediaType)
                               .build());

        fileService.saveFile(fileName);
    }

    @Transactional
    public void replaceMedia(final String newFileName, final String oldFileName,
                             final Long sourceId, final MediaType mediaType) {
        log.info("Replace media with oldFileName #{}, newFileName #{}", oldFileName, newFileName);

        if (Objects.equals(oldFileName, newFileName)) {
            return;
        }

        if (StringHelper.isBlank(newFileName)) {
            mediaService.deleteBySourceIdAndType(sourceId, mediaType);
            fileService.deleteFile(oldFileName);
            return;
        }

        Media media = mediaService.getBySourceIdAndMediaType(sourceId, mediaType).orElse(null);

        if (media == null) {
            mediaService.save(Media.builder()
                                   .sourceId(sourceId)
                                   .storedKey(newFileName)
                                   .type(mediaType)
                                   .build());

            fileService.saveFile(newFileName);
        } else {
            media.setStoredKey(newFileName);
            mediaService.save(media);

            fileService.replaceFile(newFileName, oldFileName);
        }
    }

    @Transactional
    public void deleteMedia(final Long id) {
        log.info("Delete media by id #{}", id);

        Media media = mediaService.getById(id)
                                  .orElseThrow(() -> new ObjectNotFoundException("media", MEDIA_NOT_FOUND));

        mediaService.delete(media);
        fileService.deleteFile(media.getStoredKey());
    }
}
