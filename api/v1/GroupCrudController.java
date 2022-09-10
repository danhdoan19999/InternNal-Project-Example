package com.nals.rw360.api.v1;

import com.nals.rw360.bloc.v1.GroupCrudBloc;
import com.nals.rw360.dto.v1.request.group.GroupCreateReq;
import com.nals.rw360.dto.v1.request.group.GroupUpdateReq;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.Validator;

@RestController
@RequestMapping("/api/v1/groups")
public class GroupCrudController
    extends BaseController {

    private final GroupCrudBloc groupCrudBloc;

    public GroupCrudController(final Validator validator, final GroupCrudBloc groupCrudBloc) {
        super(validator);
        this.groupCrudBloc = groupCrudBloc;
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<?> updateGroup(@PathVariable final Long id, @Valid @RequestBody final GroupUpdateReq req) {
        groupCrudBloc.updateGroup(id, req);
        return noContent();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')")
    public ResponseEntity<?> createGroup(@Valid @RequestBody final GroupCreateReq req) {
        return created(groupCrudBloc.createGroup(req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getGroupDetail(@PathVariable final Long id) {
        return ok(groupCrudBloc.getGroupDetail(id));
    }
}
