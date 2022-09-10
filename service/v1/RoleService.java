package com.nals.rw360.service.v1;

import com.nals.rw360.domain.Role;
import com.nals.rw360.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@Transactional(readOnly = true)
public class RoleService
    extends BaseService<Role, RoleRepository> {

    public RoleService(final RoleRepository repository) {
        super(repository);
    }

    public Optional<Role> getByName(final String name) {
        log.info("Get role by name #{}", name);
        return getRepository().findByName(name);
    }

    public Set<Role> fetchByUserId(final Long userId) {
        log.info("Fetch roles by user id #{}", userId);
        return getRepository().fetchByUserId(userId);
    }

    public boolean existsByUserIdAndNames(final Long userId, final Set<String> roles) {
        log.info("Check role for group leader by userId #{} and names #{}", userId, roles);
        return getRepository().existsByUserIdAndNames(userId, roles);
    }

    public List<Role> fetchByUserIds(final Collection<Long> userIds) {
        log.info("Fetch by user ids #{}", userIds);
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        return getRepository().fetchByUserIds(userIds);
    }
}
