package com.nals.rw360.v1;

import com.nals.rw360.AbstractTest;
import com.nals.rw360.Review360App;
import com.nals.rw360.api.v1.SubGroupListController;
import com.nals.rw360.blueprints.GroupBlueprint;
import com.nals.rw360.blueprints.GroupTypeBlueprint;
import com.nals.rw360.blueprints.SubGroupBlueprint;
import com.nals.rw360.blueprints.UserBlueprint;
import com.nals.rw360.blueprints.UserSubGroupBlueprint;
import com.nals.rw360.domain.Group;
import com.nals.rw360.domain.GroupType;
import com.nals.rw360.domain.SubGroup;
import com.nals.rw360.domain.User;
import com.nals.rw360.domain.UserSubGroup;
import com.nals.rw360.repository.GroupTypeRepository;
import com.nals.rw360.repository.SubGroupRepository;
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
public class SubGroupListControllerIntTest
    extends AbstractTest {

    @Autowired
    private SubGroupListController subGroupListController;

    @Autowired
    private SubGroupRepository subGroupRepository;

    @Autowired
    private GroupTypeRepository groupTypeRepository;

    private MockMvc restMvc;

    private String baseUrl;

    private User user;
    private User user2;

    private Group group;

    private SubGroup subGroup;

    private UserSubGroup userSubGroup;
    private UserSubGroup userSubGroup2;

    @Before
    public void setup()
        throws RegisterBlueprintException, CreateModelException {
        this.restMvc = MockMvcBuilders.standaloneSetup(subGroupListController)
                                      .setMessageConverters(getHttpMessageConverters())
                                      .setControllerAdvice(getExceptionTranslator())
                                      .build();
        group = initGroup();

        initData();

        this.baseUrl = String.format("%s%s%s", "/api/v1/groups/", group.getId(), "/sub-groups");
    }

    @Test
    @Transactional
    public void test_fetchAllMembersOfSubGroup_shouldBeOk()
        throws Exception {
        mockAuthentication(user, ROLE_ADMIN.name());

        userSubGroup.setUserId(user.getId());
        userSubGroup.setGroupId(group.getId());
        userSubGroup.setSubGroupId(subGroup.getId());

        userSubGroup2.setUserId(user2.getId());
        userSubGroup2.setGroupId(group.getId());
        userSubGroup2.setSubGroupId(subGroup.getId());

        restMvc.perform(MockMvcRequestBuilders.get(this.baseUrl + "/" + subGroup.getId() + "/members"));
    }

    @Test
    @Transactional
    public void test_searchMembersOfSubGroup_shouldBeOk()
        throws Exception {
        mockAuthentication(user, ROLE_ADMIN.name());

        String keyword = "da";

        user.setName("danh");
        userSubGroup.setUserId(user.getId());
        userSubGroup.setGroupId(group.getId());
        userSubGroup.setSubGroupId(subGroup.getId());

        user2.setName("hoa");
        userSubGroup2.setUserId(user2.getId());
        userSubGroup2.setGroupId(group.getId());
        userSubGroup2.setSubGroupId(subGroup.getId());

        restMvc.perform(MockMvcRequestBuilders.get(this.baseUrl + "/" + subGroup.getId() + "/members")
                                              .param("keyword", keyword))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(matchJsonPath("$.data.items.length()", 1));
    }

    public void test_fetchAllSubGroupsByGroupId_shouldBeOk()
        throws Exception {
        mockAuthentication(user, ROLE_ADMIN.name());

        restMvc.perform(MockMvcRequestBuilders.get(this.baseUrl))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(matchJsonPath("$.data.items.length()", 2));
    }

    public void test_searchSubGroups_shouldBeOk()
        throws Exception {
        mockAuthentication(user, ROLE_ADMIN.name());

        String keyword = "Sub Group";
        SubGroup subGroup3 = createFakeModel(SubGroup.class);
        subGroup3.setName("Group");
        subGroupRepository.save(subGroup3);

        restMvc.perform(MockMvcRequestBuilders.get(this.baseUrl)
                                              .param("keyword", keyword))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(matchJsonPath("$.data.items.length()", 2));
    }

    private void initData()
        throws RegisterBlueprintException, CreateModelException {
        registerBlueprints(UserBlueprint.class,
                           GroupBlueprint.class,
                           SubGroupBlueprint.class,
                           UserSubGroupBlueprint.class);

        registerBlueprints(UserBlueprint.class, SubGroupBlueprint.class, GroupTypeBlueprint.class);

        user = createFakeModel(User.class);
        getUserRepository().save(user);

        user2 = createFakeModel(User.class);
        getUserRepository().save(user2);

        group = createFakeModel(Group.class);
        getGroupRepository().save(group);

        subGroup = createFakeModel(SubGroup.class);
        getSubGroupRepository().save(subGroup);

        userSubGroup = createFakeModel(UserSubGroup.class);
        getUserSubGroupRepository().save(userSubGroup);

        userSubGroup2 = createFakeModel(UserSubGroup.class);
        getUserSubGroupRepository().save(userSubGroup2);

        GroupType groupType = createFakeModel(GroupType.class);
        groupTypeRepository.save(groupType);

        SubGroup subGroup1 = createFakeModel(SubGroup.class);
        subGroup1.setGroupId(group.getId());
        subGroup1.setGroupTypeId(groupType.getId());
        subGroupRepository.save(subGroup1);

        SubGroup subGroup2 = createFakeModel(SubGroup.class);
        subGroup2.setGroupId(group.getId());
        subGroup2.setGroupTypeId(groupType.getId());
        subGroupRepository.save(subGroup2);
    }

    private Group initGroup()
        throws RegisterBlueprintException, CreateModelException {
        registerBlueprints(GroupBlueprint.class);
        Group group = createFakeModel(Group.class);

        return getGroupRepository().save(group);
    }
}
