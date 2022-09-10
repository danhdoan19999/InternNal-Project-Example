package com.nals.rw360.bloc.v1;

import com.nals.rw360.domain.Group;
import com.nals.rw360.domain.SubGroup;
import com.nals.rw360.dto.v1.request.group.GroupSearchReq;
import com.nals.rw360.dto.v1.request.group.sub.member.MemberSearchReq;
import com.nals.rw360.dto.v1.response.group.GroupLeaderRes;
import com.nals.rw360.dto.v1.response.group.GroupRes;
import com.nals.rw360.dto.v1.response.group.sub.SubGroupRes;
import com.nals.rw360.dto.v1.response.group.sub.member.MemberRes;
import com.nals.rw360.helpers.PaginationHelper;
import com.nals.rw360.helpers.SecurityHelper;
import com.nals.rw360.helpers.UserHelper;
import com.nals.rw360.mapper.v1.GroupMapper;
import com.nals.rw360.mapper.v1.SubGroupMapper;
import com.nals.rw360.service.v1.FileService;
import com.nals.rw360.service.v1.GroupService;
import com.nals.rw360.service.v1.RoleService;
import com.nals.rw360.service.v1.SubGroupService;
import com.nals.rw360.service.v1.UserService;
import com.nals.rw360.service.v1.UserSubGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.nals.rw360.enums.RoleType.ROLE_ACCOUNT_LEADER;
import static com.nals.rw360.enums.RoleType.ROLE_MANAGER;
import static com.nals.rw360.enums.RoleType.ROLE_MEMBER;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupListBloc {

    private final FileService fileService;

    private final GroupService groupService;

    private final SubGroupService subGroupService;

    private final UserService userService;

    private final RoleService roleService;

    private final UserSubGroupService userSubGroupService;

    public Page<GroupRes> searchGroups(final GroupSearchReq req) {
        log.info("Search group by name");
        Long currentUserId = SecurityHelper.getCurrentUserId();

        Pageable pageable = PaginationHelper.generatePageRequest(req);
        Page<Group> groups;

        if (SecurityHelper.isSuperUser()) {
            groups = groupService.searchGroupByName(req.getKeyword(), pageable);
        } else {
            groups = groupService.searchGroupByUserIdAndName(currentUserId, req.getKeyword(), pageable);
        }

        return groups.map(group -> {
            GroupRes groupRes = GroupMapper.INSTANCE.toGroupRes(group);
            groupRes.setImageUrl(fileService.getFullFileUrl(group.getImageName()));
            groupRes.setSubGroups(fetchSubGroupByGroupId(group.getId()));
            return groupRes;
        });
    }

    public Page<MemberRes> searchMembers(final Long groupId, final MemberSearchReq req) {
        log.info("Search members of group by group id #{}", groupId);
        var pageable = PaginationHelper.generatePageRequest(req);
        var group = groupService.getById(groupId, "group");

        var members = userService.fetchByIdAndName(fetchUserIdsByGroupId(group), req.getKeyword(), pageable);

        return UserHelper.members(roleService, fileService, members);
    }

    public List<GroupLeaderRes> fetchAllGroupLeader() {
        log.info("Fetch all group leader");

        Set<String> roles = new HashSet<>();
        roles.add(ROLE_MANAGER.name());
        roles.add(ROLE_ACCOUNT_LEADER.name());

        return groupService.fetchAllGroupLeader(roles)
                           .stream()
                           .map(user -> {
                               GroupLeaderRes groupLeaderRes = GroupMapper.INSTANCE.toGroupLeaderRes(user);
                               groupLeaderRes.setImageUrl(fileService.getFullFileUrl(user.getImageName()));
                               return groupLeaderRes;
                           }).collect(Collectors.toList());
    }

    private List<SubGroupRes> fetchSubGroupByGroupId(final Long groupId) {
        List<SubGroup> subGroups = subGroupService.fetchSubGroupsByGroupId(groupId);
        List<SubGroupRes> res = new ArrayList<>();

        subGroups.forEach(subGroup -> {
            SubGroupRes subGroupRes = SubGroupMapper.INSTANCE.toSubGroupRes(subGroup);
            subGroupRes.setImageUrl(fileService.getFullFileUrl(subGroup.getImageName()));
            res.add(subGroupRes);
        });

        return res;
    }

    private Set<Long> fetchUserIdsByGroupId(final Group group) {
        Set<Long> userIds;

        if (SecurityHelper.hasRole(ROLE_MEMBER.name())) {
            userIds = userSubGroupService.fetchAllUserIdsByGroupIdAndCurrentUserId(group.getId(),
                                                                                   SecurityHelper.getCurrentUserId());
        } else {
            userIds = userSubGroupService.fetchAllUserIdByGroupId(group.getId());
        }

        userIds.add(group.getManagerId());

        return userIds;
    }
}
