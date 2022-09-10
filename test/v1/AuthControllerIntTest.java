package com.nals.rw360.v1;

import com.nals.rw360.AbstractTest;
import com.nals.rw360.Review360App;
import com.nals.rw360.api.v1.AuthController;
import com.nals.rw360.blueprints.UserBlueprint;
import com.nals.rw360.domain.Permission;
import com.nals.rw360.domain.Role;
import com.nals.rw360.domain.RolePermission;
import com.nals.rw360.domain.User;
import com.nals.rw360.domain.UserRole;
import com.nals.rw360.dto.v1.request.auth.LoginReq;
import com.nals.rw360.helpers.DateHelper;
import com.nals.rw360.helpers.TestHelper;
import com.tobedevoured.modelcitizen.CreateModelException;
import com.tobedevoured.modelcitizen.RegisterBlueprintException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import static com.nals.rw360.helpers.TestHelper.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Review360App.class)
public class AuthControllerIntTest
    extends AbstractTest {

    @Autowired
    private AuthController authController;

    private User user;
    private MockMvc restMvc;
    private String baseUrl;

    @Before
    public void setup()
        throws RegisterBlueprintException, CreateModelException {
        this.restMvc = MockMvcBuilders.standaloneSetup(authController)
                                      .setMessageConverters(getHttpMessageConverters())
                                      .setControllerAdvice(getExceptionTranslator())
                                      .build();
        this.baseUrl = "/api/v1/auth";

        fakeData();
    }

    @Test
    @Transactional
    public void test_login_shouldBeOk()
        throws Exception {
        LoginReq dto = new LoginReq(user.getUsername(), ACCOUNT_PASSWORD);

        String imageUrl = getFileService().getFullFileUrl(user.getImageName());

        restMvc.perform(MockMvcRequestBuilders.post(baseUrl + "/login")
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(dto)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.data.access_token").exists())
               .andExpect(jsonPath("$.data.refresh_token").exists())
               .andExpect(jsonPath("$.data.roles").exists())
               .andExpect(jsonPath("$.data.permissions").exists())
               .andExpect(jsonPath("$.data.expired_in").exists())
               .andExpect(matchJsonPath("$.data.user_info.id", user.getId()))
               .andExpect(matchJsonPath("$.data.user_info.name", user.getName()))
               .andExpect(matchJsonPath("$.data.user_info.email", user.getEmail()))
               .andExpect(matchJsonPath("$.data.user_info.phone", user.getPhone()))
               .andExpect(matchJsonPath("$.data.user_info.address", user.getAddress()))
               .andExpect(matchJsonPath("$.data.user_info.gender", user.getGender().name()))
               .andExpect(matchJsonPath("$.data.user_info.dob", DateHelper.toMillis(user.getDob())))
               .andExpect(matchJsonPath("$.data.user_info.phone", user.getPhone()))
               .andExpect(matchJsonPath("$.data.user_info.image_name", user.getImageName()))
               .andExpect(matchJsonPath("$.data.user_info.image_url", imageUrl))
               .andExpect(matchJsonPath("$.data.user_info.is_first_login", true));
    }

    private void fakeData()
        throws RegisterBlueprintException, CreateModelException {
        registerBlueprints(UserBlueprint.class);

        user = createFakeModel(User.class);
        user.setPassword(getPasswordEncoder().encode(ACCOUNT_PASSWORD));
        getUserRepository().save(user);

        Long permissionId = getPermissionRepository().save(new Permission("READ")).getId();
        Long roleId = getRoleRepository().save(new Role("ROLE_REQUESTER")).getId();

        getUserRoleRepository().save(UserRole.builder()
                                             .userId(user.getId())
                                             .roleId(roleId)
                                             .build());

        getRolePermissionRepository().save(RolePermission.builder()
                                                         .roleId(roleId)
                                                         .permissionId(permissionId)
                                                         .build());
    }
}
