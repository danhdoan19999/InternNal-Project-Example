package com.nals.rw360.v1;

import com.nals.rw360.AbstractTest;
import com.nals.rw360.Review360App;
import com.nals.rw360.api.v1.GroupTypeListController;
import com.nals.rw360.blueprints.GroupTypeBlueprint;
import com.nals.rw360.blueprints.UserBlueprint;
import com.nals.rw360.domain.GroupType;
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
public class GroupTypeListControllerIntTest
    extends AbstractTest {
    @Autowired
    private GroupTypeListController groupTypeListController;
    private MockMvc restMvc;
    private String baseUrl;
    private User user;
    private GroupType groupType;
    private GroupType groupType2;

    @Before
    public void setup()
        throws RegisterBlueprintException, CreateModelException {
        this.restMvc = MockMvcBuilders.standaloneSetup(groupTypeListController)
                                      .setMessageConverters(getHttpMessageConverters())
                                      .setControllerAdvice(getExceptionTranslator())
                                      .build();

        this.baseUrl = "/api/v1/group-types";

        initData();
    }

    @Test
    @Transactional
    public void test_fetchAllGroupType_shouldBeOk()
        throws Exception {
        mockAuthentication(user, ROLE_ADMIN.name());

        restMvc.perform(MockMvcRequestBuilders.get(this.baseUrl))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(matchJsonPath("$.data.items.length()", 2));
    }

    private void initData()
        throws RegisterBlueprintException, CreateModelException {
        registerBlueprints(UserBlueprint.class, GroupTypeBlueprint.class);

        user = createFakeModel(User.class);
        getUserRepository().save(user);

        groupType = createFakeModel(GroupType.class);
        getGroupTypeRepository().save(groupType);

        groupType2 = createFakeModel(GroupType.class);
        getGroupTypeRepository().save(groupType2);
    }
}
