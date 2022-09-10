package com.nals.rw360.v1;

import com.nals.rw360.AbstractTest;
import com.nals.rw360.Review360App;
import com.nals.rw360.api.v1.GroupListController;
import com.nals.rw360.blueprints.GroupBlueprint;
import com.nals.rw360.blueprints.UserBlueprint;
import com.nals.rw360.blueprints.UserSubGroupBlueprint;
import com.nals.rw360.domain.Group;
import com.nals.rw360.domain.User;
import com.nals.rw360.domain.UserSubGroup;
import com.nals.rw360.repository.GroupRepository;
import com.nals.rw360.repository.UserSubGroupRepository;
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
import static com.nals.rw360.enums.RoleType.ROLE_MEMBER;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Review360App.class)
public class GroupListControllerIntTest
    extends AbstractTest {

    @Autowired
    private GroupListController groupListController;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserSubGroupRepository userSubGroupRepository;

    private MockMvc restMvc;
    private String baseUrl;

    private User user1;
    private User user2;
    private Group group1;
    private Group group2;
    private UserSubGroup userSubGroup1;
    private UserSubGroup userSubGroup2;

    @Before
    public void setup()
        throws RegisterBlueprintException, CreateModelException {
        this.restMvc = MockMvcBuilders.standaloneSetup(groupListController)
                                      .setMessageConverters(getHttpMessageConverters())
                                      .setControllerAdvice(getExceptionTranslator())
                                      .build();

        this.baseUrl = "/api/v1/groups";
        fakeData();
    }

    @Test
    @Transactional
    public void test_fetchAllGroupLeader_shouldBeOk()
        throws Exception {
        User leader1 = initUser();
        User leader2 = initUser();
        mockAuthentication(leader1, ROLE_MANAGER.name());
        createRoleAndPermissions(leader2, ROLE_ACCOUNT_LEADER.name());

        restMvc.perform(MockMvcRequestBuilders.get(this.baseUrl + "/leaders"))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(matchJsonPath("$.data.items.length()", 2));
    }

    @Test
    @Transactional
    public void test_fetchAllGroups_shouldBeOk()
        throws Exception {
        mockAuthentication(user1, ROLE_ADMIN.name());

        restMvc.perform(MockMvcRequestBuilders.get(this.baseUrl))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(matchJsonPath("$.data.items.length()", 2));
    }

    @Test
    @Transactional
    public void test_searchGroups_shouldBeOk()
        throws Exception {
        mockAuthentication(user1, ROLE_ADMIN.name());

        group1.setName("Group 1");
        group2.setName("Group 2");
        groupRepository.save(group1);
        groupRepository.save(group2);

        String keyword = "Group";
        Group group3 = createFakeModel(Group.class);
        group3.setName("Sub");
        groupRepository.save(group3);

        restMvc.perform(MockMvcRequestBuilders.get(this.baseUrl)
                                              .param("keyword", keyword))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(matchJsonPath("$.data.items.length()", 2));
    }

    @Test
    @Transactional
    public void test_fetchAllMembersOfGroup_shouldBeOk()
        throws Exception {
        mockAuthentication(user1, ROLE_ADMIN.name());
        createRoleAndPermissions(user2, ROLE_MANAGER.name());

        userSubGroup1.setUserId(user1.getId());
        userSubGroup1.setGroupId(group1.getId());
        userSubGroupRepository.save(userSubGroup1);

        group1.setManagerId(user2.getId());
        groupRepository.save(group1);

        restMvc.perform(MockMvcRequestBuilders.get(this.baseUrl + "/" + group1.getId() + "/members"))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(matchJsonPath("$.data.items.length()", 2));
    }

    @Test
    @Transactional
    public void test_searchMembersOfGroup_shouldBeOk()
        throws Exception {
        mockAuthentication(user1, ROLE_ADMIN.name());
        createRoleAndPermissions(user2, ROLE_MEMBER.name());

        user1.setName("User 1");
        getUserRepository().save(user1);
        userSubGroup1.setGroupId(group1.getId());
        userSubGroup1.setUserId(user1.getId());
        getUserSubGroupRepository().save(userSubGroup1);

        user2.setName("User 2");
        group1.setManagerId(user2.getId());
        groupRepository.save(group1);

        String keyword = "User";
        User user3 = initUser();
        user3.setName("Test");
        getUserRepository().save(user3);
        createRoleAndPermissions(user3, ROLE_MEMBER.name());

        restMvc.perform(MockMvcRequestBuilders.get(this.baseUrl + "/" + group1.getId() + "/members")
                                              .param("keyword", keyword))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(matchJsonPath("$.data.items.length()", 2));
    }

    private void fakeData()
        throws RegisterBlueprintException, CreateModelException {
        registerBlueprints(GroupBlueprint.class,
                           UserBlueprint.class,
                           UserSubGroupBlueprint.class);

        user1 = initUser();
        user2 = initUser();

        group1 = createFakeModel(Group.class);
        getGroupRepository().save(group1);

        group2 = createFakeModel(Group.class);
        getGroupRepository().save(group2);

        userSubGroup1 = createFakeModel(UserSubGroup.class);
        userSubGroupRepository.save(userSubGroup1);

        userSubGroup2 = createFakeModel(UserSubGroup.class);
        userSubGroupRepository.save(userSubGroup2);
    }

    private User initUser()
        throws CreateModelException {
        User user = createFakeModel(User.class);
        return getUserRepository().save(user);
    }
}
