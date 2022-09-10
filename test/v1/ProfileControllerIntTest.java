package com.nals.rw360.v1;

import com.nals.rw360.AbstractTest;
import com.nals.rw360.Review360App;
import com.nals.rw360.api.v1.ProfileController;
import com.nals.rw360.blueprints.UserBlueprint;
import com.nals.rw360.blueprints.request.ProfileUpdateReqBlueprint;
import com.nals.rw360.domain.Permission;
import com.nals.rw360.domain.Role;
import com.nals.rw360.domain.RolePermission;
import com.nals.rw360.domain.User;
import com.nals.rw360.dto.v1.request.user.ProfileUpdateReq;
import com.nals.rw360.helpers.DateHelper;
import com.nals.rw360.helpers.TestHelper;
import com.tobedevoured.modelcitizen.CreateModelException;
import com.tobedevoured.modelcitizen.RegisterBlueprintException;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import static com.nals.rw360.enums.RoleType.ROLE_MEMBER;
import static com.nals.rw360.errors.ErrorCodes.INVALID_PHONE;
import static com.nals.rw360.errors.ErrorCodes.PHONE_ALREADY_USED;
import static com.nals.rw360.helpers.TestHelper.APPLICATION_JSON_UTF8;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Review360App.class)
public class ProfileControllerIntTest
    extends AbstractTest {

    @Autowired
    private ProfileController profileController;

    private MockMvc restMvc;
    private String baseUrl;

    @Before
    public void setup()
        throws RegisterBlueprintException {
        this.restMvc = MockMvcBuilders.standaloneSetup(profileController)
                                      .setMessageConverters(getHttpMessageConverters())
                                      .setControllerAdvice(getExceptionTranslator())
                                      .build();

        this.baseUrl = "/api/v1/me";

        fakeData();
    }

    @Test
    @Transactional
    public void test_getProfile_shouldBeOk()
        throws Exception {
        User user = initUser();
        String imageUrl = getFileService().getFullFileUrl(user.getImageName());
        mockAuthentication(user, ROLE_MEMBER.name());

        restMvc.perform(MockMvcRequestBuilders.get(this.baseUrl))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(matchJsonPath("$.data.id", user.getId()))
               .andExpect(matchJsonPath("$.data.name", user.getName()))
               .andExpect(matchJsonPath("$.data.email", user.getEmail()))
               .andExpect(matchJsonPath("$.data.phone", user.getPhone()))
               .andExpect(matchJsonPath("$.data.address", user.getAddress()))
               .andExpect(matchJsonPath("$.data.gender", user.getGender().name()))
               .andExpect(matchJsonPath("$.data.dob", DateHelper.toMillis(user.getDob())))
               .andExpect(matchJsonPath("$.data.phone", user.getPhone()))
               .andExpect(matchJsonPath("$.data.image_name", user.getImageName()))
               .andExpect(matchJsonPath("$.data.image_url", imageUrl))
               .andExpect(matchJsonPath("$.data.is_first_login", true));
    }

    @Test
    @Transactional
    public void test_updateProfile_withInvalidPhone_shouldBadRequest()
        throws Exception {
        User user = initUser();
        ProfileUpdateReq req = initBasicInfoReq(user);

        mockAuthentication(user, ROLE_MEMBER.name());

        req.setPhone(RandomStringUtils.randomAlphabetic(10));

        restMvc.perform(MockMvcRequestBuilders.put(baseUrl)
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(req)))
               .andExpect(MockMvcResultMatchers.status().isBadRequest())
               .andExpect(matchJsonPath("$.errors[0].error_code", INVALID_PHONE))
               .andExpect(matchJsonPath("$.errors[0].field", "phone"))
               .andExpect(matchJsonPath("$.errors[0].message", "Invalid phone number"));
    }

    @Test
    @Transactional
    public void test_updateProfile_withPhoneAlreadyUsed_shouldBadRequest()
        throws Exception {
        User user = initUser();
        ProfileUpdateReq req = initBasicInfoReq(user);

        mockAuthentication(user, ROLE_MEMBER.name());

        User updatedUser = createFakeModel(User.class);
        getUserRepository().save(updatedUser);

        req.setPhone(updatedUser.getPhone());

        restMvc.perform(MockMvcRequestBuilders.put(baseUrl)
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(req)))
               .andExpect(MockMvcResultMatchers.status().isBadRequest())
               .andExpect(matchJsonPath("$.errors[0].error_code", PHONE_ALREADY_USED))
               .andExpect(matchJsonPath("$.errors[0].field", "phone"))
               .andExpect(matchJsonPath("$.errors[0].message", "Phone number already used"));
    }

    private void fakeData()
        throws RegisterBlueprintException {
        registerBlueprints(UserBlueprint.class,
                           ProfileUpdateReqBlueprint.class);

        Long permissionId = getPermissionRepository().save(new Permission("READ")).getId();
        Long roleId = getRoleRepository().save(new Role(ROLE_MEMBER.name())).getId();

        getRolePermissionRepository().save(RolePermission.builder()
                                                         .roleId(roleId)
                                                         .permissionId(permissionId)
                                                         .build());
    }

    private User initUser()
        throws CreateModelException {
        User user = createFakeModel(User.class);
        return getUserRepository().save(user);
    }

    private ProfileUpdateReq initBasicInfoReq(final User user)
        throws RegisterBlueprintException, CreateModelException {
        registerBlueprints(ProfileUpdateReqBlueprint.class);
        mockAuthentication(user, ROLE_MEMBER.name());
        return createFakeModel(ProfileUpdateReq.class);
    }
}
