package com.nals.rw360.bloc.v1;

import com.nals.rw360.domain.UserSubGroup;
import com.nals.rw360.dto.v1.request.group.sub.AddMemberReq;
import com.nals.rw360.errors.ObjectNotFoundException;
import com.nals.rw360.errors.ValidatorException;
import com.nals.rw360.helpers.SecurityHelper;
import com.nals.rw360.service.v1.SubGroupService;
import com.nals.rw360.service.v1.UserService;
import com.nals.rw360.service.v1.UserSubGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static com.nals.rw360.errors.ErrorCodes.CAN_NOT_REMOVE_MANAGER_SUB_GROUP;
import static com.nals.rw360.errors.ErrorCodes.NOT_PERMISSION_REMOVE_MEMBER_OF_SUB_GROUP;
import static com.nals.rw360.errors.ErrorCodes.USER_ALREADY_JOIN_THIS_SUB_GROUP;
import static com.nals.rw360.errors.ErrorCodes.USER_NOT_EXISTS_IN_SUB_GROUP;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubGroupBloc {
    private final UserSubGroupService userSubGroupService;

    private final SubGroupService subGroupService;

    private final UserService userService;

    @Transactional
    public void addMembers(final Long subGroupId, final AddMemberReq addMemberReq) {
        log.info("Add members to the sub group have id #{} with data #{}", subGroupId, addMemberReq);

        validateAddMembers(subGroupId, addMemberReq.getIds());

        var currentMemberIds = userSubGroupService.fetchUserIdsBySubGroupId(subGroupId);

        saveNewMembers(subGroupId, currentMemberIds, addMemberReq.getIds());
    }

    @Transactional
    public void saveNewMembers(final Long subGroupId,
                               final Set<Long> currentMemberIds,
                               final Set<Long> memberIds) {
        var groupId = subGroupService.getGroupIdById(subGroupId)
                                     .orElseThrow(() -> new ObjectNotFoundException("group"));

        memberIds.forEach(id -> {
            if (currentMemberIds.contains(id)) {
                throw new ValidatorException("User have id: " + id + " already join this sub-group",
                                             "user_id",
                                             USER_ALREADY_JOIN_THIS_SUB_GROUP);
            }

            userSubGroupService.save(UserSubGroup.builder()
                                                 .userId(id)
                                                 .groupId(groupId)
                                                 .subGroupId(subGroupId)
                                                 .build());
        });
    }

    @Transactional
    public void removeMember(final Long userId, final Long subGroupId) {
        log.info("Remove member by user id #{} for sub group #{}", userId, subGroupId);

        validateRemoveMember(userId, subGroupId, SecurityHelper.getCurrentUserId());

        var userSubGroup =
            userSubGroupService.getByUserIdAndSubGroupId(userId, subGroupId)
                               .orElseThrow(() -> new ObjectNotFoundException("user_sub_group",
                                                                              USER_NOT_EXISTS_IN_SUB_GROUP));

        userSubGroupService.delete(userSubGroup);
    }

    private void validateAddMembers(final Long subGroupId, final Set<Long> memberIds) {
        if (!subGroupService.existsById(subGroupId)) {
            throw new ObjectNotFoundException("sub_group");
        }

        memberIds.forEach(id -> {
            if (!userService.existsById(id)) {
                throw new ObjectNotFoundException("user");
            }
        });
    }

    private void validateRemoveMember(final Long userId, final Long subGroupId, final Long curentUserId) {
        if (subGroupService.existsByIdAndManagerId(subGroupId, userId)) {
            throw new ValidatorException("Not allowed to remove the manager from the sub group",
                                         "manager_id",
                                         CAN_NOT_REMOVE_MANAGER_SUB_GROUP);
        }

        if (!SecurityHelper.isAdmin() && !userSubGroupService.existsBySubGroupIdAndUserId(subGroupId, curentUserId)) {
            throw new ValidatorException("Do not have permission to remove member of sub group",
                                         "current_user",
                                         NOT_PERMISSION_REMOVE_MEMBER_OF_SUB_GROUP);
        }
    }
}
