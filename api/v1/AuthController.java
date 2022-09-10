package com.nals.rw360.api.v1;

import com.nals.rw360.bloc.v1.AuthBloc;
import com.nals.rw360.dto.v1.request.auth.LoginReq;
import com.nals.rw360.dto.v1.request.auth.LoginSocialReq;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.Validator;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController
    extends BaseController {

    private final AuthBloc authBloc;

    public AuthController(final Validator validator, final AuthBloc authBloc) {
        super(validator);
        this.authBloc = authBloc;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid final @RequestBody LoginReq req) {
        return ok(authBloc.login(req));
    }

    @PostMapping("/login-social")
    public ResponseEntity<?> loginWithSocial(@Valid @RequestBody final LoginSocialReq req) {
        return ok(authBloc.loginWithSocial(req));
    }

    @GetMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestParam("refresh_token") final String token) {
        return ok(authBloc.refreshOAuthToken(token));
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(final HttpServletRequest request, final HttpServletResponse response) {
        authBloc.logout(request, response);
        return noContent();
    }
}
