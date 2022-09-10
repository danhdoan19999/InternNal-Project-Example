package com.nals.rw360.api.v1;

import com.nals.rw360.bloc.v1.SubGroupCrudBloc;
import com.nals.rw360.dto.v1.request.group.sub.SubGroupCreateReq;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.Validator;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/sub-groups")
public class SubGroupCrudController
    extends BaseController {

    private final SubGroupCrudBloc subGroupCrudBloc;

    public SubGroupCrudController(final Validator validator, final SubGroupCrudBloc subGroupCrudBloc) {
        super(validator);
        this.subGroupCrudBloc = subGroupCrudBloc;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_ACCOUNT_LEADER', 'ROLE_PMO')")
    public ResponseEntity<?> createSubGroup(@Valid @RequestBody final SubGroupCreateReq req,
                                            @PathVariable(value = "groupId") final Long groupId) {
        return created(subGroupCrudBloc.createSubGroup(req, groupId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSubGroupDetail(@PathVariable final Long id) {
        return ok(subGroupCrudBloc.getSubGroupDetail(id));
    }

    @GetMapping("/{id}/available-members")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_ACCOUNT_LEADER', 'ROLE_PMO')")
    public ResponseEntity<?> fetchAllAvailableMembers(@PathVariable("groupId") final Long groupId,
                                                      @PathVariable("id") final Long id) {
        return ok(subGroupCrudBloc.fetchAllMembersNotExistsInSubGroup(groupId, id));
    }
}
