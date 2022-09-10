package com.nals.rw360.v1;

import com.nals.rw360.AbstractTest;
import com.nals.rw360.Review360App;
import com.nals.rw360.api.v1.UserListController;
import com.nals.rw360.blueprints.UserBlueprint;
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
public class UserListControllerIntTest
    extends AbstractTest {

    @Autowired
    private UserListController userListController;

    private MockMvc restMvc;
    private String baseUrl;

    @Before
    public void setup()
        throws RegisterBlueprintException, CreateModelException {
        this.restMvc = MockMvcBuilders.standaloneSetup(userListController)
                                      .setMessageConverters(getHttpMessageConverters())
                                      .setControllerAdvice(getExceptionTranslator())
                                      .build();

        this.baseUrl = "/api/v1/users";
        fakeData();
    }

    @Test
    @Transactional
    public void test_searchUsers_shouldBeOk()
        throws Exception {
        User user1 = initUser();
        mockAuthentication(user1, ROLE_ADMIN.name());

        user1.setName("User 1");
        getUserRepository().save(user1);

        User user2 = initUser();
        user2.setName("User 2");
        getUserRepository().save(user2);

        String keyword = "User";

        User user3 = initUser();
        user3.setName("Account");
        getUserRepository().save(user3);

        restMvc.perform(MockMvcRequestBuilders.get(this.baseUrl)
                                              .param("keyword", keyword))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(matchJsonPath("$.data.items.length()", 2));
    }

    private void fakeData()
        throws RegisterBlueprintException {
        registerBlueprints(UserBlueprint.class);
    }

    private User initUser()
        throws CreateModelException {
        User user = createFakeModel(User.class);
        return getUserRepository().save(user);
    }
}
