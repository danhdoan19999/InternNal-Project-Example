package com.nals.rw360.service.v1;

import com.nals.rw360.domain.GroupType;
import com.nals.rw360.repository.GroupTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class GroupTypeService
    extends BaseService<GroupType, GroupTypeRepository> {

    public GroupTypeService(final GroupTypeRepository repository) {
        super(repository);
    }
}
