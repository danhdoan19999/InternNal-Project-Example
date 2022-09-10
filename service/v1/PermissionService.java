package com.nals.rw360.service.v1;

import com.nals.rw360.domain.Permission;
import com.nals.rw360.repository.PermissionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@Transactional(readOnly = true)
public class PermissionService
    extends BaseService<Permission, PermissionRepository> {

    public PermissionService(final PermissionRepository repository) {
        super(repository);
    }

    public Set<Permission> fetchByUserId(final Long userId) {
        log.info("Fetch permissions by user id #{}", userId);
        return getRepository().fetchByUserId(userId);
    }
}
