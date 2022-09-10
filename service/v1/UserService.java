package com.nals.rw360.service.v1;

import com.nals.rw360.domain.User;
import com.nals.rw360.enums.AuthProvider;
import com.nals.rw360.errors.ObjectNotFoundException;
import com.nals.rw360.helpers.SecurityHelper;
import com.nals.rw360.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class UserService
    extends BaseService<User, UserRepository> {

    public UserService(final UserRepository repository) {
        super(repository);
    }

    public String getEmailByKey(final String key) {
        log.info("Get email by activation key or reset key: #{}", key);
        return getRepository().getEmailByKey(key);
    }

    public Optional<User> getByUsername(final String username) {
        log.info("Get by username #{}", username);
        return getRepository().findOneByUsername(username);
    }

    public User getBasicInfoById(final Long id) {
        log.info("Get basic info by id #{}", id);
        return getRepository().getBasicInfoById(id);
    }

    public boolean existsUserByEmail(final String email) {
        log.info("Check exists user by email {}", email);
        return getRepository().existsByEmail(email);
    }

    public boolean existsUserByPhone(final String phone, final Long id) {
        log.info("Check exists user by phone {}", phone);
        return getRepository().existsByPhoneAndIdIsNot(phone, id);
    }

    public Optional<User> getByActivationKey(final String activationKey) {
        log.info("Get user by activation key #{}", activationKey);
        return getRepository().findOneByActivationKey(activationKey);
    }

    public Optional<User> getByEmail(final String email) {
        log.info("Get user by email #{}", email);
        return getRepository().findOneByEmail(email);
    }

    public Optional<User> getActivatedByEmail(final String email) {
        log.info("Get activated by email #{}", email);
        return getRepository().findOneByEmailAndActivatedIsTrue(email);
    }

    public Optional<User> getByResetKey(final String key) {
        log.info("Get by reset key #{}", key);
        return getRepository().findOneByResetKeyAndActivatedIsTrue(key);
    }

    public Optional<User> getBySocialInfo(final String socialId,
                                          final String socialEmail,
                                          final AuthProvider provider) {
        log.info("Get user by social info #{}-{}-{}", provider, socialId, socialEmail);
        return getRepository().getBySocialInfo(socialId, socialEmail, provider.name());
    }

    public User getCurrentUser() {
        Long userId = SecurityHelper.getCurrentUserId();
        log.info("Get current user #{}", userId);
        return getRepository().findById(userId).orElseThrow(() -> new ObjectNotFoundException("user"));
    }

    @Transactional
    public void refreshLoginSuccessInfo(final String username) {
        log.info("Refresh last login success info by username #{}", username);
        getRepository().refreshLoginSuccessInfo(username, Instant.now());
    }

    public List<User> searchUsersByNameIsNotRole(final String keyword, final String roleName) {
        log.info("Search users by name #{} is not role #{}:", keyword, roleName);
        return getRepository().searchUsersByNameIsNotRole(keyword, roleName);
    }

    public Page<User> searchByGroupIdAndSubGroupIdAndName(final Long groupId,
                                                          final Long subGroupId,
                                                          final String keyword,
                                                          final Pageable pageable) {
        log.info("Search members by group id #{} , sub group id #{} and name #{}", groupId, subGroupId, keyword);
        return getRepository().searchByGroupIdAndSubGroupIdAndName(groupId, subGroupId, keyword, pageable);
    }

    public List<User> fetchUsersBySubGroupId(final Collection<Long> subGroupIds) {
        log.info("Fetch list user by sub group ids: #{}", subGroupIds);
        return getRepository().findAllBySubGroupId(subGroupIds);
    }

    public List<User> fetchAllByIdNotIn(final Collection<Long> ids) {
        log.info("Fetch all user by id not in ids: #{}", ids);
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return getRepository().findAllByIdNotIn(ids);
    }

    public Page<User> fetchByIdAndName(final Collection<Long> ids, final String keyword, final Pageable pageable) {
        log.info("Fetch all user by id in #{} and keyword #{}", ids, keyword);
        return getRepository().findByIdAndName(ids, keyword, pageable);
    }

    public Page<User> fetchByName(final String keyword, final Pageable pageable) {
        log.info("Fetch user by name #{}", keyword);
        return getRepository().findByName(keyword, pageable);
    }
}
