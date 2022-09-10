package com.nals.rw360.service.v1;

import com.nals.rw360.domain.UserRole;
import com.nals.rw360.errors.ObjectNotFoundException;
import com.nals.rw360.repository.UserRoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.nals.rw360.errors.ErrorCodes.USER_NOT_FOUND;

@Slf4j
@Service
@Transactional(readOnly = true)
public class UserRoleService
    extends BaseService<UserRole, UserRoleRepository> {

    public UserRoleService(final UserRoleRepository repository) {
        super(repository);
    }

    public UserRole getUserRoleByUserId(final Long userId) {
        log.info("Get UserRole by userId #{}", userId);

        return getRepository().findUserRoleByUserId(userId)
                              .orElseThrow(() -> new ObjectNotFoundException("User", USER_NOT_FOUND));
    }

    public boolean existsByRoleNameAndUserId(final String roleName, final Long useId) {
        log.info("Check exists user role by roleName #{} and userId #{}", roleName, useId);
        return getRepository().existsByRoleNameAndUserId(roleName, useId);
    }
}
