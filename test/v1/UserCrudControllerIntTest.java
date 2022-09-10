package com.nals.rw360.v1;

import com.nals.rw360.AbstractTest;
import com.nals.rw360.Review360App;
import com.nals.rw360.api.v1.UserCrudController;
import com.nals.rw360.blueprints.RoleBlueprint;
import com.nals.rw360.blueprints.UserBlueprint;
import com.nals.rw360.blueprints.request.ProfileUpdateReqBlueprint;
import com.nals.rw360.blueprints.request.UserCreateReqBlueprint;
import com.nals.rw360.config.ApplicationProperties;
import com.nals.rw360.domain.Permission;
import com.nals.rw360.domain.Role;
import com.nals.rw360.domain.RolePermission;
import com.nals.rw360.domain.User;
import com.nals.rw360.dto.v1.request.user.ChangePasswordReq;
import com.nals.rw360.dto.v1.request.user.FinishResetPasswordReq;
import com.nals.rw360.dto.v1.request.user.ResendActivationKeyReq;
import com.nals.rw360.dto.v1.request.user.ResetPasswordReq;
import com.nals.rw360.dto.v1.request.user.UserActivateReq;
import com.nals.rw360.dto.v1.request.user.UserCreateReq;
import com.nals.rw360.dto.v1.request.user.UserUpdateReq;
import com.nals.rw360.dto.v1.request.user.VerifyKeyReq;
import com.nals.rw360.dto.v1.response.DataRes;
import com.nals.rw360.helpers.CodeHelper;
import com.nals.rw360.helpers.DateHelper;
import com.nals.rw360.helpers.JsonHelper;
import com.nals.rw360.helpers.RandomHelper;
import com.nals.rw360.helpers.StringHelper;
import com.nals.rw360.helpers.TestHelper;
import com.tobedevoured.modelcitizen.CreateModelException;
import com.tobedevoured.modelcitizen.RegisterBlueprintException;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.security.RandomUtil;

import java.time.Instant;

import static com.nals.rw360.config.Constants.DEFAULT_LANG_KEY;
import static com.nals.rw360.config.Constants.PASSWORD_MIN_LENGTH;
import static com.nals.rw360.enums.RoleType.ROLE_ADMIN;
import static com.nals.rw360.enums.RoleType.ROLE_MANAGER;
import static com.nals.rw360.enums.RoleType.ROLE_MEMBER;
import static com.nals.rw360.errors.ErrorCodes.EMAIL_ALREADY_USED;
import static com.nals.rw360.errors.ErrorCodes.EXPIRED_KEY;
import static com.nals.rw360.errors.ErrorCodes.INVALID_EMAIL;
import static com.nals.rw360.errors.ErrorCodes.INVALID_OLD_PASSWORD;
import static com.nals.rw360.errors.ErrorCodes.INVALID_PASSWORD;
import static com.nals.rw360.errors.ErrorCodes.LIMIT_NUMBER_OF_SEND_KEY;
import static com.nals.rw360.errors.ErrorCodes.OBJECT_NOT_FOUND;
import static com.nals.rw360.helpers.TestHelper.APPLICATION_JSON_UTF8;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Review360App.class)
public class UserCrudControllerIntTest
    extends AbstractTest {

    private static final String EMAIL = "user@gmail.com";

    @Autowired
    private UserCrudController userCrudController;

    @Autowired
    private CodeHelper codeHelper;

    @Autowired
    private ApplicationProperties applicationProperties;

    private MockMvc restMvc;
    private String baseUrl;

    @Before
    public void setup()
        throws RegisterBlueprintException, CreateModelException {
        this.restMvc = MockMvcBuilders.standaloneSetup(userCrudController)
                                      .setMessageConverters(getHttpMessageConverters())
                                      .setControllerAdvice(getExceptionTranslator())
                                      .build();

        this.baseUrl = "/api/v1/users";

        fakeData();
    }

    @Test
    @Transactional
    public void test_createUser_shouldBeOk()
        throws Exception {
        Role role = initRole();
        mockAuthentication(initUser(null, true), ROLE_MANAGER.name());
        UserCreateReq dto = initUserCreateReq();
        dto.setRoleId(role.getId());

        MvcResult mvcResult = restMvc.perform(MockMvcRequestBuilders.post(baseUrl)
                                                                    .contentType(APPLICATION_JSON_UTF8)
                                                                    .content(TestHelper.convertObjectToJsonBytes(dto)))
                                     .andExpect(MockMvcResultMatchers.status().isCreated())
                                     .andReturn();

        DataRes response = JsonHelper.readValue(mvcResult.getResponse().getContentAsString(), DataRes.class);

        Long userId = StringHelper.toNumber(response.getData().toString(), Long.class);
        User user = getUserRepository().findOneById(userId).orElse(null);

        Assertions.assertThat(user).isNotNull();
        Assertions.assertThat(user.getName()).isEqualTo(dto.getName());
        Assertions.assertThat(user.getEmail()).isEqualTo(dto.getEmail());
        Assertions.assertThat(user.getLangKey()).isEqualTo(DEFAULT_LANG_KEY);
        Assertions.assertThat(user.isActivated()).isTrue();

        Assertions.assertThat(getUserRoleRepository().findAllByUserId(userId)).hasSize(1);
    }

    @Test
    @Transactional
    public void test_createUser_emailIsInvalid_shouldBadRequest()
        throws Exception {
        mockAuthentication(initUser(null, true), ROLE_ADMIN.name());
        UserCreateReq dto = initUserCreateReq();
        dto.setEmail("invalidEmail@.com");

        restMvc.perform(MockMvcRequestBuilders.post(baseUrl)
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(dto)))
               .andExpect(MockMvcResultMatchers.status().isBadRequest())
               .andExpect(matchJsonPath("$.errors[0].error_code", INVALID_EMAIL))
               .andExpect(matchJsonPath("$.errors[0].field", "email"))
               .andExpect(matchJsonPath("$.errors[0].message", "Invalid email"));
    }

    @Test
    @Transactional
    public void test_createUser_emailAlreadyUsed_shouldBadRequest()
        throws Exception {
        mockAuthentication(initUser(null, true), ROLE_ADMIN.name());
        String existedEmail = "existedEmail@gmail.com";

        User user = createFakeModel(User.class);
        user.setEmail(existedEmail);
        getUserRepository().save(user);

        UserCreateReq dto = initUserCreateReq();
        dto.setEmail(user.getEmail());

        restMvc.perform(MockMvcRequestBuilders.post(baseUrl)
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(dto)))
               .andExpect(MockMvcResultMatchers.status().isBadRequest())
               .andExpect(matchJsonPath("$.errors[0].error_code", EMAIL_ALREADY_USED))
               .andExpect(matchJsonPath("$.errors[0].field", "email"))
               .andExpect(matchJsonPath("$.errors[0].message", "Email already used"));
    }

    @Test
    @Transactional
    public void test_activateUser_shouldBeOk()
        throws Exception {
        var currentTime = Instant.now();
        var activationKey = generateKey(EMAIL, 1,
                                        currentTime, currentTime.plus(24, HOURS));

        var user = initUser(codeHelper.generateKey(activationKey), false);

        var dto = UserActivateReq.builder()
                                 .key(user.getActivationKey())
                                 .password("activatedUser@123")
                                 .build();

        restMvc.perform(MockMvcRequestBuilders.post(baseUrl + "/activate")
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(dto)))
               .andExpect(MockMvcResultMatchers.status().isNoContent());

        User activatedUser = getUserRepository().findById(user.getId()).orElse(null);
        Assertions.assertThat(activatedUser).isNotNull();
        Assertions.assertThat(activatedUser.isActivated()).isTrue();
        Assertions.assertThat(activatedUser.getActivationKey()).isNull();
    }

    @Test
    @Transactional
    public void test_activateUser_keyIsExpired_shouldBeBadRequest()
        throws Exception {
        var currentTime = Instant.now();
        var activationKey = generateKey(EMAIL, 1,
                                        currentTime.minus(26, HOURS),
                                        currentTime.minus(25, HOURS));

        var user = initUser(codeHelper.generateKey(activationKey), false);

        var dto = UserActivateReq.builder()
                                 .key(user.getActivationKey())
                                 .password("activatedUser@123")
                                 .build();

        restMvc.perform(MockMvcRequestBuilders.post(baseUrl + "/activate")
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(dto)))
               .andExpect(MockMvcResultMatchers.status().isBadRequest())
               .andExpect(matchJsonPath("$.errors[0].error_code", EXPIRED_KEY))
               .andExpect(matchJsonPath("$.errors[0].field", "key"))
               .andExpect(matchJsonPath("$.errors[0].message", "Expired key"));
    }

    @Test
    @Transactional
    public void test_activateUser_passwordLengthLessThanMinLength_shouldBeBadRequest()
        throws Exception {
        var currentTime = Instant.now();
        var activationKey = generateKey(EMAIL, 1,
                                        currentTime, currentTime.plus(24, HOURS));

        var user = initUser(codeHelper.generateKey(activationKey), false);

        var dto = UserActivateReq.builder()
                                 .key(user.getActivationKey())
                                 .password("User@12")
                                 .build();

        restMvc.perform(MockMvcRequestBuilders.post(baseUrl + "/activate")
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(dto)))
               .andExpect(MockMvcResultMatchers.status().isBadRequest())
               .andExpect(matchJsonPath("$.errors[0].error_code", INVALID_PASSWORD))
               .andExpect(matchJsonPath("$.errors[0].field", "password"))
               .andExpect(matchJsonPath("$.errors[0].message",
                                        String.format("Password must be at least %s characters", PASSWORD_MIN_LENGTH)));
    }

    @Test
    @Transactional
    public void test_resendActivationKey_shouldBeOk()
        throws Exception {
        var currentTime = Instant.now();
        var activationKey = generateKey(EMAIL, 1,
                                        currentTime, currentTime.plus(23, HOURS)
                                                                .plus(10, MINUTES));
        var user = initUser(codeHelper.generateKey(activationKey), false);

        var oldActivationKey = user.getActivationKey();

        var dto = ResendActivationKeyReq.builder()
                                        .email(EMAIL)
                                        .build();

        restMvc.perform(MockMvcRequestBuilders.post(baseUrl + "/resend-activation-key")
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(dto)))
               .andExpect(MockMvcResultMatchers.status().isNoContent());

        var inactiveUser = getUserRepository().findById(user.getId()).orElse(null);
        Assertions.assertThat(inactiveUser).isNotNull();

        var newActivationKey = codeHelper.getKeyInfo(inactiveUser.getActivationKey());
        Assertions.assertThat(inactiveUser.getActivationKey()).isNotEqualTo(oldActivationKey);
        Assertions.assertThat(newActivationKey.getExpiredDate()).isNotEqualTo(activationKey.getExpiredDate());
        Assertions.assertThat(newActivationKey.getSendDate()).isEqualTo(activationKey.getSendDate());
        Assertions.assertThat(newActivationKey.getNumberOfSendKey()).isEqualTo(activationKey.getNumberOfSendKey() + 1);
    }

    @Test
    @Transactional
    public void test_resendActivationKey_equalThanLimitNumberWithinOneHours_shouldBeOk()
        throws Exception {
        var currentTime = Instant.now();
        var sendActivationKeyNumber = applicationProperties.getActivationKeyLimitNumber() - 1;
        var activationKey = generateKey(EMAIL, sendActivationKeyNumber,
                                        currentTime, currentTime.plus(23, HOURS)
                                                                .plus(10, MINUTES));
        var user = initUser(codeHelper.generateKey(activationKey), false);

        var oldActivationKey = user.getActivationKey();

        var dto = ResendActivationKeyReq.builder()
                                        .email(EMAIL)
                                        .build();

        restMvc.perform(MockMvcRequestBuilders.post(baseUrl + "/resend-activation-key")
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(dto)))
               .andExpect(MockMvcResultMatchers.status().isNoContent());

        var inactiveUser = getUserRepository().findById(user.getId()).orElse(null);
        Assertions.assertThat(inactiveUser).isNotNull();

        var newActivationKey = codeHelper.getKeyInfo(inactiveUser.getActivationKey());
        Assertions.assertThat(inactiveUser.getActivationKey()).isNotEqualTo(oldActivationKey);
        Assertions.assertThat(newActivationKey.getExpiredDate()).isNotEqualTo(activationKey.getExpiredDate());
        Assertions.assertThat(newActivationKey.getSendDate()).isEqualTo(activationKey.getSendDate());
        Assertions.assertThat(newActivationKey.getNumberOfSendKey()).isEqualTo(activationKey.getNumberOfSendKey() + 1);
    }

    @Test
    @Transactional
    public void test_resendActivationKey_afterOneHour_shouldBeOk()
        throws Exception {
        var currentTime = Instant.now();
        var sendActivationKeyNumber = applicationProperties.getActivationKeyLimitNumber();

        var activationKey = generateKey(EMAIL, sendActivationKeyNumber,
                                        currentTime.minus(3, HOURS),
                                        currentTime.plus(18, HOURS));
        var user = initUser(codeHelper.generateKey(activationKey), false);

        var oldActivationKey = user.getActivationKey();

        var dto = ResendActivationKeyReq.builder()
                                        .email(EMAIL)
                                        .build();

        restMvc.perform(MockMvcRequestBuilders.post(baseUrl + "/resend-activation-key")
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(dto)))
               .andExpect(MockMvcResultMatchers.status().isNoContent());

        var inactiveUser = getUserRepository().findById(user.getId()).orElse(null);
        Assertions.assertThat(inactiveUser).isNotNull();

        var newActivationKey = codeHelper.getKeyInfo(inactiveUser.getActivationKey());
        Assertions.assertThat(inactiveUser.getActivationKey()).isNotEqualTo(oldActivationKey);
        Assertions.assertThat(newActivationKey.getExpiredDate()).isNotEqualTo(activationKey.getExpiredDate());
        Assertions.assertThat(newActivationKey.getSendDate()).isNotEqualTo(activationKey.getSendDate());
        Assertions.assertThat(newActivationKey.getNumberOfSendKey()).isEqualTo(1);
    }

    @Test
    @Transactional
    public void test_resendActivationKey_greaterThanLimitNumberWithinOneHours_shouldBeBadRequest()
        throws Exception {
        var currentTime = Instant.now();
        var sendNumber = applicationProperties.getActivationKeyLimitNumber() + 1;
        var activationKey = generateKey(EMAIL, sendNumber, currentTime,
                                        currentTime.plus(24, HOURS));
        initUser(codeHelper.generateKey(activationKey), false);

        var dto = ResendActivationKeyReq.builder()
                                        .email(EMAIL)
                                        .build();

        restMvc.perform(MockMvcRequestBuilders.post(baseUrl + "/resend-activation-key")
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(dto)))
               .andExpect(MockMvcResultMatchers.status().isBadRequest())
               .andExpect(matchJsonPath("$.errors[0].error_code", LIMIT_NUMBER_OF_SEND_KEY))
               .andExpect(matchJsonPath("$.errors[0].message",
                                        String.format("Limit %s times of resending key per hour",
                                                      applicationProperties.getActivationKeyLimitNumber())));
    }

    @Test
    @Transactional
    public void test_requestResetPassword_resetKeyIsNull_shouldBeOk()
        throws Exception {
        var user = initUser(null, true);

        var req = ResetPasswordReq.builder()
                                  .email(user.getEmail())
                                  .build();

        restMvc.perform(MockMvcRequestBuilders.post(baseUrl + "/reset-password/init")
                                              .contentType(TestHelper.APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(req)))
               .andExpect(MockMvcResultMatchers.status().isNoContent());

        var existUser = getUserRepository().findOneByEmailAndActivatedIsTrue(user.getEmail()).orElse(null);
        Assertions.assertThat(existUser).isNotNull();
        Assertions.assertThat(existUser.getResetKey()).isNotNull();
    }

    @Test
    @Transactional
    public void test_requestResetPassword_resetKeyIsNotNull_shouldBeOk()
        throws Exception {
        var currentTime = Instant.now();
        var sendNumber = applicationProperties.getResetPasswordKeyLimitNumber() - 1;
        var resetKey = generateKey(EMAIL, sendNumber, currentTime,
                                   currentTime.plus(30, MINUTES));
        var user = initUser(codeHelper.generateKey(resetKey), true);

        var oldResetKey = user.getResetKey();

        var req = ResetPasswordReq.builder()
                                  .email(user.getEmail())
                                  .build();

        restMvc.perform(MockMvcRequestBuilders.post(baseUrl + "/reset-password/init")
                                              .contentType(TestHelper.APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(req)))
               .andExpect(MockMvcResultMatchers.status().isNoContent());

        var existUser = getUserRepository().findOneByEmailAndActivatedIsTrue(user.getEmail()).orElse(null);
        Assertions.assertThat(existUser).isNotNull();

        var newResetKey = codeHelper.getKeyInfo(existUser.getResetKey());
        Assertions.assertThat(existUser.getResetKey()).isNotEqualTo(oldResetKey);
        Assertions.assertThat(newResetKey.getExpiredDate()).isNotEqualTo(resetKey.getExpiredDate());
        Assertions.assertThat(newResetKey.getNumberOfSendKey()).isEqualTo(sendNumber + 1);
    }

    @Test
    @Transactional
    public void test_requestResetPassword_emailIsInvalid_shouldNotFound()
        throws Exception {
        var resetPasswordReq = new ResetPasswordReq("invalid.email@test.com");

        restMvc.perform(MockMvcRequestBuilders.post(baseUrl + "/reset-password/init")
                                              .contentType(TestHelper.APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(resetPasswordReq)))
               .andExpect(MockMvcResultMatchers.status().isNotFound())
               .andExpect(matchJsonPath("$.errors[0].error_code", OBJECT_NOT_FOUND))
               .andExpect(matchJsonPath("$.errors[0].field", "email"))
               .andExpect(matchJsonPath("$.errors[0].message", "Object not found"));
    }

    @Test
    @Transactional
    public void test_finishResetPassword_shouldBeOk()
        throws Exception {
        var currentTime = Instant.now();
        var sendNumber = applicationProperties.getResetPasswordKeyLimitNumber();
        var resetKey = generateKey(EMAIL, sendNumber, currentTime,
                                   currentTime.plus(30, MINUTES));

        var user = initUser(codeHelper.generateKey(resetKey), true);

        var newPassword = RandomHelper.generatePassword();

        var req = FinishResetPasswordReq.builder()
                                        .key(user.getResetKey())
                                        .password(newPassword)
                                        .build();

        restMvc.perform(MockMvcRequestBuilders.post(baseUrl + "/reset-password/finish")
                                              .contentType(TestHelper.APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(req)))
               .andExpect(MockMvcResultMatchers.status().isNoContent());

        var existUser = getUserRepository().findOneByEmailAndActivatedIsTrue(user.getEmail()).orElse(null);
        Assertions.assertThat(existUser).isNotNull();
        Assertions.assertThat(existUser.getResetKey()).isNull();
        Assertions.assertThat(getPasswordEncoder().matches(newPassword, existUser.getPassword())).isTrue();
    }

    @Test
    @Transactional
    public void test_finishResetPassword_withInvalidResetKey_shouldNotFound()
        throws Exception {
        var currentTime = Instant.now();
        var sendNumber = applicationProperties.getResetPasswordKeyLimitNumber();
        var resetKey = generateKey(EMAIL, sendNumber, currentTime,
                                   currentTime.plus(30, MINUTES));

        initUser(codeHelper.generateKey(resetKey), true);

        var req = FinishResetPasswordReq.builder()
                                        .key(RandomHelper.generateResetKey())
                                        .password(RandomHelper.generatePassword())
                                        .build();

        restMvc.perform(MockMvcRequestBuilders.post(baseUrl + "/reset-password/finish")
                                              .contentType(TestHelper.APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(req)))
               .andExpect(MockMvcResultMatchers.status().isNotFound())
               .andExpect(matchJsonPath("$.errors[0].error_code", OBJECT_NOT_FOUND))
               .andExpect(matchJsonPath("$.errors[0].field", "key"))
               .andExpect(matchJsonPath("$.errors[0].message", "Object not found"));
    }

    @Test
    @Transactional
    public void test_finishResetPassword_resetKeyIsExpired_shouldNotFound()
        throws Exception {
        var currentTime = Instant.now();
        var sendNumber = applicationProperties.getResetPasswordKeyLimitNumber();
        var resetKey = generateKey(EMAIL, sendNumber, currentTime.minus(80, MINUTES),
                                   currentTime.minus(20, MINUTES));

        var user = initUser(codeHelper.generateKey(resetKey), true);

        var req = FinishResetPasswordReq.builder()
                                        .key(user.getResetKey())
                                        .password(RandomHelper.generatePassword())
                                        .build();

        restMvc.perform(MockMvcRequestBuilders.post(baseUrl + "/reset-password/finish")
                                              .contentType(TestHelper.APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(req)))
               .andExpect(MockMvcResultMatchers.status().isBadRequest())
               .andExpect(matchJsonPath("$.errors[0].error_code", EXPIRED_KEY))
               .andExpect(matchJsonPath("$.errors[0].field", "key"))
               .andExpect(matchJsonPath("$.errors[0].message", "Expired key"));
    }

    @Test
    @Transactional
    public void test_verifyExpiredKey_keyIsValid_shouldReturnFalse()
        throws Exception {
        var currentTime = Instant.now();
        var sendNumber = applicationProperties.getResetPasswordKeyLimitNumber();
        var resetKey = generateKey(EMAIL, sendNumber, currentTime,
                                   currentTime.plus(60, MINUTES));

        var user = initUser(codeHelper.generateKey(resetKey), true);

        var dto = VerifyKeyReq.builder()
                              .key(user.getResetKey())
                              .build();

        restMvc.perform(MockMvcRequestBuilders.post(baseUrl + "/key-expired")
                                              .contentType(TestHelper.APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(dto)))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(matchJsonPath("$.data.email", EMAIL))
               .andExpect(matchJsonPath("$.data.expired", false));
    }

    @Test
    @Transactional
    public void test_verifyExpiredKey_keyIsExpired_shouldReturnTrue()
        throws Exception {
        var currentTime = Instant.now();
        var sendNumber = applicationProperties.getResetPasswordKeyLimitNumber();
        var resetKey = generateKey(EMAIL, sendNumber, currentTime.minus(80, MINUTES),
                                   currentTime.minus(60, MINUTES));

        var user = initUser(codeHelper.generateKey(resetKey), true);

        var dto = VerifyKeyReq.builder()
                              .key(user.getResetKey())
                              .build();

        restMvc.perform(MockMvcRequestBuilders.post(baseUrl + "/key-expired")
                                              .contentType(TestHelper.APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(dto)))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(matchJsonPath("$.data.expired", true));
    }

    @Test
    @Transactional
    public void test_updateUser_shouldBeOk()
        throws Exception {

        User initUser = initChangePassword();
        mockAuthentication(initUser, ROLE_MEMBER.name());

        UserUpdateReq dto = UserUpdateReq.builder()
                                         .isFirstLogin(false)
                                         .build();

        restMvc.perform(MockMvcRequestBuilders.patch(baseUrl + "/" + initUser.getId())
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(dto)))
               .andExpect(MockMvcResultMatchers.status().isNoContent());

        User user = getUserRepository().findOneByUsername(CURRENT_USER_USERNAME).orElse(null);

        Assertions.assertThat(user).isNotNull();
        Assertions.assertThat(user.getIsFirstLogin()).isEqualTo(dto.getIsFirstLogin());
    }

    @Test
    @Transactional
    public void test_changePassword_shouldBeOk()
        throws Exception {
        User initUser = initChangePassword();
        mockAuthentication(initUser, ROLE_MEMBER.name());

        String newPassword = "newPassword123";
        ChangePasswordReq dto = ChangePasswordReq.builder()
                                                 .oldPassword(ACCOUNT_PASSWORD)
                                                 .newPassword(newPassword)
                                                 .build();

        restMvc.perform(MockMvcRequestBuilders.post(baseUrl + "/change-password")
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(dto)))
               .andExpect(MockMvcResultMatchers.status().isNoContent());

        User user = getUserRepository().findOneByUsername(CURRENT_USER_USERNAME).orElse(null);

        Assertions.assertThat(user).isNotNull();
        Assertions.assertThat(user.getUsername()).isEqualTo(CURRENT_USER_USERNAME);
        Assertions.assertThat(getPasswordEncoder().matches(newPassword, user.getPassword())).isTrue();
    }

    @Test
    @Transactional
    public void test_changePassword_passwordLengthLessThanMinLength_shouldBeBadRequest()
        throws Exception {
        User user = initChangePassword();
        mockAuthentication(user, ROLE_MEMBER.name());

        String newPassword = "new";
        ChangePasswordReq dto = ChangePasswordReq.builder()
                                                 .oldPassword(ACCOUNT_PASSWORD)
                                                 .newPassword(newPassword)
                                                 .build();

        restMvc.perform(MockMvcRequestBuilders.post(baseUrl + "/change-password")
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(dto)))
               .andExpect(MockMvcResultMatchers.status().isBadRequest())
               .andExpect(matchJsonPath("$.errors[0].error_code", INVALID_PASSWORD))
               .andExpect(matchJsonPath("$.errors[0].field", "password"))
               .andExpect(matchJsonPath("$.errors[0].message",
                                        String.format("Password must be at least %s characters", PASSWORD_MIN_LENGTH)));
    }

    @Test
    @Transactional
    public void test_changePassword_userNotExist_shouldBeBadRequest()
        throws Exception {
        User user = initChangePassword();
        Assertions.assertThat(user.getUsername()).isEqualTo(CURRENT_USER_USERNAME);

        ChangePasswordReq dto = ChangePasswordReq.builder()
                                                 .oldPassword(ACCOUNT_PASSWORD)
                                                 .newPassword("newPassword")
                                                 .build();

        restMvc.perform(MockMvcRequestBuilders.post(baseUrl + "/change-password")
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(dto)))
               .andExpect(MockMvcResultMatchers.status().isNotFound())
               .andExpect(matchJsonPath("$.errors[0].error_code", OBJECT_NOT_FOUND))
               .andExpect(matchJsonPath("$.errors[0].field", "user"))
               .andExpect(matchJsonPath("$.errors[0].message", "Object not found"));
    }

    @Test
    @Transactional
    public void test_changePassword_oldPasswordIsWrong_shouldBeBadRequest()
        throws Exception {
        User user = initChangePassword();
        mockAuthentication(user, ROLE_MEMBER.name());

        Assertions.assertThat(user.getUsername()).isEqualTo(CURRENT_USER_USERNAME);
        Assertions.assertThat(getPasswordEncoder().matches(ACCOUNT_PASSWORD, user.getPassword())).isTrue();

        ChangePasswordReq dto = ChangePasswordReq.builder()
                                                 .oldPassword("oldPassword")
                                                 .newPassword("newPassword")
                                                 .build();

        restMvc.perform(MockMvcRequestBuilders.post(baseUrl + "/change-password")
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(dto)))
               .andExpect(MockMvcResultMatchers.status().isBadRequest())
               .andExpect(matchJsonPath("$.errors[0].error_code", INVALID_OLD_PASSWORD))
               .andExpect(matchJsonPath("$.errors[0].field", "oldPassword"))
               .andExpect(matchJsonPath("$.errors[0].message", "Invalid old password"));
    }

    private void fakeData()
        throws RegisterBlueprintException, CreateModelException {
        registerBlueprints(UserBlueprint.class,
                           ProfileUpdateReqBlueprint.class,
                           RoleBlueprint.class,
                           UserCreateReqBlueprint.class);

        Long permissionId = getPermissionRepository().save(new Permission("READ")).getId();
        Long roleId = getRoleRepository().save(new Role(ROLE_MEMBER.name())).getId();

        getRolePermissionRepository().save(RolePermission.builder()
                                                         .roleId(roleId)
                                                         .permissionId(permissionId)
                                                         .build());
    }

    private User initUser(final String key, final boolean activated)
        throws CreateModelException {
        User user = createFakeModel(User.class);
        user.setEmail(EMAIL);
        user.setActivated(activated);
        user.setResetKey(key);
        user.setActivationKey(key);

        return getUserRepository().save(user);
    }

    private CodeHelper.Key generateKey(final String email,
                                       final Integer sendNumber,
                                       final Instant sendDate,
                                       final Instant expiredDate) {
        return CodeHelper.Key.builder()
                             .email(email)
                             .key(RandomUtil.generateActivationKey())
                             .numberOfSendKey(sendNumber)
                             .sendDate(DateHelper.truncatedToSecond(sendDate))
                             .expiredDate(DateHelper.truncatedToSecond(expiredDate))
                             .build();
    }

    private User initChangePassword()
        throws CreateModelException {
        User user = createFakeModel(User.class);
        user.setUsername(CURRENT_USER_USERNAME);
        user.setPassword(getPasswordEncoder().encode(ACCOUNT_PASSWORD));
        user.setActivationKey(null);

        return getUserRepository().save(user);
    }

    private UserCreateReq initUserCreateReq()
        throws RegisterBlueprintException, CreateModelException {
        registerBlueprints(UserCreateReqBlueprint.class);

        return createFakeModel(UserCreateReq.class);
    }

    private Role initRole()
        throws CreateModelException {
        Role role = createFakeModel(Role.class);
        return getRoleRepository().save(role);
    }
}
