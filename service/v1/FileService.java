package com.nals.rw360.service.v1;

import com.nals.rw360.config.ApplicationProperties;
import com.nals.rw360.errors.FileException;
import com.nals.rw360.helpers.FileHelper;
import com.nals.rw360.helpers.StringHelper;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import liquibase.repackaged.org.apache.commons.collections4.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import static com.nals.rw360.helpers.StringHelper.EMPTY;

@Slf4j
@Service
public class FileService {
    private static final String FILE_NAME_PATTERN = "%s_%s.%s";
    private static final int LENGTH_OF_RANDOM_STRING = 30;
    private final String bucketName;
    private final String tempDir;
    private final String workingDir;
    private final MinioClient minioClient;
    private final int maxSizeAllow;
    private final Set<String> allowExtensions;
    private final String minioFrontEndpointUrl;

    public FileService(final ApplicationProperties applicationProperties,
                       final MinioClient minioClient) {
        ApplicationProperties.MinIO minIOConfig = applicationProperties.getMinIO();
        this.bucketName = minIOConfig.getBucketName();
        this.tempDir = minIOConfig.getTempDir();
        this.workingDir = minIOConfig.getWorkingDir();
        this.minioFrontEndpointUrl = minIOConfig.getAddress();
        this.minioClient = minioClient;

        ApplicationProperties.FileUpload fileUpload = applicationProperties.getFileUpload();
        this.maxSizeAllow = fileUpload.getMaxSizeAllow();
        this.allowExtensions = fileUpload.getAllowExtensions();
    }

    public void saveFile(final String fileName) {
        log.debug("Save file #{}", fileName);
        saveFile(fileName, allowExtensions);
    }

    public void replaceFile(final String fileName, final String oldFileName) {
        log.debug("Replace file #{} to #{}", oldFileName, fileName);
        replaceFile(fileName, oldFileName, allowExtensions);
    }

    public void replaceFile(final String fileName, final String oldFileName, final Set<String> extensions) {
        log.debug("Replace file #{} to #{}", oldFileName, fileName);

        if (Objects.equals(fileName, oldFileName)) {
            return;
        }

        if (invalidFileName(fileName)) {
            throw new FileException("Invalid file name");
        }

        saveFile(fileName, extensions);

        deleteFile(oldFileName);
    }

    public String generateFileName(final String originalFilename) {
        return String.format(FILE_NAME_PATTERN,
                             RandomStringUtils.randomAlphanumeric(LENGTH_OF_RANDOM_STRING),
                             Instant.now().toEpochMilli(),
                             FileHelper.getExtension(originalFilename));
    }

    public String getFullFileUrl(final String fileName) {
        if (StringHelper.isBlank(fileName)) {
            return null;
        }

        if (fileName.startsWith("http")) {
            return fileName;
        }

        String protocol;
        try {
            protocol = String.format("%s://", new URL(minioFrontEndpointUrl).getProtocol());
        } catch (MalformedURLException e) {
            log.warn(e.getMessage());
            return fileName;
        }

        String cdnHost = minioFrontEndpointUrl.replace(protocol, EMPTY);
        String fileHost = FileHelper.concatPath(cdnHost, bucketName, workingDir, fileName.trim());

        return String.format("%s%s", protocol, fileHost);
    }

    public String getFullFileTempUrl(final String fileName) {
        return getFullFileUrl(fileName).replace(workingDir, tempDir);
    }

    public String uploadFile(final InputStream inputStream, final String fileName) {
        log.debug("Upload file #{}", fileName);
        return uploadFile(generateFileName(fileName), inputStream, tempDir);
    }

    private String uploadFile(final String fileName, final InputStream inputStream, final String targetDir) {
        if (invalidFileName(fileName)) {
            return null;
        }

        String key = makeObjectRequestKey(targetDir, fileName);

        try {
            PutObjectArgs objectArgs = PutObjectArgs.builder()
                                                    .bucket(bucketName)
                                                    .object(key)
                                                    .contentType(URLConnection.guessContentTypeFromName(fileName))
                                                    .stream(inputStream, -1, 1024 * 1024 * 5)
                                                    .build();

            minioClient.putObject(objectArgs);
        } catch (Exception e) {
            throw new FileException("Upload failed", e);
        }

        return fileName;
    }

    private void saveFile(final String fileName, final Set<String> extensions) {
        if (invalidFileName(fileName)) {
            return;
        }

        validateExtension(fileName, extensions);
        moveFile(tempDir, workingDir, fileName);
    }

    private void moveFile(final String sourceDir, final String destinationDir, final String fileName) {
        // Copy file from temp dir to working dir
        copyFile(sourceDir, fileName, destinationDir, fileName);

        // Delete temp file after copied to working dir
        deleteFile(tempDir, fileName);
    }

    private void copyFile(final String sourceDir, final String originalPath,
                          final String targetDir, final String destinationPath) {

        try {
            String sourceKey = makeObjectRequestKey(sourceDir, originalPath);
            String destinationKey = makeObjectRequestKey(targetDir, destinationPath);

            CopyObjectArgs request = CopyObjectArgs.builder()
                                                   .bucket(bucketName)
                                                   .object(destinationKey)
                                                   .source(
                                                       CopySource.builder()
                                                                 .bucket(bucketName)
                                                                 .object(sourceKey)
                                                                 .build()
                                                   )
                                                   .build();
            minioClient.copyObject(request);
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new FileException("Copy failed", e);
        }
    }

    private void deleteFile(final String dir, final String fileName) {
        try {
            String key = makeObjectRequestKey(dir, fileName);

            RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                                                                .bucket(bucketName)
                                                                .object(key)
                                                                .build();

            minioClient.removeObject(removeObjectArgs);
        } catch (Exception e) {
            throw new FileException("Delete failed", e);
        }
    }

    public Collection<String> getAllFileTemp() {
        ListObjectsArgs listObjectsArgs = ListObjectsArgs.builder()
                                                         .bucket(bucketName)
                                                         .prefix(tempDir)
                                                         .recursive(true)
                                                         .build();
        Collection<String> fileNames = new ArrayList<>();
        try {
            for (Result<Item> item : minioClient.listObjects(listObjectsArgs)) {
                fileNames.add(item.get().objectName());
            }
            return fileNames;
        } catch (Exception e) {
            throw new FileException("No files");
        }
    }

    public void deleteAllFileTemp(final Collection<String> fileNames) {
        log.debug("Delete files #{}", fileNames);
        if (CollectionUtils.isEmpty(fileNames)) {
            return;
        }

        fileNames.forEach(fileName -> {
            deleteFile(EMPTY, fileName);
        });
    }

    public void deleteFile(final String fileName) {
        log.debug("Delete file #{}", fileName);
        if (invalidFileName(fileName)) {
            return;
        }

        deleteFile(workingDir, fileName);
    }

    private String makeObjectRequestKey(final String dir, final String fileName) {
        String key = StringHelper.isBlank(dir) ? fileName : FileHelper.concatPath(dir, fileName);
        return key.startsWith("/") ? key.substring(1) : key;
    }

    private void validateExtension(final String fileName, final Set<String> extensions) {
        String fileExtension = FileHelper.getExtension(fileName);
        if (extensions.stream().noneMatch(extension -> extension.equalsIgnoreCase(fileExtension))) {
            throw new FileException("Invalid extension");
        }
    }

    public void validateFile(final MultipartFile uploadFile)
        throws IOException {
        if (Objects.isNull(uploadFile)) {
            throw new FileException("File not found");
        }

        if (StringHelper.isBlank(uploadFile.getOriginalFilename())) {
            throw new FileException("Invalid file name");
        }

        if (uploadFile.isEmpty() || uploadFile.getBytes().length > maxSizeAllow * 1024) {
            throw new FileException("File size is not allow");
        }

        validateExtension(uploadFile.getOriginalFilename(), allowExtensions);
    }

    private boolean invalidFileName(final String fileName) {
        return StringHelper.isBlank(fileName) || fileName.startsWith("http");
    }
}
