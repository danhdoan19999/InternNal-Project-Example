package com.nals.rw360.api.v1;

import com.nals.rw360.bloc.v1.UserCrudBloc;
import com.nals.rw360.dto.v1.request.user.ChangePasswordReq;
import com.nals.rw360.dto.v1.request.user.FinishResetPasswordReq;
import com.nals.rw360.dto.v1.request.user.ResendActivationKeyReq;
import com.nals.rw360.dto.v1.request.user.ResetPasswordReq;
import com.nals.rw360.dto.v1.request.user.UserActivateReq;
import com.nals.rw360.dto.v1.request.user.UserCreateReq;
import com.nals.rw360.dto.v1.request.user.UserUpdateReq;
import com.nals.rw360.dto.v1.request.user.VerifyKeyReq;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.Validator;

@RestController
@RequestMapping("/api/v1/users")
public class UserCrudController
    extends BaseController {

    private final UserCrudBloc userCrudBloc;

    public UserCrudController(final Validator validator, final UserCrudBloc userCrudBloc) {
        super(validator);
        this.userCrudBloc = userCrudBloc;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<?> createUser(@Valid @RequestBody final UserCreateReq req) {
        return created(userCrudBloc.createUser(req));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable final Long id, @Valid @RequestBody final UserUpdateReq req) {
        userCrudBloc.updateUser(id, req);
        return noContent();
    }

    @PostMapping("/activate")
    public ResponseEntity<?> activateUser(@Valid @RequestBody final UserActivateReq req) {
        userCrudBloc.activateUser(req);
        return noContent();
    }

    @PostMapping("/resend-activation-key")
    public ResponseEntity<?> resendActivationKey(@Valid @RequestBody final ResendActivationKeyReq req) {
        userCrudBloc.resendActivationKey(req.getEmail());
        return noContent();
    }

    @PostMapping("/reset-password/init")
    public ResponseEntity<?> requestResetPassword(@Valid @RequestBody final ResetPasswordReq req) {
        userCrudBloc.requestResetPassword(req);
        return noContent();
    }

    @PostMapping("/reset-password/finish")
    public ResponseEntity<?> finishResetPassword(@Valid @RequestBody final FinishResetPasswordReq req) {
        userCrudBloc.finishResetPassword(req);
        return noContent();
    }

    @PostMapping("/key-expired")
    public ResponseEntity<?> verifyExpiredKey(@Valid @RequestBody final VerifyKeyReq req) {
        return ok(userCrudBloc.verifyExpiredKey(req.getKey()));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody final ChangePasswordReq req) {
        userCrudBloc.changePassword(req);
        return noContent();
    }
}
