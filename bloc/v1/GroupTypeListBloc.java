package com.nals.rw360.bloc.v1;

import com.nals.rw360.dto.v1.response.group.type.GroupTypeRes;
import com.nals.rw360.mapper.v1.GroupTypeMapper;
import com.nals.rw360.service.v1.GroupTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupTypeListBloc {
    private final GroupTypeService groupTypeService;

    public List<GroupTypeRes> fetchAllGroupType() {
        log.info("Fetch all group type");
        return groupTypeService.fetchAll()
                               .stream()
                               .map(GroupTypeMapper.INSTANCE::toGroupTypeRes)
                               .collect(Collectors.toList());
    }
}
