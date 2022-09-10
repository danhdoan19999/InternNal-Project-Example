package com.nals.rw360.api.v1;

import com.nals.rw360.bloc.v1.SubGroupCrudBloc;
import com.nals.rw360.dto.v1.request.group.sub.SubGroupUpdateReq;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.Validator;

@RestController
@RequestMapping("/api/v1/sub-groups")
public class SubGroupUpdateController
    extends BaseController {

    private final SubGroupCrudBloc subGroupCrudBloc;

    public SubGroupUpdateController(final Validator validator, final SubGroupCrudBloc subGroupCrudBloc) {
        super(validator);
        this.subGroupCrudBloc = subGroupCrudBloc;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER','ROLE_ACCOUNT_LEADER','ROLE_PMO')")
    public ResponseEntity<?> updateSubGroup(@PathVariable final Long id,
                                            @Valid @RequestBody final SubGroupUpdateReq req) {
        subGroupCrudBloc.updateSubGroup(id, req);
        return noContent();
    }
}
