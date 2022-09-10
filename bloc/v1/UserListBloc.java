package com.nals.rw360.bloc.v1;

import com.nals.rw360.dto.v1.request.group.sub.member.MemberSearchReq;
import com.nals.rw360.dto.v1.response.group.sub.member.MemberRes;
import com.nals.rw360.helpers.PaginationHelper;
import com.nals.rw360.helpers.UserHelper;
import com.nals.rw360.service.v1.FileService;
import com.nals.rw360.service.v1.RoleService;
import com.nals.rw360.service.v1.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserListBloc {

    private final FileService fileService;
    private final UserService userService;
    private final RoleService roleService;

    public Page<MemberRes> searchUsers(final MemberSearchReq req) {
        log.info("Search by name #{}", req.getKeyword());
        var pageable = PaginationHelper.generatePageRequest(req);

        var users = userService.fetchByName(req.getKeyword(), pageable);

        return UserHelper.members(roleService, fileService, users);
    }
}
