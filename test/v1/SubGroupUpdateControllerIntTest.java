package com.nals.rw360.v1;

import com.nals.rw360.AbstractTest;
import com.nals.rw360.Review360App;
import com.nals.rw360.api.v1.SubGroupUpdateController;
import com.nals.rw360.blueprints.GroupTypeBlueprint;
import com.nals.rw360.blueprints.SubGroupBlueprint;
import com.nals.rw360.blueprints.UserBlueprint;
import com.nals.rw360.blueprints.request.SubGroupUpdateReqBlueprint;
import com.nals.rw360.domain.GroupType;
import com.nals.rw360.domain.SubGroup;
import com.nals.rw360.domain.User;
import com.nals.rw360.dto.v1.request.group.sub.SubGroupUpdateReq;
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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.transaction.Transactional;

import static com.nals.rw360.enums.RoleType.ROLE_ADMIN;
import static com.nals.rw360.enums.RoleType.ROLE_MANAGER;
import static com.nals.rw360.enums.RoleType.ROLE_MEMBER;
import static com.nals.rw360.errors.ErrorCodes.NAME_NOT_BLANK;
import static com.nals.rw360.errors.ErrorCodes.OBJECT_NOT_FOUND;
import static com.nals.rw360.errors.ErrorCodes.ROLE_NOT_LEADER;
import static com.nals.rw360.errors.ErrorCodes.SUB_GROUP_NAME_ALREADY_USED;
import static com.nals.rw360.helpers.TestHelper.APPLICATION_JSON_UTF8;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Review360App.class)
public class SubGroupUpdateControllerIntTest
    extends AbstractTest {
    @Autowired
    private SubGroupUpdateController subGroupCrudController;

    private MockMvc restMvc;

    private String baseUrl;

    @Before
    public void setup()
        throws RegisterBlueprintException {
        this.restMvc = MockMvcBuilders.standaloneSetup(subGroupCrudController)
                                      .setMessageConverters(getHttpMessageConverters())
                                      .setControllerAdvice(getExceptionTranslator())
                                      .build();

        this.baseUrl = "/api/v1/sub-groups";

        fakeData();
    }

    @Test
    @Transactional
    public void test_updateSubGroup_shouldBeOk()
        throws Exception {
        SubGroup initSubGroup = initSubGroup();
        mockAuthentication(initUser(), ROLE_ADMIN.name());

        var manager = initUser();
        createRoleAndPermissions(manager, ROLE_MANAGER.name());

        var groupType = initGroupType();

        var req = initSubGroupUpdateReq();
        req.setManagerId(manager.getId());
        req.setGroupTypeId(groupType.getId());

        restMvc.perform(MockMvcRequestBuilders.put(baseUrl + "/" + initSubGroup.getId())
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(req)))
               .andExpect(MockMvcResultMatchers.status().isNoContent());

        SubGroup subGroup = getSubGroupRepository().findById(initSubGroup.getId()).orElse(null);

        Assertions.assertThat(subGroup).isNotNull();
        Assertions.assertThat(initSubGroup.getName()).isEqualTo(req.getName());
        Assertions.assertThat(initSubGroup.getDescription()).isEqualTo(req.getDescription());
        Assertions.assertThat(initSubGroup.getImageName()).isEqualTo(req.getImageName());
        Assertions.assertThat(initSubGroup.getGroupTypeId()).isEqualTo(req.getGroupTypeId());
        Assertions.assertThat(initSubGroup.getManagerId()).isEqualTo(req.getManagerId());
    }

    @Test
    @Transactional
    public void test_updateSubGroup_nameAlreadyUsed_shouldBeBadRequest()
        throws Exception {

        SubGroup initSubGroup = initSubGroup();
        String existedName = "PROJECT PHP";
        mockAuthentication(initUser(), ROLE_ADMIN.name());

        SubGroup subGroup = createFakeModel(SubGroup.class);
        subGroup.setName(existedName);
        getSubGroupRepository().save(subGroup);

        SubGroupUpdateReq req = initSubGroupUpdateReq();
        req.setName(existedName);

        restMvc.perform(MockMvcRequestBuilders.put(baseUrl + "/" + initSubGroup.getId())
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(req)))
               .andExpect(MockMvcResultMatchers.status().isBadRequest())
               .andExpect(matchJsonPath("$.errors[0].error_code", SUB_GROUP_NAME_ALREADY_USED))
               .andExpect(matchJsonPath("$.errors[0].message", "Sub group name already used"))
               .andExpect(matchJsonPath("$.errors[0].field", "name"));

        Assertions.assertThat(initSubGroup.getName()).isNotEqualTo(req.getName());
    }

    @Test
    @Transactional
    public void test_updateSubGroup_nameBlank_shouldBeBadRequest()
        throws Exception {
        SubGroup initSubGroup = initSubGroup();
        mockAuthentication(initUser(), ROLE_ADMIN.name());

        SubGroupUpdateReq req = initSubGroupUpdateReq();
        req.setName("");

        restMvc.perform(MockMvcRequestBuilders.put(baseUrl + "/" + initSubGroup.getId())
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(req)))
               .andExpect(MockMvcResultMatchers.status().isBadRequest())
               .andExpect(matchJsonPath("$.errors[0].field", "name"))
               .andExpect(matchJsonPath("$.errors[0].error_code", NAME_NOT_BLANK));

        Assertions.assertThat(initSubGroup.getName()).isNotEqualTo(req.getName());
    }

    @Test
    public void test_updateSubGroup_idNotExit_shouldBeBadRequest()
        throws Exception {
        mockAuthentication(initUser(), ROLE_ADMIN.name());

        SubGroupUpdateReq req = initSubGroupUpdateReq();

        restMvc.perform(MockMvcRequestBuilders.put(baseUrl + "/" + Long.MAX_VALUE)
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(req)))
               .andExpect(MockMvcResultMatchers.status().isNotFound())
               .andExpect(matchJsonPath("$.errors[0].error_code", OBJECT_NOT_FOUND))
               .andExpect(matchJsonPath("$.errors[0].message", "Object not found"))
               .andExpect(matchJsonPath("$.errors[0].field", "sub_group"));
    }

    @Test
    @Transactional
    public void test_updateSubGroup_invalidRoleForSubGroupManager_shouldBeBadRequest()
        throws Exception {
        SubGroup initSubGroup = initSubGroup();
        mockAuthentication(initUser(), ROLE_ADMIN.name());

        User member = initUser();
        createRoleAndPermissions(member, ROLE_MEMBER.name());

        SubGroupUpdateReq req = initSubGroupUpdateReq();
        req.setManagerId(member.getId());

        restMvc.perform(MockMvcRequestBuilders.put(baseUrl + "/" + initSubGroup.getId())
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(req)))
               .andDo(MockMvcResultHandlers.print())
               .andExpect(MockMvcResultMatchers.status().isBadRequest())
               .andExpect(matchJsonPath("$.errors[0].error_code", ROLE_NOT_LEADER))
               .andExpect(matchJsonPath("$.errors[0].message",
                                        "User don't have role for to be a sub group leader"))
               .andExpect(matchJsonPath("$.errors[0].field", "manager_id"));
    }

    private void fakeData()
        throws RegisterBlueprintException {
        registerBlueprints(SubGroupBlueprint.class,
                           SubGroupUpdateReqBlueprint.class,
                           UserBlueprint.class,
                           GroupTypeBlueprint.class);
    }

    private User initUser()
        throws CreateModelException {
        User user = createFakeModel(User.class);

        return getUserRepository().save(user);
    }

    private SubGroup initSubGroup()
        throws CreateModelException {
        SubGroup subGroup = createFakeModel(SubGroup.class);

        return getSubGroupRepository().save(subGroup);
    }

    private GroupType initGroupType()
        throws CreateModelException {
        GroupType groupType = createFakeModel(GroupType.class);

        return getGroupTypeRepository().save(groupType);
    }

    private SubGroupUpdateReq initSubGroupUpdateReq()
        throws CreateModelException, RegisterBlueprintException {
        registerBlueprints(SubGroupUpdateReqBlueprint.class);

        return createFakeModel(SubGroupUpdateReq.class);
    }
}
