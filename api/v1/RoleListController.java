package com.nals.rw360.api.v1;

import com.nals.rw360.bloc.v1.RoleListBloc;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Validator;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleListController
    extends BaseController {

    private final RoleListBloc roleListBloc;

    public RoleListController(final Validator validator, final RoleListBloc roleListBloc) {
        super(validator);
        this.roleListBloc = roleListBloc;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<?> fetchAllRoles() {
        return ok(roleListBloc.fetchAllRoles());
    }
}
