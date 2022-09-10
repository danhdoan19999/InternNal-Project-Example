package com.nals.rw360.bloc.v1;

import com.nals.rw360.config.ApplicationProperties;
import com.nals.rw360.domain.Role;
import com.nals.rw360.domain.User;
import com.nals.rw360.domain.UserRole;
import com.nals.rw360.dto.v1.request.user.ChangePasswordReq;
import com.nals.rw360.dto.v1.request.user.FinishResetPasswordReq;
import com.nals.rw360.dto.v1.request.user.ProfileUpdateReq;
import com.nals.rw360.dto.v1.request.user.ResetPasswordReq;
import com.nals.rw360.dto.v1.request.user.UserActivateReq;
import com.nals.rw360.dto.v1.request.user.UserCreateReq;
import com.nals.rw360.dto.v1.request.user.UserUpdateReq;
import com.nals.rw360.dto.v1.response.KeyInfoRes;
import com.nals.rw360.dto.v1.response.user.ProfileRes;
import com.nals.rw360.enums.AuthProvider;
import com.nals.rw360.enums.MediaType;
import com.nals.rw360.errors.ErrorProblem;
import com.nals.rw360.errors.ObjectNotFoundException;
import com.nals.rw360.errors.ValidatorException;
import com.nals.rw360.helpers.CodeHelper;
import com.nals.rw360.helpers.DateHelper;
import com.nals.rw360.helpers.RandomHelper;
import com.nals.rw360.helpers.SecurityHelper;
import com.nals.rw360.helpers.StringHelper;
import com.nals.rw360.helpers.ValidatorHelper;
import com.nals.rw360.mapper.v1.UserMapper;
import com.nals.rw360.security.social.SocialUser;
import com.nals.rw360.service.v1.FileService;
import com.nals.rw360.service.v1.MailService;
import com.nals.rw360.service.v1.RoleService;
import com.nals.rw360.service.v1.UserRoleService;
import com.nals.rw360.service.v1.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.security.RandomUtil;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.nals.rw360.config.Constants.DEFAULT_LANG_KEY;
import static com.nals.rw360.config.Constants.PASSWORD_MIN_LENGTH;
import static com.nals.rw360.enums.RoleType.ROLE_MEMBER;
import static com.nals.rw360.errors.ErrorCodes.EMAIL_ALREADY_USED;
import static com.nals.rw360.errors.ErrorCodes.EXPIRED_KEY;
import static com.nals.rw360.errors.ErrorCodes.INVALID_AUTH_PROVIDER;
import static com.nals.rw360.errors.ErrorCodes.INVALID_EMAIL;
import static com.nals.rw360.errors.ErrorCodes.INVALID_OLD_PASSWORD;
import static com.nals.rw360.errors.ErrorCodes.INVALID_PASSWORD;
import static com.nals.rw360.errors.ErrorCodes.INVALID_PHONE;
import static com.nals.rw360.errors.ErrorCodes.LIMIT_NUMBER_OF_SEND_KEY;
import static com.nals.rw360.errors.ErrorCodes.PHONE_ALREADY_USED;
import static com.nals.rw360.helpers.StringHelper.isBlank;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserCrudBloc {

    private final CodeHelper codeHelper;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationProperties applicationProperties;
    private final RoleService roleService;
    private final MailService mailService;
    private final UserService userService;
    private final UserRoleService userRoleService;
    private final MediaCrudBloc mediaCrudBloc;
    private final FileService fileService;

    public ProfileRes getProfile() {
        Long currentUserId = SecurityHelper.getCurrentUserId();
        log.info("Get basic info of user has currentUserId #{}", currentUserId);

        User user = userService.getBasicInfoById(currentUserId);
        ProfileRes res = UserMapper.INSTANCE.toUserBasicInfoRes(user);
        res.setImageUrl(fileService.getFullFileUrl(user.getImageName()));

        return res;
    }

    public KeyInfoRes verifyExpiredKey(final String key) {
        log.info("Verify expired key");

        String email = userService.getEmailByKey(key);
        if (StringHelper.isBlank(email)) {
            return KeyInfoRes.builder()
                             .expired(true)
                             .build();
        }

        try {
            CodeHelper.Key keyInfo = codeHelper.getKeyInfo(key);
            return KeyInfoRes.builder()
                             .expired(false)
                             .email(keyInfo.getEmail())
                             .build();
        } catch (Exception ex) {
            log.error("Expired key");
            return KeyInfoRes.builder()
                             .email(email)
                             .expired(true)
                             .build();
        }
    }

    @Transactional
    public Long createUser(final UserCreateReq req) {
        log.info("Create user with data #{}", req);
        validateCreateUser(req);

        User user = createUser(req.getName(), req.getEmail(), req.getPassword());
        setRoleForUser(user.getId(), req.getRoleId());

        return user.getId();
    }

    @Transactional
    public User registerWithSocial(final AuthProvider provider, final SocialUser socialUser) {
        log.info("Register user by social #{} info #{}", provider, socialUser);

        String email = socialUser.getEmail();
        User user = userService.save(User.builder()
                                         .email(email)
                                         .username(email)
                                         .name(socialUser.getName())
                                         .imageName(socialUser.getPicture())
                                         .activated(true)
                                         .isFirstLogin(true)
                                         .langKey(DEFAULT_LANG_KEY)
                                         .build());

        createDefaultRoleForUser(user);

        return user;
    }

    @Transactional
    public void activateUser(final UserActivateReq req) {
        String key = req.getKey();
        log.info("Activating user by key #{}", key);

        User user = userService.getByActivationKey(key)
                               .orElseThrow(() -> new ObjectNotFoundException("key"));

        if (user.isActivated()) {
            return;
        }

        validateKeyAndPassword(key, req.getPassword());

        user.setActivated(true);
        user.setActivationKey(null);
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        userService.save(user);
    }

    @Transactional
    public void resendActivationKey(final String email) {
        log.info("Resend activation key to #{}", email);

        var user = userService.getByEmail(email)
                              .orElseThrow(() -> new ObjectNotFoundException("user"));

        if (user.isActivated()) {
            return;
        }

        CodeHelper.Key activationKey;

        try {
            activationKey = codeHelper.getKeyInfo(user.getActivationKey());
        } catch (Exception ex) {
            throw new ValidatorException("Expired key", "key", EXPIRED_KEY);
        }

        var currentTime = Instant.now();
        var sendNumber = calculateNumberOfSendKey(currentTime,
                                                  applicationProperties.getActivationKeyLimitTimeAmount(),
                                                  HOURS,
                                                  applicationProperties.getActivationKeyLimitNumber(),
                                                  activationKey);

        var expiredDate = currentTime.plus(applicationProperties.getActivationExpiredTime(), HOURS);

        activationKey.setNumberOfSendKey(sendNumber);
        activationKey.setExpiredDate(expiredDate);
        activationKey.setKey(RandomUtil.generateActivationKey());

        //For case reset resend number
        if (sendNumber == 1) {
            activationKey.setSendDate(currentTime);
        }

        user.setActivationKey(codeHelper.generateKey(activationKey));
        userService.save(user);

        mailService.sendRegisterEmail(user, ROLE_MEMBER);
    }

    @Transactional
    public void requestResetPassword(final ResetPasswordReq req) {
        var email = req.getEmail();
        log.info("Send request reset password by email #{}", email);

        var user = userService.getActivatedByEmail(email)
                              .orElseThrow(() -> new ObjectNotFoundException("email"));

        var currentTime = Instant.now();
        var expiredDate = currentTime.plus(applicationProperties.getResetPasswordExpiredTime(), MINUTES);

        if (Objects.isNull(user.getResetKey())) {
            user.setResetKey(generateKey(email, currentTime, expiredDate));
        } else {
            CodeHelper.Key resetKey;
            try {
                resetKey = codeHelper.getKeyInfo(user.getResetKey());
                var sendNumber = calculateNumberOfSendKey(currentTime,
                                                          applicationProperties.getResetPasswordKeyLimitTimeAmount(),
                                                          MINUTES,
                                                          applicationProperties.getResetPasswordKeyLimitNumber(),
                                                          resetKey);

                resetKey.setNumberOfSendKey(sendNumber);
                resetKey.setExpiredDate(expiredDate);
                resetKey.setKey(RandomUtil.generateActivationKey());

                //For case reset resend number
                if (sendNumber == 1) {
                    resetKey.setSendDate(currentTime);
                }

                user.setResetKey(codeHelper.generateKey(resetKey));
            } catch (Exception ex) {
                log.warn(ex.getMessage());
                user.setResetKey(generateKey(email, currentTime, expiredDate));
            }
        }

        userService.save(user);

        mailService.sendPasswordResetMail(user);
    }

    @Transactional
    public void finishResetPassword(final FinishResetPasswordReq req) {
        var resetKey = req.getKey();
        var password = req.getPassword();
        var user = userService.getByResetKey(resetKey)
                              .orElseThrow(() -> new ObjectNotFoundException("key"));

        log.info("Finish password reset by email #{}", user.getEmail());

        validateKeyAndPassword(resetKey, password);

        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setResetKey(null);
        userService.save(user);
    }

    @Transactional
    public void changePassword(final ChangePasswordReq req) {
        String newPassword = req.getNewPassword();
        String oldPassword = req.getOldPassword();
        var currentUser = SecurityHelper.getCurrentUserLogin();
        log.info("Change password user #{}", currentUser);

        var user = currentUser.flatMap(userService::getByUsername)
                              .orElseThrow(() -> new ObjectNotFoundException("user"));

        if (Objects.equals(newPassword, oldPassword)) {
            return;
        } else if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new ValidatorException("Invalid old password", "oldPassword", INVALID_OLD_PASSWORD);
        }

        List<ErrorProblem> errors = validatePassword(newPassword);
        if (CollectionUtils.isNotEmpty(errors)) {
            throw new ValidatorException(errors);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userService.save(user);
    }

    @Transactional
    public void updateUser(final Long id, final UserUpdateReq req) {
        log.info("Update user info by id #{} with data #{}", id, req);
        User user = userService.getCurrentUser();
        user.setIsFirstLogin(req.getIsFirstLogin());
        userService.save(user);
    }

    @Transactional
    public void updateProfile(final ProfileUpdateReq req) {
        Long currentUserId = SecurityHelper.getCurrentUserId();
        log.info("Update profile of user #{}", currentUserId);
        validateBeforeUpdateProfile(currentUserId, req);

        User user = userService.getById(currentUserId, "user");

        String oldImageName = user.getImageName();
        String newImageName = req.getImageName();

        userService.save(UserMapper.INSTANCE.toUser(req, user));

        mediaCrudBloc.replaceMedia(newImageName, oldImageName, currentUserId, MediaType.USER_AVATAR);
    }

    private User createUser(final String name, final String email, final String password) {

        return userService.save(User.builder()
                                    .name(name)
                                    .username(email)
                                    .password(passwordEncoder.encode(password))
                                    .email(email)
                                    .langKey(DEFAULT_LANG_KEY)
                                    .activated(true)
                                    .isFirstLogin(true)
                                    .build());
    }

    private String generateKey(final String email, final Instant currentTime, final Instant expiredDate) {
        return codeHelper.generateKey(
            CodeHelper.Key.builder()
                          .email(email)
                          .key(RandomHelper.generateRandomAlphanumericString())
                          .numberOfSendKey(1)
                          .sendDate(DateHelper.truncatedToSecond(currentTime))
                          .expiredDate(DateHelper.truncatedToSecond(expiredDate))
                          .build());
    }

    private void validateCreateUser(final UserCreateReq req) {
        List<ErrorProblem> errors = validatePassword(req.getPassword());

        if (!ValidatorHelper.isValidEmail(req.getEmail())) {
            throw new ValidatorException("Invalid email", "email", INVALID_EMAIL);
        }

        if (userService.existsUserByEmail(req.getEmail())) {
            throw new ValidatorException("Email already used", "email", EMAIL_ALREADY_USED);
        }
        if (!errors.isEmpty()) {
            throw new ValidatorException(errors);
        }
    }

    private void validateKeyAndPassword(final String key, final String password) {
        List<ErrorProblem> errors = validatePassword(password);

        try {
            codeHelper.getKeyInfo(key);
        } catch (Exception ex) {
            log.error(ex.getMessage());

            var problem = new ErrorProblem();
            problem.setField("key");
            problem.setMessage("Expired key");
            problem.setErrorCode(EXPIRED_KEY);

            errors.add(problem);
        }

        if (CollectionUtils.isNotEmpty(errors)) {
            throw new ValidatorException(errors);
        }
    }

    private int calculateNumberOfSendKey(final Instant currentTime,
                                         final int timeAmount,
                                         final ChronoUnit timeUnit,
                                         final int limitNumber,
                                         final CodeHelper.Key key) {
        if (key.getSendDate().plus(timeAmount, timeUnit).isBefore(currentTime)) {
            return 1;
        }

        int numberOfSendKey = key.getNumberOfSendKey() + 1;

        if (numberOfSendKey > limitNumber) {
            throw new ValidatorException(String.format("Limit %s times of resending key per hour", limitNumber),
                                         "", LIMIT_NUMBER_OF_SEND_KEY);
        }

        return numberOfSendKey;
    }

    public void linkedSocialIdToUser(final AuthProvider provider, final String socialId, final User user) {
        switch (provider) {
            case GOOGLE:
                if (isBlank(user.getGoogleId())) {
                    user.setGoogleId(socialId);
                    userService.save(user);
                }
                break;
            default:
                throw new ValidatorException("Auth provider not support", "provider", INVALID_AUTH_PROVIDER);
        }
    }

    private void createDefaultRoleForUser(final User user) {
        Role role = roleService.getByName(ROLE_MEMBER.name())
                               .orElseThrow(() -> new ObjectNotFoundException("role"));

        userRoleService.save(UserRole.builder()
                                     .userId(user.getId())
                                     .roleId(role.getId())
                                     .build());
    }

    private void setRoleForUser(final Long userId, final Long roleId) {
        Role role = roleService.getById(roleId)
                               .orElseThrow(() -> new ObjectNotFoundException("role"));

        UserRole userRole = UserRole.builder()
                                    .roleId(role.getId())
                                    .userId(userId)
                                    .build();

        userRoleService.save(userRole);
    }

    private List<ErrorProblem> validatePassword(final String password) {
        List<ErrorProblem> errors = new ArrayList<>();

        if (!ValidatorHelper.isValidPassword(password)) {
            var problem = new ErrorProblem();
            problem.setField("password");
            problem.setMessage(String.format("Password must be at least %s characters", PASSWORD_MIN_LENGTH));
            problem.setErrorCode(INVALID_PASSWORD);

            errors.add(problem);
        }

        return errors;
    }

    private void validateBeforeUpdateProfile(final Long id, final ProfileUpdateReq req) {
        if (!ValidatorHelper.isValidPhone(req.getPhone())) {
            throw new ValidatorException("Invalid phone number", "phone", INVALID_PHONE);
        }

        if (userService.existsUserByPhone(req.getPhone(), id)) {
            throw new ValidatorException("Phone number already used", "phone", PHONE_ALREADY_USED);
        }
    }
}
