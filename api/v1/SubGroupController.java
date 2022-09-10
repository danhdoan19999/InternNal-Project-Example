package com.nals.rw360.api.v1;

import com.nals.rw360.bloc.v1.SubGroupBloc;
import com.nals.rw360.dto.v1.request.group.sub.AddMemberReq;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.Validator;

@RestController
@RequestMapping("/api/v1/sub-groups/{subGroupId}")
public class SubGroupController
    extends BaseController {

    private final SubGroupBloc subGroupBloc;

    public SubGroupController(final Validator validator, final SubGroupBloc subGroupBloc) {
        super(validator);
        this.subGroupBloc = subGroupBloc;
    }

    @PostMapping("/members")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_ACCOUNT_LEADER', 'ROLE_PMO')")
    public ResponseEntity<?> addMembers(@PathVariable(value = "subGroupId") final Long subGroupId,
                                        @Valid @RequestBody final AddMemberReq req) {
        subGroupBloc.addMembers(subGroupId, req);
        return noContent();
    }

    @DeleteMapping("/members/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_ACCOUNT_LEADER', 'ROLE_PMO')")
    public ResponseEntity<?> removeMember(@PathVariable(value = "userId") final Long userId,
                                          @PathVariable(value = "subGroupId") final Long subGroupId) {
        subGroupBloc.removeMember(userId, subGroupId);
        return noContent();
    }
}
