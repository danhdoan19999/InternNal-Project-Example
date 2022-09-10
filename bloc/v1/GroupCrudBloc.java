package com.nals.rw360.bloc.v1;

import com.nals.rw360.domain.Group;
import com.nals.rw360.domain.User;
import com.nals.rw360.dto.v1.request.group.GroupCreateReq;
import com.nals.rw360.dto.v1.request.group.GroupUpdateReq;
import com.nals.rw360.dto.v1.response.group.GroupDetailRes;
import com.nals.rw360.dto.v1.response.user.LeaderRes;
import com.nals.rw360.errors.ObjectNotFoundException;
import com.nals.rw360.errors.ValidatorException;
import com.nals.rw360.helpers.SecurityHelper;
import com.nals.rw360.helpers.StringHelper;
import com.nals.rw360.mapper.v1.GroupMapper;
import com.nals.rw360.mapper.v1.UserMapper;
import com.nals.rw360.service.v1.FileService;
import com.nals.rw360.service.v1.GroupService;
import com.nals.rw360.service.v1.RoleService;
import com.nals.rw360.service.v1.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static com.nals.rw360.enums.MediaType.GROUP_IMAGE;
import static com.nals.rw360.enums.RoleType.ROLE_ACCOUNT_LEADER;
import static com.nals.rw360.enums.RoleType.ROLE_MANAGER;
import static com.nals.rw360.errors.ErrorCodes.GROUP_NAME_ALREADY_USED;
import static com.nals.rw360.errors.ErrorCodes.MANAGER_ID_NOT_FOUND;
import static com.nals.rw360.errors.ErrorCodes.NOT_PERMISSION_UPDATE_GROUP;
import static com.nals.rw360.errors.ErrorCodes.ROLE_NOT_LEADER;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupCrudBloc {

    private final FileService fileService;

    private final GroupService groupService;

    private final UserService userService;

    private final RoleService roleService;

    private final MediaCrudBloc mediaCrudBloc;

    public GroupDetailRes getGroupDetail(final Long groupId) {
        log.info("Get basic info of group has id #{}", groupId);
        Group group;

        if (SecurityHelper.isSuperUser()) {
            group = groupService.getById(groupId).orElseThrow(() -> new ObjectNotFoundException("group"));
        } else {
            group = groupService.getByIdAndUserId(groupId, SecurityHelper.getCurrentUserId())
                                .orElseThrow(() -> new ObjectNotFoundException("group"));
        }

        User leader = userService.getById(group.getManagerId())
                                 .orElseThrow(() -> new ObjectNotFoundException("user"));

        LeaderRes leaderRes = UserMapper.INSTANCE.toLeaderRes(leader);
        leaderRes.setImageUrl(fileService.getFullFileUrl(leader.getImageName()));

        GroupDetailRes res = GroupMapper.INSTANCE.toGroupDetailRes(group);
        res.setLeader(leaderRes);
        res.setImageUrl(fileService.getFullFileUrl(group.getImageName()));

        return res;
    }

    @Transactional
    public Long createGroup(final GroupCreateReq req) {
        log.info("Create group with data #{}", req);

        validateCreateGroup(req);

        Long groupId = groupService.save(GroupMapper.INSTANCE.toGroup(req)).getId();

        if (StringHelper.isNotBlank(req.getImageName())) {
            mediaCrudBloc.saveMedia(req.getImageName(), groupId, GROUP_IMAGE);
        }

        return groupId;
    }

    @Transactional
    public void updateGroup(final Long id, final GroupUpdateReq req) {
        log.info("Update group info by id #{} with data #{}", id, req);

        var group = groupService.getById(id)
                                .orElseThrow(() -> new ObjectNotFoundException("group"));

        User user = userService.getCurrentUser();

        validateBeforeUpdateGroup(id, req, user.getId(), group);

        mediaCrudBloc.replaceMedia(req.getImageName(), group.getImageName(), id, GROUP_IMAGE);
        groupService.save(GroupMapper.INSTANCE.toGroup(req, group));
    }

    private void validateCreateGroup(final GroupCreateReq req) {
        if (groupService.existsGroupByName(req.getName())) {
            throw new ValidatorException("Group name already used", "name", GROUP_NAME_ALREADY_USED);
        }

        if (!isGroupLeader(req.getManagerId())) {
            throw new ValidatorException("User don't have role for to be a group leader",
                                         "manager_id",
                                         ROLE_NOT_LEADER);
        }
    }

    private boolean isGroupLeader(final Long userId) {
        Set<String> roles = new HashSet<>();
        roles.add(ROLE_MANAGER.name());
        roles.add(ROLE_ACCOUNT_LEADER.name());

        return roleService.existsByUserIdAndNames(userId, roles);
    }

    private void validateBeforeUpdateGroup(final Long id,
                                           final GroupUpdateReq req,
                                           final Long userId,
                                           final Group group) {
        if (groupService.existsGroupByName(req.getName(), id)) {
            throw new ValidatorException("Group name already used", "name", GROUP_NAME_ALREADY_USED);
        }

        if (!userService.existsById(req.getManagerId())) {
            throw new ObjectNotFoundException("Manager not found", "manager_id", MANAGER_ID_NOT_FOUND);
        }

        if (!isGroupLeader(req.getManagerId())) {
            throw new ValidatorException("User don't have role for to be a group leader",
                                         "manager_id",
                                         ROLE_NOT_LEADER);
        }

        if (!SecurityHelper.isAdmin() && !group.getManagerId().equals(userId)) {
            throw new ValidatorException("Do not have permission to update group",
                                         NOT_PERMISSION_UPDATE_GROUP);
        }
    }
}
