package com.nals.rw360.api.v1;

import com.nals.rw360.bloc.v1.UserCrudBloc;
import com.nals.rw360.dto.v1.request.user.ProfileUpdateReq;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.Validator;

@RestController
@RequestMapping("/api/v1/me")
public class ProfileController
    extends BaseController {

    private final UserCrudBloc userCrudBloc;

    public ProfileController(final Validator validator, final UserCrudBloc userCrudBloc) {
        super(validator);
        this.userCrudBloc = userCrudBloc;
    }

    @GetMapping
    public ResponseEntity<?> getProfile() {
        return ok(userCrudBloc.getProfile());
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(@Valid @RequestBody final ProfileUpdateReq req) {
        userCrudBloc.updateProfile(req);
        return noContent();
    }
}
