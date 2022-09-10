package com.nals.rw360.api.v1;

import com.nals.rw360.bloc.v1.UserListBloc;
import com.nals.rw360.dto.v1.request.group.sub.member.MemberSearchReq;
import com.nals.rw360.helpers.JsonHelper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Validator;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserListController
    extends BaseController {

    private final UserListBloc userListBloc;

    public UserListController(final Validator validator, final UserListBloc userListBloc) {
        super(validator);
        this.userListBloc = userListBloc;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<?> searchUsers(@RequestParam final Map<String, Object> reqParams) {
        MemberSearchReq req = JsonHelper.MAPPER.convertValue(reqParams, MemberSearchReq.class);
        return ok(userListBloc.searchUsers(req));
    }
}
