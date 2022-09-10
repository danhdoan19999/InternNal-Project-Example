package com.nals.rw360.v1;

import com.nals.rw360.AbstractTest;
import com.nals.rw360.Review360App;
import com.nals.rw360.api.v1.SubGroupController;
import com.nals.rw360.blueprints.GroupBlueprint;
import com.nals.rw360.blueprints.SubGroupBlueprint;
import com.nals.rw360.blueprints.UserBlueprint;
import com.nals.rw360.blueprints.UserSubGroupBlueprint;
import com.nals.rw360.blueprints.request.AddMemberReqBlueprint;
import com.nals.rw360.domain.Group;
import com.nals.rw360.domain.SubGroup;
import com.nals.rw360.domain.User;
import com.nals.rw360.domain.UserSubGroup;
import com.nals.rw360.dto.v1.request.group.sub.AddMemberReq;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.transaction.Transactional;

import java.util.Arrays;
import java.util.HashSet;

import static com.nals.rw360.enums.RoleType.ROLE_ADMIN;
import static com.nals.rw360.errors.ErrorCodes.OBJECT_NOT_FOUND;
import static com.nals.rw360.errors.ErrorCodes.USER_ALREADY_JOIN_THIS_SUB_GROUP;
import static com.nals.rw360.errors.ErrorCodes.USER_NOT_EXISTS_IN_SUB_GROUP;
import static com.nals.rw360.helpers.TestHelper.APPLICATION_JSON_UTF8;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Review360App.class)
public class SubGroupControllerIntTest
    extends AbstractTest {

    @Autowired
    private SubGroupController subGroupController;

    private MockMvc restMvc;

    private String baseUrl;

    private User user1;
    private User user2;

    private Group group;

    private SubGroup subGroup;

    private UserSubGroup userSubGroup;

    @Before
    public void setup()
        throws RegisterBlueprintException, CreateModelException {
        this.restMvc = MockMvcBuilders.standaloneSetup(subGroupController)
                                      .setMessageConverters(getHttpMessageConverters())
                                      .setControllerAdvice(getExceptionTranslator())
                                      .build();
        fakeData();
        this.baseUrl = "/api/v1/sub-groups/";
    }

    @Test
    @Transactional
    public void test_addMembers_shouldBeOk()
        throws Exception {
        mockAuthentication(initUser(), ROLE_ADMIN.name());

        var req = initAddMemberReq();
        req.setIds(new HashSet<>(Arrays.asList(user1.getId(), user2.getId())));

        restMvc.perform(MockMvcRequestBuilders.post(baseUrl + subGroup.getId() + "/members")
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(req)))
               .andExpect(MockMvcResultMatchers.status().isNoContent());

        var userIds = getUserSubGroupRepository().findAllUserIdsBySubGroupId(subGroup.getId());
        Assertions.assertThat(userIds.containsAll(req.getIds())).isTrue();
    }

    @Test
    @Transactional
    public void test_addMembers_userNotFound_shouldBeBadRequest()
        throws Exception {
        mockAuthentication(initUser(), ROLE_ADMIN.name());

        var req = initAddMemberReq();

        restMvc.perform(MockMvcRequestBuilders.post(baseUrl + subGroup.getId() + "/members")
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(req)))
               .andExpect(MockMvcResultMatchers.status().isNotFound())
               .andExpect(matchJsonPath("$.errors[0].error_code", OBJECT_NOT_FOUND))
               .andExpect(matchJsonPath("$.errors[0].field", "user"))
               .andExpect(matchJsonPath("$.errors[0].message", "Object not found"));
    }

    @Test
    @Transactional
    public void test_addMembers_userAlreadyJoinSubGroup_shouldBeBadRequest()
        throws Exception {
        mockAuthentication(initUser(), ROLE_ADMIN.name());

        userSubGroup.setSubGroupId(subGroup.getId());
        userSubGroup.setUserId(user1.getId());
        userSubGroup.setGroupId(group.getId());
        getUserSubGroupRepository().save(userSubGroup);

        var req = initAddMemberReq();
        req.setIds(new HashSet<>(Arrays.asList(user1.getId(), user2.getId())));

        restMvc.perform(MockMvcRequestBuilders.post(baseUrl + subGroup.getId() + "/members")
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(req)))
               .andExpect(MockMvcResultMatchers.status().isBadRequest())
               .andExpect(matchJsonPath("$.errors[0].error_code", USER_ALREADY_JOIN_THIS_SUB_GROUP))
               .andExpect(matchJsonPath("$.errors[0].field", "user_id"))
               .andExpect(matchJsonPath("$.errors[0].message",
                                        "User have id: " + user1.getId() + " already join this sub-group"));
    }

    @Test
    @Transactional
    public void test_removeMember_shouldBeOk()
        throws Exception {
        mockAuthentication(initUser(), ROLE_ADMIN.name());

        restMvc.perform(MockMvcRequestBuilders.delete(baseUrl
                                                          + userSubGroup.getSubGroupId() + "/members/"
                                                          + userSubGroup.getUserId())
                                              .contentType(APPLICATION_JSON_UTF8))
               .andExpect(MockMvcResultMatchers.status().isNoContent());
        Assertions.assertThat(getUserSubGroupRepository()
                                  .findAllBySubGroupId(userSubGroup.getSubGroupId())).hasSize(0);
    }

    @Test
    @Transactional
    public void test_removeMember_userNotFound_shouldBeBadRequest()
        throws Exception {
        mockAuthentication(initUser(), ROLE_ADMIN.name());

        var notExistsId = Long.MAX_VALUE;

        restMvc.perform(MockMvcRequestBuilders.delete(baseUrl
                                                          + userSubGroup.getSubGroupId() + "/members/"
                                                          + notExistsId)
                                              .contentType(APPLICATION_JSON_UTF8))
               .andExpect(MockMvcResultMatchers.status().isNotFound())
               .andExpect(matchJsonPath("$.errors[0].error_code", USER_NOT_EXISTS_IN_SUB_GROUP))
               .andExpect(matchJsonPath("$.errors[0].field", "user_sub_group"))
               .andExpect(matchJsonPath("$.errors[0].message", "Object not found"));
    }

    private void fakeData()
        throws RegisterBlueprintException, CreateModelException {
        registerBlueprints(SubGroupBlueprint.class, UserBlueprint.class, GroupBlueprint.class,
                           UserSubGroupBlueprint.class, AddMemberReqBlueprint.class);

        user1 = createFakeModel(User.class);
        getUserRepository().save(user1);

        user2 = createFakeModel(User.class);
        getUserRepository().save(user2);

        group = createFakeModel(Group.class);
        getGroupRepository().save(group);

        subGroup = createFakeModel(SubGroup.class);
        subGroup.setGroupId(group.getId());
        getSubGroupRepository().save(subGroup);

        userSubGroup = createFakeModel(UserSubGroup.class);
        getUserSubGroupRepository().save(userSubGroup);
    }

    private User initUser()
        throws CreateModelException {
        User user = createFakeModel(User.class);

        return getUserRepository().save(user);
    }

    private AddMemberReq initAddMemberReq()
        throws RegisterBlueprintException, CreateModelException {
        registerBlueprints(AddMemberReqBlueprint.class);

        return createFakeModel(AddMemberReq.class);
    }
}
