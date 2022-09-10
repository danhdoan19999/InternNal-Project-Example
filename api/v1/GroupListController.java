package com.nals.rw360.api.v1;

import com.nals.rw360.bloc.v1.GroupListBloc;
import com.nals.rw360.dto.v1.request.group.GroupSearchReq;
import com.nals.rw360.dto.v1.request.group.sub.member.MemberSearchReq;
import com.nals.rw360.helpers.JsonHelper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Validator;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/groups")
public class GroupListController
    extends BaseController {

    private final GroupListBloc groupListBloc;

    public GroupListController(final Validator validator, final GroupListBloc groupListBloc) {
        super(validator);
        this.groupListBloc = groupListBloc;
    }

    @GetMapping
    public ResponseEntity<?> searchGroups(@RequestParam final Map<String, Object> reqParams) {
        GroupSearchReq req = JsonHelper.MAPPER.convertValue(reqParams, GroupSearchReq.class);
        return ok(groupListBloc.searchGroups(req));
    }

    @GetMapping("/leaders")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<?> fetchAllGroupLeader() {
        return ok(groupListBloc.fetchAllGroupLeader());
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<?> searchMembers(@PathVariable("id") final Long id,
                                           @RequestParam final Map<String, Object> reqParams) {
        MemberSearchReq req = JsonHelper.MAPPER.convertValue(reqParams, MemberSearchReq.class);
        return ok(groupListBloc.searchMembers(id, req));
    }
}
