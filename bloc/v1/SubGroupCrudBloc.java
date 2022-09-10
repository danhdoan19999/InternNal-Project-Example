package com.nals.rw360.bloc.v1;

import com.nals.rw360.domain.GroupType;
import com.nals.rw360.domain.SubGroup;
import com.nals.rw360.domain.User;
import com.nals.rw360.dto.v1.request.group.sub.AddMemberReq;
import com.nals.rw360.dto.v1.request.group.sub.SubGroupCreateReq;
import com.nals.rw360.dto.v1.request.group.sub.SubGroupUpdateReq;
import com.nals.rw360.dto.v1.response.group.sub.SubGroupDetailRes;
import com.nals.rw360.dto.v1.response.group.sub.leader.SubGroupLeaderRes;
import com.nals.rw360.dto.v1.response.group.sub.member.SubGroupMemberRes;
import com.nals.rw360.enums.Status;
import com.nals.rw360.errors.ObjectNotFoundException;
import com.nals.rw360.errors.ValidatorException;
import com.nals.rw360.helpers.SecurityHelper;
import com.nals.rw360.helpers.StringHelper;
import com.nals.rw360.mapper.v1.GroupTypeMapper;
import com.nals.rw360.mapper.v1.SubGroupMapper;
import com.nals.rw360.mapper.v1.UserMapper;
import com.nals.rw360.service.v1.FileService;
import com.nals.rw360.service.v1.GroupService;
import com.nals.rw360.service.v1.GroupTypeService;
import com.nals.rw360.service.v1.SubGroupService;
import com.nals.rw360.service.v1.UserRoleService;
import com.nals.rw360.service.v1.UserService;
import com.nals.rw360.service.v1.UserSubGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.nals.rw360.enums.MediaType.SUB_GROUP_IMAGE;
import static com.nals.rw360.enums.RoleType.ROLE_MEMBER;
import static com.nals.rw360.errors.ErrorCodes.GROUP_TYPE_NOT_FOUND;
import static com.nals.rw360.errors.ErrorCodes.MANAGER_ID_NOT_FOUND;
import static com.nals.rw360.errors.ErrorCodes.NOT_PERMISSION_UPDATE_SUB_GROUP;
import static com.nals.rw360.errors.ErrorCodes.ROLE_NOT_LEADER;
import static com.nals.rw360.errors.ErrorCodes.SUB_GROUP_NAME_ALREADY_USED;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubGroupCrudBloc {

    private final SubGroupService subGroupService;

    private final SubGroupBloc subGroupBloc;

    private final MediaCrudBloc mediaCrudBloc;

    private final UserRoleService userRoleService;

    private final UserSubGroupService userSubGroupService;

    private final GroupService groupService;

    private final GroupTypeService groupTypeService;

    private final UserService userService;

    private final FileService fileService;

    @Transactional
    public Long createSubGroup(final SubGroupCreateReq req, final Long groupId) {
        log.info("Create sub group with data #{}", req);

        if (!groupService.existsById(groupId)) {
            throw new ObjectNotFoundException("group");
        }

        validateCreateSubGroup(req);

        Long subGroupId = saveSubGroup(req, groupId);

        if (StringHelper.isNotBlank(req.getImageName())) {
            mediaCrudBloc.saveMedia(req.getImageName(), subGroupId, SUB_GROUP_IMAGE);
        }

        return subGroupId;
    }

    public SubGroupDetailRes getSubGroupDetail(final Long subGroupId) {
        log.info("Get basic info of sub group has id #{}", subGroupId);
        SubGroup subGroup;

        if (SecurityHelper.hasRole(ROLE_MEMBER.name())) {
            subGroup = subGroupService.getByIdAndUserId(subGroupId, SecurityHelper.getCurrentUserId())
                                      .orElseThrow(() -> new ObjectNotFoundException("sub_group"));
        } else {
            subGroup = subGroupService.getById(subGroupId).orElseThrow(() -> new ObjectNotFoundException("sub_group"));
        }

        return convertSubGroupDetailRes(subGroup);
    }

    @Transactional
    public void updateSubGroup(final Long id, final SubGroupUpdateReq req) {
        log.info("Update sub group by id #{} with data #{}", id, req);

        var subGroup = subGroupService.getById(id)
                                      .orElseThrow(() -> new ObjectNotFoundException("sub_group"));

        validateBeforeUpdateSubGroup(id, req, SecurityHelper.getCurrentUserId(), subGroup);

        if (!userSubGroupService.existsBySubGroupIdAndUserId(id, req.getManagerId())) {
            saveManager(id, req.getManagerId());
        }

        mediaCrudBloc.replaceMedia(req.getImageName(), subGroup.getImageName(), id, SUB_GROUP_IMAGE);
        subGroupService.save(SubGroupMapper.INSTANCE.toSubGroup(req, subGroup));
    }

    public List<SubGroupMemberRes> fetchAllMembersNotExistsInSubGroup(final Long groupId, final Long id) {
        log.info("Fetch all members not exists in sub group by group id #{} sub group id #{}", groupId, id);

        if (!groupService.existsById(groupId)) {
            throw new ObjectNotFoundException("group");
        }

        if (!subGroupService.existsById(id)) {
            throw new ObjectNotFoundException("sub_group");
        }

        var ids = userSubGroupService.fetchAllUserIdByGroupIdAndSubGroupId(groupId, id);
        List<SubGroupMemberRes> res;

        if (ids.size() > 0) {
            res = userService.fetchAllByIdNotIn(ids).stream().map(UserMapper.INSTANCE::toSubGroupMemberRes)
                             .collect(Collectors.toList());
        } else {
            res = userService.fetchAll().stream().map(UserMapper.INSTANCE::toSubGroupMemberRes)
                             .collect(Collectors.toList());
        }

        return res;
    }

    private void validateBeforeUpdateSubGroup(final Long id,
                                              final SubGroupUpdateReq req,
                                              final Long userId,
                                              final SubGroup subGroup) {
        if (!SecurityHelper.isAdmin() && !userSubGroupService.existsBySubGroupIdAndUserId(subGroup.getId(), userId)) {
            throw new ValidatorException("Do not have permission to update sub group",
                                         "current_user", NOT_PERMISSION_UPDATE_SUB_GROUP);
        }

        if (subGroupService.existsByNameAndIdIsNot(req.getName(), id)) {
            throw new ValidatorException("Sub group name already used", "name", SUB_GROUP_NAME_ALREADY_USED);
        }

        if (userRoleService.existsByRoleNameAndUserId(ROLE_MEMBER.name(), req.getManagerId())) {
            throw new ValidatorException("User don't have role for to be a sub group leader",
                                         "manager_id",
                                         ROLE_NOT_LEADER);
        }

        if (!groupTypeService.existsById(req.getGroupTypeId())) {
            throw new ObjectNotFoundException("Group type not found", "group_type_id", GROUP_TYPE_NOT_FOUND);
        }

        if (!userService.existsById(req.getManagerId())) {
            throw new ObjectNotFoundException("Manager not found", "manager_id", MANAGER_ID_NOT_FOUND);
        }
    }

    private void saveManager(final Long subGroupId, final Long managerId) {
        var addMemberReq = AddMemberReq.builder()
                                       .ids(Collections.singleton(managerId))
                                       .build();
        subGroupBloc.addMembers(subGroupId, addMemberReq);
    }

    private Long saveSubGroup(final SubGroupCreateReq req, final Long groupId) {
        var subGroup = SubGroupMapper.INSTANCE.toSubGroup(req);
        subGroup.setStatus(Status.OPEN);
        subGroup.setGroupId(groupId);

        Long subGroupId = subGroupService.save(subGroup).getId();

        saveManager(subGroupId, req.getManagerId());

        return subGroupId;
    }

    private void validateCreateSubGroup(final SubGroupCreateReq req) {
        if (subGroupService.existsByName(req.getName())) {
            throw new ValidatorException("Sub group name already used", "name", SUB_GROUP_NAME_ALREADY_USED);
        }

        if (!userService.existsById(req.getManagerId())) {
            throw new ObjectNotFoundException("Manager not found", "manager_id", MANAGER_ID_NOT_FOUND);
        }

        if (userRoleService.existsByRoleNameAndUserId(ROLE_MEMBER.name(), req.getManagerId())) {
            throw new ValidatorException("User don't have role for to be a sub group leader",
                                         "manager_id",
                                         ROLE_NOT_LEADER);
        }

        if (!groupTypeService.existsById(req.getGroupTypeId())) {
            throw new ObjectNotFoundException("Group type not found", "group_type_id", GROUP_TYPE_NOT_FOUND);
        }
    }

    private SubGroupDetailRes convertSubGroupDetailRes(final SubGroup subGroup) {
        User user = userService.getById(subGroup.getManagerId()).get();
        SubGroupLeaderRes subGroupLeaderRes = UserMapper.INSTANCE.toSubGroupLeaderRes(user);
        subGroupLeaderRes.setImageUrl(fileService.getFullFileUrl(user.getImageName()));

        GroupType groupType = groupTypeService.getById(subGroup.getGroupTypeId()).get();

        SubGroupDetailRes res = SubGroupMapper.INSTANCE.toSubGroupDetail(subGroup);
        res.setImageUrl(fileService.getFullFileUrl(subGroup.getImageName()));
        res.setGroupType(GroupTypeMapper.INSTANCE.toGroupTypeRes(groupType));
        res.setManager(subGroupLeaderRes);

        return res;
    }
}
