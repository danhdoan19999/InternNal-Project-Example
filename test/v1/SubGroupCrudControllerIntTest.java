package com.nals.rw360.v1;

import com.github.javafaker.Faker;
import com.nals.rw360.AbstractTest;
import com.nals.rw360.Review360App;
import com.nals.rw360.api.v1.SubGroupCrudController;
import com.nals.rw360.blueprints.GroupBlueprint;
import com.nals.rw360.blueprints.GroupTypeBlueprint;
import com.nals.rw360.blueprints.SubGroupBlueprint;
import com.nals.rw360.blueprints.UserBlueprint;
import com.nals.rw360.blueprints.UserSubGroupBlueprint;
import com.nals.rw360.blueprints.request.SubGroupCreateReqBlueprint;
import com.nals.rw360.domain.Group;
import com.nals.rw360.domain.GroupType;
import com.nals.rw360.domain.SubGroup;
import com.nals.rw360.domain.User;
import com.nals.rw360.dto.v1.request.group.sub.SubGroupCreateReq;
import com.nals.rw360.dto.v1.response.DataRes;
import com.nals.rw360.helpers.JsonHelper;
import com.nals.rw360.helpers.StringHelper;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.transaction.Transactional;

import static com.nals.rw360.enums.RoleType.ROLE_ADMIN;
import static com.nals.rw360.enums.RoleType.ROLE_MANAGER;
import static com.nals.rw360.enums.RoleType.ROLE_MEMBER;
import static com.nals.rw360.errors.ErrorCodes.GROUP_TYPE_NOT_FOUND;
import static com.nals.rw360.errors.ErrorCodes.INVALID_NAME;
import static com.nals.rw360.errors.ErrorCodes.NAME_NOT_BLANK;
import static com.nals.rw360.errors.ErrorCodes.OBJECT_NOT_FOUND;
import static com.nals.rw360.errors.ErrorCodes.ROLE_NOT_LEADER;
import static com.nals.rw360.errors.ErrorCodes.SUB_GROUP_NAME_ALREADY_USED;
import static com.nals.rw360.helpers.StringHelper.EMPTY;
import static com.nals.rw360.helpers.TestHelper.APPLICATION_JSON_UTF8;
import static java.util.Locale.ENGLISH;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Review360App.class)
public class SubGroupCrudControllerIntTest
    extends AbstractTest {

    @Autowired
    private SubGroupCrudController subGroupCrudController;

    private MockMvc restMvc;

    private String baseUrl;

    private final Faker faker = new Faker(ENGLISH);

    private Group group;

    @Before
    public void setup()
        throws RegisterBlueprintException, CreateModelException {
        this.restMvc = MockMvcBuilders.standaloneSetup(subGroupCrudController)
                                      .setMessageConverters(getHttpMessageConverters())
                                      .setControllerAdvice(getExceptionTranslator())
                                      .build();

        fakeData();
        group = initGroup();
        this.baseUrl = String.format("%s%s%s", "/api/v1/groups/", group.getId(), "/sub-groups");
    }

    @Test
    @Transactional
    public void test_createSubGroup_shouldBeOk()
        throws Exception {
        mockAuthentication(initUser(), ROLE_ADMIN.name());

        var manager = initUser();
        createRoleAndPermissions(manager, ROLE_MANAGER.name());

        var groupType = initGroupType();

        var req = initSubGroupCreateReq();
        req.setManagerId(manager.getId());
        req.setGroupTypeId(groupType.getId());

        MvcResult mvcResult = restMvc.perform(MockMvcRequestBuilders.post(baseUrl)
                                                                    .contentType(APPLICATION_JSON_UTF8)
                                                                    .content(TestHelper.convertObjectToJsonBytes(req)))
                                     .andExpect(MockMvcResultMatchers.status().isCreated())
                                     .andReturn();

        DataRes response = JsonHelper.readValue(mvcResult.getResponse().getContentAsString(), DataRes.class);

        Long subGroupId = StringHelper.toNumber(response.getData().toString(), Long.class);
        SubGroup subGroup = getSubGroupRepository().findOneById(subGroupId).orElse(null);

        Assertions.assertThat(subGroup).isNotNull();
        Assertions.assertThat(subGroup.getName()).isEqualTo(req.getName());
        Assertions.assertThat(subGroup.getDescription()).isEqualTo(req.getDescription());
        Assertions.assertThat(subGroup.getManagerId()).isEqualTo(req.getManagerId());
    }

    @Test
    @Transactional
    public void test_createSubGroup_groupTypeNotFound_shouldBeBadRequest()
        throws Exception {
        mockAuthentication(initUser(), ROLE_ADMIN.name());

        var manager = initUser();
        createRoleAndPermissions(manager, ROLE_MANAGER.name());

        Long idNotExists = Long.MAX_VALUE;

        var req = initSubGroupCreateReq();
        req.setGroupTypeId(idNotExists);
        req.setManagerId(manager.getId());

        restMvc.perform(MockMvcRequestBuilders.post(baseUrl)
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(req)))
               .andExpect(MockMvcResultMatchers.status().isNotFound())
               .andExpect(matchJsonPath("$.errors[0].error_code", GROUP_TYPE_NOT_FOUND))
               .andExpect(matchJsonPath("$.errors[0].message", "Group type not found"))
               .andExpect(matchJsonPath("$.errors[0].field", "group_type_id"));
    }

    @Test
    @Transactional
    public void test_getSubGroupDetail_shouldBeOk()
        throws Exception {

        mockAuthentication(initUser(), ROLE_ADMIN.name());

        User manager = initUser();
        createRoleAndPermissions(manager, ROLE_MANAGER.name());

        GroupType groupType = initGroupType();

        var subGroup = createFakeModel(SubGroup.class);
        subGroup.setManagerId(manager.getId());
        subGroup.setGroupTypeId(groupType.getId());
        getSubGroupRepository().save(subGroup);

        String imageUrl = getFileService().getFullFileUrl(subGroup.getImageName());
        String imageUrlManager = getFileService().getFullFileUrl(manager.getImageName());

        restMvc.perform(MockMvcRequestBuilders.get(this.baseUrl + "/" + subGroup.getId()))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(matchJsonPath("$.data.id", subGroup.getId()))
               .andExpect(matchJsonPath("$.data.name", subGroup.getName()))
               .andExpect(matchJsonPath("$.data.description", subGroup.getDescription()))
               .andExpect(matchJsonPath("$.data.image_name", subGroup.getImageName()))
               .andExpect(matchJsonPath("$.data.image_url", imageUrl))
               .andExpect(matchJsonPath("$.data.group_type.id", groupType.getId()))
               .andExpect(matchJsonPath("$.data.group_type.name", groupType.getName()))
               .andExpect(matchJsonPath("$.data.manager.id", manager.getId()))
               .andExpect(matchJsonPath("$.data.manager.name", manager.getName()))
               .andExpect(matchJsonPath("$.data.manager.image_url", imageUrlManager));
    }

    @Test
    @Transactional
    public void test_getSubGroupDetail_idNotFound_shouldBeBadRequest()
        throws Exception {

        mockAuthentication(initUser(), ROLE_ADMIN.name());

        Long idNotExist = Long.MAX_VALUE;

        restMvc.perform(MockMvcRequestBuilders.get(this.baseUrl + "/" + idNotExist))
               .andExpect(MockMvcResultMatchers.status().isNotFound())
               .andExpect(matchJsonPath("$.errors[0].error_code", OBJECT_NOT_FOUND))
               .andExpect(matchJsonPath("$.errors[0].message", "Object not found"))
               .andExpect(matchJsonPath("$.errors[0].field", "sub_group"));
    }

    @Test
    @Transactional
    public void test_createSubGroup_invalidRoleForGroupManager_shouldBeBadRequest()
        throws Exception {
        mockAuthentication(initUser(), ROLE_ADMIN.name());

        var member = initUser();
        createRoleAndPermissions(member, ROLE_MEMBER.name());

        var req = initSubGroupCreateReq();
        req.setManagerId(member.getId());

        restMvc.perform(MockMvcRequestBuilders.post(baseUrl)
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(req)))
               .andExpect(MockMvcResultMatchers.status().isBadRequest())
               .andExpect(matchJsonPath("$.errors[0].error_code", ROLE_NOT_LEADER))
               .andExpect(matchJsonPath("$.errors[0].message",
                                        "User don't have role for to be a sub group leader"))
               .andExpect(matchJsonPath("$.errors[0].field", "manager_id"));
    }

    @Test
    @Transactional
    public void test_createSubGroup_nameAlreadyUsed_shouldBeBadRequest()
        throws Exception {

        String existedName = "JAVA";
        mockAuthentication(initUser(), ROLE_ADMIN.name());

        var subGroup = createFakeModel(SubGroup.class);
        subGroup.setName(existedName);
        getSubGroupRepository().save(subGroup);

        var req = initSubGroupCreateReq();
        req.setName(existedName);

        restMvc.perform(MockMvcRequestBuilders.post(baseUrl)
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(req)))
               .andExpect(MockMvcResultMatchers.status().isBadRequest())
               .andExpect(matchJsonPath("$.errors[0].error_code", SUB_GROUP_NAME_ALREADY_USED))
               .andExpect(matchJsonPath("$.errors[0].message", "Sub group name already used"))
               .andExpect(matchJsonPath("$.errors[0].field", "name"));
    }

    @Test
    @Transactional
    public void test_createSubGroup_nameBlank_shouldBeBadRequest()
        throws Exception {

        mockAuthentication(initUser(), ROLE_ADMIN.name());

        var req = initSubGroupCreateReq();
        req.setName(EMPTY);

        restMvc.perform(MockMvcRequestBuilders.post(baseUrl)
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(req)))
               .andExpect(MockMvcResultMatchers.status().isBadRequest())
               .andExpect(matchJsonPath("$.errors[0].error_code", NAME_NOT_BLANK))
               .andExpect(matchJsonPath("$.errors[0].message",
                                        "error.validation.name.not_blank"))
               .andExpect(matchJsonPath("$.errors[0].field", "name"));
    }

    @Test
    @Transactional
    public void test_createSubGroup_nameLengthGreaterThanMaxLength_shouldBeBadRequest()
        throws Exception {

        mockAuthentication(initUser(), ROLE_ADMIN.name());

        var req = initSubGroupCreateReq();
        req.setName(faker.lorem().characters(500));

        restMvc.perform(MockMvcRequestBuilders.post(baseUrl)
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(req)))
               .andExpect(MockMvcResultMatchers.status().isBadRequest())
               .andExpect(matchJsonPath("$.errors[0].error_code", INVALID_NAME))
               .andExpect(matchJsonPath("$.errors[0].message", "error.validation.name.size"))
               .andExpect(matchJsonPath("$.errors[0].field", "name"));
    }

    @Test
    @Transactional
    public void test_fetchAllMembers_notInSubGroup_shouldBeOk()
        throws Exception {
        getUserRepository().deleteAll();

        User user1 = initUser();
        mockAuthentication(user1, ROLE_ADMIN.name());

        SubGroup subGroup = initSubGroup();
        subGroup.setGroupId(group.getId());
        getSubGroupRepository().save(subGroup);

        initUser();

        restMvc.perform(MockMvcRequestBuilders.get(this.baseUrl + "/" + subGroup.getId() + "/available-members"))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(matchJsonPath("$.data.items.length()", 2));
    }

    @Test
    @Transactional
    public void test_fetchAllMembers_groupNotExists_shouldBeBadRequest()
        throws Exception {
        User user1 = initUser();
        mockAuthentication(user1, ROLE_ADMIN.name());

        Long groupIdNotExists = Long.MAX_VALUE;
        var url = "/api/v1/groups/" + groupIdNotExists + "/sub-groups/" + initSubGroup().getId() + "/available-members";

        restMvc.perform(MockMvcRequestBuilders.get(url))
               .andExpect(MockMvcResultMatchers.status().isNotFound())
               .andExpect(matchJsonPath("$.errors[0].error_code", OBJECT_NOT_FOUND))
               .andExpect(matchJsonPath("$.errors[0].message", "Object not found"))
               .andExpect(matchJsonPath("$.errors[0].field", "group"));
    }

    @Test
    @Transactional
    public void test_fetchAllMembers_subGroupNotExists_shouldBeBadRequest()
        throws Exception {
        User user1 = initUser();
        mockAuthentication(user1, ROLE_ADMIN.name());

        Long idNotExists = Long.MAX_VALUE;

        restMvc.perform(MockMvcRequestBuilders.get(this.baseUrl + "/" + idNotExists + "/available-members"))
               .andExpect(MockMvcResultMatchers.status().isNotFound())
               .andExpect(matchJsonPath("$.errors[0].error_code", OBJECT_NOT_FOUND))
               .andExpect(matchJsonPath("$.errors[0].message", "Object not found"))
               .andExpect(matchJsonPath("$.errors[0].field", "sub_group"));
    }

    private void fakeData()
        throws RegisterBlueprintException {
        registerBlueprints(SubGroupBlueprint.class, SubGroupCreateReqBlueprint.class, UserBlueprint.class,
                           GroupBlueprint.class, GroupTypeBlueprint.class, UserSubGroupBlueprint.class);
    }

    private User initUser()
        throws CreateModelException {
        User user = createFakeModel(User.class);
        return getUserRepository().save(user);
    }

    private Group initGroup()
        throws CreateModelException {
        Group group = createFakeModel(Group.class);

        return getGroupRepository().save(group);
    }

    private GroupType initGroupType()
        throws CreateModelException {
        GroupType groupType = createFakeModel(GroupType.class);

        return getGroupTypeRepository().save(groupType);
    }

    private SubGroupCreateReq initSubGroupCreateReq()
        throws RegisterBlueprintException, CreateModelException {
        registerBlueprints(SubGroupCreateReqBlueprint.class);

        return createFakeModel(SubGroupCreateReq.class);
    }

    private SubGroup initSubGroup()
        throws CreateModelException {
        SubGroup subGroup = createFakeModel(SubGroup.class);
        return getSubGroupRepository().save(subGroup);
    }
}
