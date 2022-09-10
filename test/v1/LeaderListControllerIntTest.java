package com.nals.rw360.v1;

import com.nals.rw360.AbstractTest;
import com.nals.rw360.Review360App;
import com.nals.rw360.api.v1.LeaderListController;
import com.nals.rw360.blueprints.GroupBlueprint;
import com.nals.rw360.blueprints.UserBlueprint;
import com.nals.rw360.domain.Group;
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

import static com.nals.rw360.enums.RoleType.ROLE_ACCOUNT_LEADER;
import static com.nals.rw360.enums.RoleType.ROLE_ADMIN;
import static com.nals.rw360.enums.RoleType.ROLE_MANAGER;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Review360App.class)
public class LeaderListControllerIntTest
    extends AbstractTest {

    @Autowired
    private LeaderListController leaderListController;

    private MockMvc restMvc;

    private String baseUrl;

    private User leader1;

    private User leader2;

    @Before
    public void setup()
        throws RegisterBlueprintException, CreateModelException {
        this.restMvc = MockMvcBuilders.standaloneSetup(leaderListController)
                                      .setMessageConverters(getHttpMessageConverters())
                                      .setControllerAdvice(getExceptionTranslator())
                                      .build();

        fakeData();
        this.baseUrl = "/api/v1/leaders";
    }

    @Test
    @Transactional
    public void test_searchSubGroupLeaders_shouldBeOk()
        throws Exception {
        mockAuthentication(initUser(), ROLE_ADMIN.name());

        createRoleAndPermissions(leader1, ROLE_MANAGER.name());
        createRoleAndPermissions(leader2, ROLE_ACCOUNT_LEADER.name());

        String keyword = "test";

        restMvc.perform(MockMvcRequestBuilders.get(this.baseUrl)
                                              .param("keyword", keyword))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(matchJsonPath("$.data.items.length()", 3));
    }

    private void fakeData()
        throws RegisterBlueprintException, CreateModelException {
        registerBlueprints(UserBlueprint.class,
                           GroupBlueprint.class);

        leader1 = initUser();
        leader2 = initUser();
    }

    private User initUser()
        throws CreateModelException {
        User user = createFakeModel(User.class);
        user.setName(user.getName() + "test");

        return getUserRepository().save(user);
    }

    private Group initGroup()
        throws CreateModelException {
        Group group = createFakeModel(Group.class);

        return getGroupRepository().save(group);
    }
}
