package com.nals.rw360.bloc.v1;

import com.nals.rw360.domain.SubGroup;
import com.nals.rw360.domain.User;
import com.nals.rw360.dto.v1.request.group.sub.SubGroupSearchReq;
import com.nals.rw360.dto.v1.request.group.sub.member.MemberSearchReq;
import com.nals.rw360.dto.v1.response.group.sub.SubGroupSearchRes;
import com.nals.rw360.dto.v1.response.group.sub.member.MemberRes;
import com.nals.rw360.dto.v1.response.group.sub.member.SubGroupMemberRes;
import com.nals.rw360.errors.ValidatorException;
import com.nals.rw360.helpers.PaginationHelper;
import com.nals.rw360.helpers.SecurityHelper;
import com.nals.rw360.helpers.UserHelper;
import com.nals.rw360.mapper.v1.SubGroupMapper;
import com.nals.rw360.mapper.v1.UserMapper;
import com.nals.rw360.service.v1.FileService;
import com.nals.rw360.service.v1.GroupTypeService;
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.nals.rw360.enums.RoleType.ROLE_MEMBER;
import static com.nals.rw360.errors.ErrorCodes.FORBIDDEN;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubGroupListBloc {
    private final FileService fileService;
    private final UserService userService;
    private final RoleService roleService;
    private final UserSubGroupService userSubGroupService;
    private final GroupTypeService groupTypeService;
    private final SubGroupService subGroupService;

    public Page<MemberRes> searchMembers(final Long groupId, final Long subGroupId, final MemberSearchReq req) {
        log.info("Search members by group id #{} sub group id #{} and name #{}", groupId, subGroupId, req.getKeyword());
        Long currentUserId = SecurityHelper.getCurrentUserId();
        boolean existsMember = userSubGroupService.existsByGroupIdAndSubGroupIdAndUserId(groupId,
                                                                                         subGroupId,
                                                                                         currentUserId);

        if (SecurityHelper.hasRole(ROLE_MEMBER.name()) && !existsMember) {
            throw new ValidatorException("Do not have permission to get list member", "", FORBIDDEN);
        }

        Pageable pageable = PaginationHelper.generatePageRequest(req);
        Page<User> members = userService.searchByGroupIdAndSubGroupIdAndName(groupId, subGroupId,
                                                                             req.getKeyword(), pageable);

        return UserHelper.members(roleService, fileService, members);
    }

    public Page<SubGroupSearchRes> searchSubGroups(final Long groupId, final SubGroupSearchReq req) {
        log.info("Search sub-group in group have group id #{} by #{} with keyword #{}",
                 groupId, req.getSortColumn(), req.getKeyword());
        Long currentUserId = SecurityHelper.getCurrentUserId();

        Pageable pageable = PaginationHelper.generatePageRequest(req);
        Page<SubGroup> subGroups;

        if (SecurityHelper.hasRole(ROLE_MEMBER.name())) {
            subGroups = subGroupService.searchByGroupIdAndUserIdAndName(groupId, currentUserId,
                                                                        req.getKeyword(), pageable);
        } else {
            subGroups = subGroupService.searchByGroupIdAndName(groupId, req.getKeyword(), pageable);
        }

        var groupTypeIds = groupTypeService.fetchAll();

        var subGroupIds = subGroups.stream().map(SubGroup::getId).collect(Collectors.toSet());
        var usersMap = userService.fetchUsersBySubGroupId(subGroupIds)
                                  .stream()
                                  .collect(Collectors.groupingBy(User::getSubGroupId));

        return subGroups.map(subGroup -> {

            var groupTypeId = groupTypeIds.stream()
                                          .filter(groupType -> groupType.getId() == subGroup.getGroupTypeId())
                                          .findAny()
                                          .orElse(null);

            var res = Optional.ofNullable(usersMap.get(subGroup.getId()))
                              .stream()
                              .flatMap(List::stream)
                              .map(user -> {
                                  SubGroupMemberRes memberSubGroupRes = UserMapper.INSTANCE.toSubGroupMemberRes(user);
                                  memberSubGroupRes.setImageUrl(
                                      fileService.getFullFileUrl(memberSubGroupRes.getImageName()));
                                  return memberSubGroupRes;
                              }).collect(Collectors.toList());

            SubGroupSearchRes subGroupSearchRes = SubGroupMapper.INSTANCE.toSubGroupSearchRes(subGroup);
            subGroupSearchRes.setGroupType(groupTypeId.getName());
            subGroupSearchRes.setImageUrl(fileService.getFullFileUrl(subGroup.getImageName()));
            subGroupSearchRes.setUsers(res);
            return subGroupSearchRes;
        });
    }
}
