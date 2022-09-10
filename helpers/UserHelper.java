package com.nals.rw360.helpers;

import com.nals.rw360.domain.Role;
import com.nals.rw360.domain.User;
import com.nals.rw360.dto.v1.response.group.sub.member.MemberRes;
import com.nals.rw360.mapper.v1.RoleMapper;
import com.nals.rw360.mapper.v1.UserMapper;
import com.nals.rw360.service.v1.FileService;
import com.nals.rw360.service.v1.RoleService;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class UserHelper {

    private UserHelper() {
    }

    public static Page<MemberRes> members(final RoleService roleService,
                                          final FileService fileService,
                                          final Page<User> users) {

        Set<Long> userIds = users.stream().map(User::getId).collect(Collectors.toSet());
        Map<Long, List<Role>> rolesMap = roleService.fetchByUserIds(userIds).stream()
                                                    .collect(Collectors.groupingBy(Role::getUserId));

        return users.map(user -> {
            MemberRes res = UserMapper.INSTANCE.toMemberRes(user);
            res.setImageUrl(fileService.getFullFileUrl(user.getImageName()));
            res.setRoles(RoleMapper.INSTANCE.toRolesRes(rolesMap.get(user.getId())));
            return res;
        });
    }
}
