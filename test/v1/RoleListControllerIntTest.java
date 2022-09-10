package com.nals.rw360.v1;

import com.nals.rw360.AbstractTest;
import com.nals.rw360.Review360App;
import com.nals.rw360.api.v1.RoleListController;
import com.nals.rw360.blueprints.RoleBlueprint;
import com.nals.rw360.blueprints.UserBlueprint;
import com.nals.rw360.domain.Role;
import com.nals.rw360.domain.User;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import static com.nals.rw360.enums.RoleType.ROLE_ADMIN;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Review360App.class)
public class RoleListControllerIntTest
    extends AbstractTest {

    @Autowired
    private RoleListController roleListController;

    private MockMvc restMvc;
    private String baseUrl;

    @Before
    public void setup()
        throws RegisterBlueprintException, CreateModelException {
        this.restMvc = MockMvcBuilders.standaloneSetup(roleListController)
                                      .setMessageConverters(getHttpMessageConverters())
                                      .setControllerAdvice(getExceptionTranslator())
                                      .build();

        this.baseUrl = "/api/v1/roles";
        fakeData();
    }

    @Test
    @Transactional
    public void test_fetchAllRoles_shouldBeOk()
        throws Exception {
        getRoleRepository().deleteAll();

        Role role1 = initRole();
        role1.setName(ROLE_ADMIN.name());
        mockAuthentication(initUser(), role1.getName());

        initRole();

        restMvc.perform(MockMvcRequestBuilders.get(this.baseUrl))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(matchJsonPath("$.data.items.length()", 2));
    }

    private void fakeData()
        throws RegisterBlueprintException {
        registerBlueprints(UserBlueprint.class,
                           RoleBlueprint.class);
    }

    private User initUser()
        throws CreateModelException {
        User user = createFakeModel(User.class);
        return getUserRepository().save(user);
    }

    private Role initRole()
        throws CreateModelException {
        Role role = createFakeModel(Role.class);
        return getRoleRepository().save(role);
    }
}
