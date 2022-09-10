package com.nals.rw360.service.v1;

import com.nals.rw360.domain.SubGroup;
import com.nals.rw360.repository.SubGroupRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class SubGroupService
    extends BaseService<SubGroup, SubGroupRepository> {

    public SubGroupService(final SubGroupRepository repository) {
        super(repository);
    }

    public List<SubGroup> fetchSubGroupsByGroupId(final Long groupId) {
        log.info("Fetch list sub group by group id: {}", groupId);
        return getRepository().findAllByGroupId(groupId);
    }

    public boolean existsByName(final String name) {
        log.info("Check exists sub group by name #{}", name);
        return getRepository().existsByName(name);
    }

    public Optional<SubGroup> getByIdAndUserId(final Long id, final Long userId) {
        log.info("Get sub group by id #{} and user id #{}", id, userId);
        return getRepository().findByIdAndUserId(id, userId);
    }

    public Page<SubGroup> searchByGroupIdAndName(final Long groupId,
                                                 final String keyword,
                                                 final Pageable pageable) {
        log.info("Search sub-groups by group id: #{} and name with keyword: #{}", groupId, keyword);
        return getRepository().findAllByGroupIdAndName(groupId, keyword, pageable);
    }

    public Page<SubGroup> searchByGroupIdAndUserIdAndName(final Long groupId,
                                                          final Long userId,
                                                          final String keyword,
                                                          final Pageable pageable) {
        log.info("Search sub-groups by group id #{}, user id #{} and name with keyword #{}", groupId, userId, keyword);
        return getRepository().findAllByGroupIdAndUserIdAndName(groupId, userId, keyword, pageable);
    }

    public boolean existsByGroupId(final Long groupId) {
        log.info("Check exists sub group by group id #{}", groupId);
        return getRepository().existsByGroupId(groupId);
    }

    public Optional<Long> getGroupIdById(final Long id) {
        log.info("Get group id by sub group id #{}", id);
        return getRepository().findGroupIdById(id);
    }

    public boolean existsByNameAndIdIsNot(final String name, final Long id) {
        log.info("Check exists sub group by name #{} and id is not #{}", name, id);
        return getRepository().existsByNameAndIdIsNot(name, id);
    }

    public boolean existsByIdAndManagerId(final Long id, final Long managerId) {
        log.info("Check exists sub group by id #{} and manager id #{}", id, managerId);
        return getRepository().existsByIdAndManagerId(id, managerId);
    }
}
