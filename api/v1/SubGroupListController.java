package com.nals.rw360.api.v1;

import com.nals.rw360.bloc.v1.SubGroupListBloc;
import com.nals.rw360.dto.v1.request.group.sub.SubGroupSearchReq;
import com.nals.rw360.dto.v1.request.group.sub.member.MemberSearchReq;
import com.nals.rw360.helpers.JsonHelper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Validator;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/sub-groups")
public class SubGroupListController
    extends BaseController {

    private final SubGroupListBloc subGroupListBloc;

    public SubGroupListController(final Validator validator, final SubGroupListBloc subGroupListBloc) {
        super(validator);
        this.subGroupListBloc = subGroupListBloc;
    }

    @GetMapping("/{subGroupId}/members")
    public ResponseEntity<?> searchMembers(@PathVariable final Long groupId,
                                           @PathVariable final Long subGroupId,
                                           @RequestParam final Map<String, Object> reqParams) {
        MemberSearchReq req = JsonHelper.MAPPER.convertValue(reqParams, MemberSearchReq.class);
        return ok(subGroupListBloc.searchMembers(groupId, subGroupId, req));
    }

    @GetMapping
    public ResponseEntity<?> searchSubGroups(@PathVariable(value = "groupId") final Long groupId,
                                             @RequestParam final Map<String, Object> reqParams) {
        SubGroupSearchReq req = JsonHelper.MAPPER.convertValue(reqParams, SubGroupSearchReq.class);
        return ok(subGroupListBloc.searchSubGroups(groupId, req));
    }
}
