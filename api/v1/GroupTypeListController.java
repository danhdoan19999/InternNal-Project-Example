package com.nals.rw360.api.v1;

import com.nals.rw360.bloc.v1.GroupTypeListBloc;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Validator;

@RestController
@RequestMapping("/api/v1/group-types")
public class GroupTypeListController
    extends BaseController {

    private final GroupTypeListBloc groupTypeListBloc;

    public GroupTypeListController(final Validator validator, final GroupTypeListBloc groupTypeListBloc) {
        super(validator);
        this.groupTypeListBloc = groupTypeListBloc;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_ACCOUNT_LEADER', 'ROLE_PMO')")
    public ResponseEntity<?> fetchAllGroupType() {
        return ok(groupTypeListBloc.fetchAllGroupType());
    }
}
