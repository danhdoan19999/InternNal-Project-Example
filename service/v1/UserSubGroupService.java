package com.nals.rw360.service.v1;

import com.nals.rw360.domain.UserSubGroup;
import com.nals.rw360.repository.UserSubGroupRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@Transactional(readOnly = true)
public class UserSubGroupService
    extends BaseService<UserSubGroup, UserSubGroupRepository> {

    public UserSubGroupService(final UserSubGroupRepository repository) {
        super(repository);
    }

    public boolean existsByGroupIdAndSubGroupIdAndUserId(final Long groupId, final Long subGroupId, final Long userId) {
        log.info("exists user sub group by group id #{} and sub group id #{} and user id #{}",
                 groupId, subGroupId, userId);
        return getRepository().existsByGroupIdAndSubGroupIdAndUserId(groupId, subGroupId, userId);
    }

    public Set<Long> fetchUserIdsBySubGroupId(final Long subGroupId) {
        return getRepository().findAllUserIdsBySubGroupId(subGroupId);
    }

    public boolean existsBySubGroupIdAndUserId(final Long subGroupId, final Long userId) {
        log.info("Check exists user sub group by sub group id #{} and user id #{}", subGroupId, userId);
        return getRepository().existsBySubGroupIdAndUserId(subGroupId, userId);
    }

    public Optional<UserSubGroup> getByUserIdAndSubGroupId(final Long userId, final Long subGroupId) {
        log.info("Get user from sub group by user id #{} and sub group id #{}", userId, subGroupId);
        return getRepository().findByUserIdAndSubGroupId(userId, subGroupId);
    }

    public Set<Long> fetchAllUserIdByGroupIdAndSubGroupId(final Long groupId, final Long subGroupId) {
        log.info("Fetch all user id by group id #{} and sub group id #{}", groupId, subGroupId);
        return getRepository().findAllUserIdByGroupIdAndSubGroupId(groupId, subGroupId);
    }

    public Set<Long> fetchAllUserIdsByGroupIdAndCurrentUserId(final Long groupId, final Long currentUserId) {
        log.info("Fetch all user id by group id #{} and current user id #{}", groupId, currentUserId);
        return getRepository().findAllUserIdsByGroupIdAndCurrentUserId(groupId, currentUserId);
    }

    public Set<Long> fetchAllUserIdByGroupId(final Long groupId) {
        log.info("Fetch all user id by group id #{}", groupId);
        return getRepository().findAllUserIdByGroupId(groupId);
    }
}
