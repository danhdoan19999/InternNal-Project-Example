package com.nals.rw360.bloc.v1;

import com.nals.rw360.dto.v1.response.role.RoleRes;
import com.nals.rw360.mapper.v1.RoleMapper;
import com.nals.rw360.service.v1.RoleService;
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
public class RoleListBloc {

    private final RoleService roleService;

    public List<RoleRes> fetchAllRoles() {
        log.info("Fetch all role");
        return roleService.fetchAll()
                          .stream()
                          .map(RoleMapper.INSTANCE::toRoleRes)
                          .collect(Collectors.toList());
    }
}
