package com.nals.rw360.service.v1;

import com.nals.rw360.domain.Group;
import com.nals.rw360.domain.User;
import com.nals.rw360.repository.GroupRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@Transactional(readOnly = true)
public class GroupService
    extends BaseService<Group, GroupRepository> {

    public GroupService(final GroupRepository repository) {
        super(repository);
    }

    public boolean existsGroupByName(final String name, final Long id) {
        log.info("Check exists group by name #{}", name);
        return getRepository().existsByNameAndIdIsNot(name, id);
    }

    public Page<Group> searchGroupByUserIdAndName(final Long currentUserId,
                                                  final String keyword,
                                                  final Pageable pageable) {
        log.info("Search group by user id #{} and name #{}", currentUserId, keyword);
        return getRepository().findGroupByUserIdAndName(currentUserId, keyword, pageable);
    }

    public Page<Group> searchGroupByName(final String keyword, final Pageable pageable) {
        log.info("Search group by name #{}", keyword);
        return getRepository().findGroupByName(keyword, pageable);
    }

    public boolean existsGroupByName(final String name) {
        log.info("Check exists user by name #{}", name);
        return getRepository().existsByName(name);
    }

    public List<User> fetchAllGroupLeader(final Set<String> roles) {
        log.info("Fetch all group leader #{}", roles);
        return getRepository().fetchUsersByRole(roles);
    }

    public Optional<Group> getByIdAndUserId(final Long id, final Long currentUserId) {
        log.info("Get group by id #{} and user id #{}", id, currentUserId);
        return getRepository().findByIdAndUserId(id, currentUserId);
    }

    public Optional<Long> getManagerIdById(final Long id) {
        log.info("Get manager id by id #{}", id);
        return getRepository().findManagerIdById(id);
    }
}
