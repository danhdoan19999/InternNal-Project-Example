package com.nals.rw360.bloc.v1;

import com.nals.rw360.config.ApplicationProperties;
import com.nals.rw360.domain.Permission;
import com.nals.rw360.domain.Role;
import com.nals.rw360.domain.User;
import com.nals.rw360.dto.v1.request.auth.LoginReq;
import com.nals.rw360.dto.v1.request.auth.LoginSocialReq;
import com.nals.rw360.dto.v1.response.OAuthTokenRes;
import com.nals.rw360.dto.v1.response.user.ProfileRes;
import com.nals.rw360.enums.AuthProvider;
import com.nals.rw360.errors.InternalServerErrorException;
import com.nals.rw360.errors.InvalidCredentialException;
import com.nals.rw360.errors.InvalidTokenException;
import com.nals.rw360.errors.InvalidUserSiteException;
import com.nals.rw360.errors.ValidatorException;
import com.nals.rw360.helpers.SecurityHelper;
import com.nals.rw360.helpers.StringHelper;
import com.nals.rw360.mapper.v1.UserMapper;
import com.nals.rw360.security.DomainUserDetails;
import com.nals.rw360.security.UserLockedException;
import com.nals.rw360.security.jwt.TokenProvider;
import com.nals.rw360.security.social.SocialTemplateFactory;
import com.nals.rw360.security.social.SocialUser;
import com.nals.rw360.service.v1.FileService;
import com.nals.rw360.service.v1.PermissionService;
import com.nals.rw360.service.v1.RoleService;
import com.nals.rw360.service.v1.UserService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.nals.rw360.enums.AuthProvider.GOOGLE;
import static com.nals.rw360.errors.ErrorCodes.INVALID_AUTH_PROVIDER;
import static com.nals.rw360.errors.ErrorCodes.INVALID_EMAIL_COMPANY;
import static com.nals.rw360.helpers.StringHelper.isBlank;
import static com.nals.rw360.security.jwt.TokenProvider.ACCESS_TOKEN_ID;
import static com.nals.rw360.security.jwt.TokenProvider.PERMISSIONS_KEY;
import static com.nals.rw360.security.jwt.TokenProvider.ROLES_KEY;
import static com.nals.rw360.security.jwt.TokenProvider.USER_ID_KEY;
import static java.lang.Boolean.TRUE;
import static java.time.temporal.ChronoUnit.MINUTES;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthBloc {

    private static final String EMAIL_NAL_PATTERN = "@nal.vn";
    private final FileService fileService;
    private final UserService userService;
    private final TokenProvider tokenProvider;
    private final ApplicationProperties properties;
    private final UserCrudBloc userCrudBloc;
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    @Transactional(noRollbackFor = {BadCredentialsException.class,
                                    InvalidCredentialException.class,
                                    UserLockedException.class})
    public OAuthTokenRes login(final LoginReq req) {
        log.info("Login with user {}", req.getUsername());

        String username = req.getUsername();
        handleUserLocked(username);

        try {
            var domainUserDetails = authenticate(username, req);
            return handleLoginSuccess(domainUserDetails);
        } catch (BadCredentialsException e) {
            handleBadCredential(username);
        }

        throw new InternalServerErrorException("Oops, something went wrong when trying to login");
    }

    @Transactional
    public OAuthTokenRes loginWithSocial(final LoginSocialReq req) {
        AuthProvider provider = req.getProvider();
        log.info("Login social with #{}", provider);

        if (GOOGLE != provider) {
            throw new ValidatorException("Auth provider not support", "provider", INVALID_AUTH_PROVIDER);
        }

        SocialUser socialUser = getUserInfo(req);
        String socialId = socialUser.getSub();
        String socialEmail = socialUser.getEmail();

        if (isBlank(socialEmail)) {
            throw new ValidatorException("Missing social email", "", INVALID_AUTH_PROVIDER);
        }

        if (!isEmailCompany(socialEmail)) {
            throw new ValidatorException("Email does not belong to the company", "email", INVALID_EMAIL_COMPANY);
        }

        User user = userService.getBySocialInfo(socialId, socialEmail, provider).orElse(null);
        if (Objects.isNull(user)) {
            user = userCrudBloc.registerWithSocial(provider, socialUser);
        } else if (!user.isActivated()) {
            // Auto activated account if login via social
            user.setActivated(TRUE);
            userService.save(user);
        }

        userCrudBloc.linkedSocialIdToUser(provider, socialId, user);

        Long userId = user.getId();
        String authorities = roleService.fetchByUserId(userId)
                                        .stream()
                                        .map(Role::getName)
                                        .collect(Collectors.joining(StringHelper.COMMA));

        String permissions = permissionService.fetchByUserId(userId)
                                              .stream()
                                              .map(Permission::getName)
                                              .collect(Collectors.joining(StringHelper.COMMA));

        return createOAuthTokenRes(userId, socialEmail, authorities, permissions);
    }

    public void logout(final HttpServletRequest request, final HttpServletResponse response) {
        Long currentUserId = SecurityHelper.getCurrentUserId();
        log.info("Logout for currentUserId #{}", currentUserId);

        // Clear security context
        SecurityContextLogoutHandler contextHandler = new SecurityContextLogoutHandler();
        contextHandler.logout(request, response, null);
    }

    public OAuthTokenRes refreshOAuthToken(final String refreshToken) {
        Claims tokenClaims = tokenProvider.validateAndGet(refreshToken);
        if (Objects.isNull(tokenClaims) || !tokenClaims.containsKey(ACCESS_TOKEN_ID)) {
            throw new InvalidTokenException();
        }

        String principalName = tokenClaims.getSubject();
        log.info("Refresh OAuth token for user: {}", principalName);

        String roles = tokenClaims.get(ROLES_KEY, String.class);
        if (SecurityHelper.isSuperUser(roles.split(StringHelper.COMMA))) {
            throw new InvalidUserSiteException();
        }

        Long userId = tokenClaims.get(USER_ID_KEY, Long.class);
        String permissions = tokenClaims.get(PERMISSIONS_KEY, String.class);

        return createOAuthTokenRes(userId, principalName, roles, permissions);
    }

    private void handleUserLocked(final String username) {
        userService.getByUsername(username).ifPresent(user -> {
            if (Objects.nonNull(user.getLockedDate())) {
                ApplicationProperties.Authentication authenticationConfig = properties.getAuthentication();
                Instant unlockTime = user.getLockedDate().plus(authenticationConfig.getLockedTimeInMinutes(), MINUTES);
                if (Instant.now().isBefore(unlockTime)) {
                    throw new UserLockedException(authenticationConfig.getLimitTryNumber(), unlockTime);
                }

                user.setLockedDate(null);
                user.setRemainTryNumber(null);
                userService.save(user);
            }
        });
    }

    private void handleBadCredential(final String username) {
        ApplicationProperties.Authentication authenticationConfig = properties.getAuthentication();

        Integer remainTryNumber = null;
        int limitTryNumber = authenticationConfig.getLimitTryNumber();

        User user = userService.getByUsername(username).orElse(null);
        if (Objects.nonNull(user)) {
            Instant unlockTime = null;

            remainTryNumber = Optional.ofNullable(user.getRemainTryNumber()).orElse(limitTryNumber) - 1;
            if (remainTryNumber <= 0) {
                remainTryNumber = 0;
                Instant now = Instant.now().truncatedTo(MINUTES);
                user.setLockedDate(now);
                unlockTime = now.plus(authenticationConfig.getLockedTimeInMinutes(), MINUTES);
            }

            user.setRemainTryNumber(remainTryNumber);
            userService.save(user);

            if (Objects.nonNull(unlockTime)) {
                throw new UserLockedException(limitTryNumber, unlockTime);
            }
        }

        throw new InvalidCredentialException(limitTryNumber, remainTryNumber);
    }

    private OAuthTokenRes createOAuthTokenRes(final Long userId,
                                              final String username,
                                              final String roles,
                                              final String permissions) {

        TokenProvider.OAuthToken oauthToken = tokenProvider.createOAuthToken(userId, username,
                                                                             roles, permissions);

        User user = userService.getBasicInfoById(userId);
        ProfileRes userRes = UserMapper.INSTANCE.toUserBasicInfoRes(user);
        userRes.setImageUrl(fileService.getFullFileUrl(userRes.getImageName()));

        return OAuthTokenRes.builder()
                            .expiredIn(oauthToken.getAccessToken().getExpiredIn())
                            .accessToken(oauthToken.getAccessToken().getValue())
                            .refreshToken(oauthToken.getRefreshToken().getValue())
                            .roles(Set.of(roles.split(StringHelper.COMMA)))
                            .permissions(Set.of(permissions.split(StringHelper.COMMA)))
                            .userInfo(userRes)
                            .build();
    }

    private SocialUser getUserInfo(final LoginSocialReq req) {
        SocialTemplateFactory socialTemplateFactory = SocialTemplateFactory.builder()
                                                                           .socialConfig(properties.getSocial())
                                                                           .accessToken(req.getAccessToken())
                                                                           .build();

        return socialTemplateFactory.getSocialTemplate(req.getProvider()).getUserInfo();
    }

    private DomainUserDetails authenticate(final String username, final LoginReq req) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(username, req.getPassword());
        var authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        return (DomainUserDetails) authentication.getPrincipal();
    }

    private OAuthTokenRes handleLoginSuccess(final DomainUserDetails domainUserDetails) {
        var roles = domainUserDetails.getRoles()
                                     .stream()
                                     .map(GrantedAuthority::getAuthority)
                                     .collect(Collectors.joining(StringHelper.COMMA));

        var permissions = domainUserDetails.getAuthorities()
                                           .stream()
                                           .map(GrantedAuthority::getAuthority)
                                           .collect(Collectors.joining(StringHelper.COMMA));

        var username = domainUserDetails.getUsername();

        userService.refreshLoginSuccessInfo(username);

        return createOAuthTokenRes(domainUserDetails.getId(), username, roles, permissions);
    }

    public static boolean isEmailCompany(final String str) {
        return str.endsWith(EMAIL_NAL_PATTERN);
    }
}
