package com.nals.rw360.v1;

import com.github.javafaker.Faker;
import com.nals.rw360.AbstractTest;
import com.nals.rw360.Review360App;
import com.nals.rw360.api.v1.GroupCrudController;
import com.nals.rw360.blueprints.GroupBlueprint;
import com.nals.rw360.blueprints.UserBlueprint;
import com.nals.rw360.blueprints.request.GroupCreateReqBlueprint;
import com.nals.rw360.blueprints.request.GroupUpdateReqBlueprint;
import com.nals.rw360.domain.Group;
import com.nals.rw360.domain.Permission;
import com.nals.rw360.domain.Role;
import com.nals.rw360.domain.RolePermission;
import com.nals.rw360.domain.User;
import com.nals.rw360.dto.v1.request.group.GroupCreateReq;
import com.nals.rw360.dto.v1.request.group.GroupUpdateReq;
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
import static com.nals.rw360.errors.ErrorCodes.GROUP_NAME_ALREADY_USED;
import static com.nals.rw360.errors.ErrorCodes.INVALID_NAME;
import static com.nals.rw360.errors.ErrorCodes.NAME_NOT_BLANK;
import static com.nals.rw360.errors.ErrorCodes.OBJECT_NOT_FOUND;
import static com.nals.rw360.errors.ErrorCodes.ROLE_NOT_LEADER;
import static com.nals.rw360.helpers.TestHelper.APPLICATION_JSON_UTF8;
import static java.util.Locale.ENGLISH;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Review360App.class)
public class GroupCrudControllerIntTest
    extends AbstractTest {

    private static final String GROUP_NAME = "Group name";

    @Autowired
    private GroupCrudController groupCrudController;

    private MockMvc restMvc;

    private String baseUrl;

    private final Faker faker = new Faker(ENGLISH);

    @Before
    public void setup()
        throws RegisterBlueprintException {
        this.restMvc = MockMvcBuilders.standaloneSetup(groupCrudController)
                                      .setMessageConverters(getHttpMessageConverters())
                                      .setControllerAdvice(getExceptionTranslator())
                                      .build();

        this.baseUrl = "/api/v1/groups";

        fakeData();
    }

    @Test
    @Transactional
    public void test_getGroupDetail_shouldBeOk()
        throws Exception {
        mockAuthentication(initUser(), ROLE_ADMIN.name());

        User manager = initUser();
        createRoleAndPermissions(manager, ROLE_MANAGER.name());

        Group group = initGroup();
        group.setManagerId(manager.getId());
        String imageUrl = getFileService().getFullFileUrl(group.getImageName());
        String imageUrlManager = getFileService().getFullFileUrl(manager.getImageName());

        restMvc.perform(MockMvcRequestBuilders.get(this.baseUrl + "/" + group.getId()))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(matchJsonPath("$.data.id", group.getId()))
               .andExpect(matchJsonPath("$.data.name", group.getName()))
               .andExpect(matchJsonPath("$.data.description", group.getDescription()))
               .andExpect(matchJsonPath("$.data.image_name", group.getImageName()))
               .andExpect(matchJsonPath("$.data.image_url", imageUrl))
               .andExpect(matchJsonPath("$.data.leader.id", group.getManagerId()))
               .andExpect(matchJsonPath("$.data.leader.name", manager.getName()))
               .andExpect(matchJsonPath("$.data.leader.image_url", imageUrlManager));
    }

    @Test
    @Transactional
    public void test_getGroupDetail_idNotExist_shouldNotFound()
        throws Exception {
        var idNotExist = Long.MAX_VALUE;
        mockAuthentication(initUser(), ROLE_ADMIN.name());

        restMvc.perform(MockMvcRequestBuilders.get(this.baseUrl + "/" + idNotExist)
                                              .contentType(TestHelper.APPLICATION_JSON_UTF8))
               .andExpect(MockMvcResultMatchers.status().isNotFound())
               .andExpect(matchJsonPath("$.errors[0].error_code", OBJECT_NOT_FOUND))
               .andExpect(matchJsonPath("$.errors[0].field", "group"))
               .andExpect(matchJsonPath("$.errors[0].message", "Object not found"));
    }

    @Test
    @Transactional
    public void test_createGroup_shouldBeOk()
        throws Exception {
        mockAuthentication(initUser(), ROLE_ADMIN.name());

        User manager = initUser();
        createRoleAndPermissions(manager, ROLE_MANAGER.name());

        GroupCreateReq req = initBasicGroupCreateReq();
        req.setManagerId(manager.getId());

        MvcResult mvcResult = restMvc.perform(MockMvcRequestBuilders.post(baseUrl)
                                                                    .contentType(APPLICATION_JSON_UTF8)
                                                                    .content(TestHelper.convertObjectToJsonBytes(req)))
                                     .andExpect(MockMvcResultMatchers.status().isCreated())
                                     .andReturn();

        DataRes response = JsonHelper.readValue(mvcResult.getResponse().getContentAsString(), DataRes.class);

        Long groupId = StringHelper.toNumber(response.getData().toString(), Long.class);
        Group group = getGroupRepository().findOneById(groupId).orElse(null);

        Assertions.assertThat(group).isNotNull();
        Assertions.assertThat(group.getName()).isEqualTo(req.getName());
        Assertions.assertThat(group.getDescription()).isEqualTo(req.getDescription());
        Assertions.assertThat(group.getImageName()).isEqualTo(req.getImageName());
        Assertions.assertThat(group.getManagerId()).isEqualTo(req.getManagerId());

        Assertions.assertThat(getGroupRepository().findAllById(groupId)).hasSize(1);
    }

    @Test
    @Transactional
    public void test_createGroup_groupNameAlreadyUsed_shouldBeBadRequest()
        throws Exception {
        User user = initUser();
        mockAuthentication(user, ROLE_ADMIN.name());

        GroupCreateReq req = initBasicGroupCreateReq();
        req.setName(" " + GROUP_NAME + " ");

        initGroup();

        restMvc.perform(MockMvcRequestBuilders.post(baseUrl)
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(req)))
               .andExpect(MockMvcResultMatchers.status().isBadRequest())
               .andExpect(matchJsonPath("$.errors[0].error_code", GROUP_NAME_ALREADY_USED))
               .andExpect(matchJsonPath("$.errors[0].field", "name"))
               .andExpect(matchJsonPath("$.errors[0].message", "Group name already used"));
    }

    @Test
    @Transactional
    public void test_createGroup_invalidRoleForGroupManager_shouldBeBadRequest()
        throws Exception {
        User member = initUser();

        mockAuthentication(initUser(), ROLE_ADMIN.name());

        createRoleAndPermissions(member, ROLE_MEMBER.name());

        GroupCreateReq req = initBasicGroupCreateReq();
        req.setManagerId(member.getId());

        restMvc.perform(MockMvcRequestBuilders.post(baseUrl)
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(req)))
               .andExpect(MockMvcResultMatchers.status().isBadRequest())
               .andExpect(matchJsonPath("$.errors[0].error_code", ROLE_NOT_LEADER))
               .andExpect(matchJsonPath("$.errors[0].field", "manager_id"))
               .andExpect(matchJsonPath("$.errors[0].message",
                                        "User don't have role for to be a group leader"));
    }

    @Test
    @Transactional
    public void test_updateGroup_shouldBeOk()
        throws Exception {
        Group initGroup = initGroup();
        mockAuthentication(initUser(), ROLE_ADMIN.name());

        var manager = initUser();
        createRoleAndPermissions(manager, ROLE_MANAGER.name());

        GroupUpdateReq dto = initGroupUpdateReq();
        dto.setManagerId(manager.getId());

        restMvc.perform(MockMvcRequestBuilders.patch(baseUrl + "/" + initGroup.getId())
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(dto)))
               .andExpect(MockMvcResultMatchers.status().isNoContent());

        Group group = getGroupRepository().findById(initGroup.getId()).orElse(null);

        Assertions.assertThat(group).isNotNull();
        Assertions.assertThat(initGroup.getName()).isEqualTo(dto.getName());
        Assertions.assertThat(initGroup.getDescription()).isEqualTo(dto.getDescription());
        Assertions.assertThat(initGroup.getImageName()).isEqualTo(dto.getImageName());
        Assertions.assertThat(initGroup.getManagerId()).isEqualTo(dto.getManagerId());
    }

    @Test
    @Transactional
    public void test_updateGroup_invalidRoleForGroupManager_shouldBeBadRequest()
        throws Exception {
        Group initGroup = initGroup();
        mockAuthentication(initUser(), ROLE_ADMIN.name());

        var member = initUser();
        createRoleAndPermissions(member, ROLE_MEMBER.name());

        GroupUpdateReq dto = initGroupUpdateReq();
        dto.setManagerId(member.getId());

        restMvc.perform(MockMvcRequestBuilders.patch(baseUrl + "/" + initGroup.getId())
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(dto)))
               .andExpect(MockMvcResultMatchers.status().isBadRequest())
               .andExpect(matchJsonPath("$.errors[0].field", "manager_id"))
               .andExpect(matchJsonPath("$.errors[0].message",
                                        "User don't have role for to be a group leader"))
               .andExpect(matchJsonPath("$.errors[0].error_code", ROLE_NOT_LEADER));
    }

    @Test
    @Transactional
    public void test_updateGroup_nameAlreadyUsed_shouldBeBadRequest()
        throws Exception {

        Group initGroup = initGroup();
        String existedName = "JAVA";
        mockAuthentication(initUser(), ROLE_ADMIN.name());

        Group group = createFakeModel(Group.class);
        group.setName(existedName);
        getGroupRepository().save(group);

        GroupUpdateReq dto = initGroupUpdateReq();
        dto.setName(existedName);

        restMvc.perform(MockMvcRequestBuilders.patch(baseUrl + "/" + initGroup.getId())
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(dto)))
               .andExpect(MockMvcResultMatchers.status().isBadRequest())
               .andExpect(matchJsonPath("$.errors[0].error_code", GROUP_NAME_ALREADY_USED))
               .andExpect(matchJsonPath("$.errors[0].message", "Group name already used"))
               .andExpect(matchJsonPath("$.errors[0].field", "name"));

        Assertions.assertThat(initGroup.getName()).isNotEqualTo(dto.getName());
    }

    @Test
    @Transactional
    public void test_updateGroup_nameBlank_shouldBeBadRequest()
        throws Exception {
        Group initGroup = initGroup();
        mockAuthentication(initUser(), ROLE_ADMIN.name());

        GroupUpdateReq dto = initGroupUpdateReq();
        dto.setName("");

        restMvc.perform(MockMvcRequestBuilders.patch(baseUrl + "/" + initGroup.getId())
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(dto)))
               .andExpect(MockMvcResultMatchers.status().isBadRequest())
               .andExpect(matchJsonPath("$.errors[0].field", "name"))
               .andExpect(matchJsonPath("$.errors[0].error_code", NAME_NOT_BLANK));

        Assertions.assertThat(initGroup.getName()).isNotEqualTo(dto.getName());
    }

    @Test
    public void test_updateGroup_idNotExit_shouldBeBadRequest()
        throws Exception {
        Long id = Long.MAX_VALUE;
        mockAuthentication(initUser(), ROLE_ADMIN.name());

        GroupUpdateReq dto = initGroupUpdateReq();

        restMvc.perform(MockMvcRequestBuilders.patch(baseUrl + "/" + id)
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(dto)))
               .andExpect(MockMvcResultMatchers.status().isNotFound())
               .andExpect(matchJsonPath("$.errors[0].error_code", OBJECT_NOT_FOUND))
               .andExpect(matchJsonPath("$.errors[0].message", "Object not found"))
               .andExpect(matchJsonPath("$.errors[0].field", "group"));
    }

    @Test
    @Transactional
    public void test_updateGroup_nameLengthGreaterThanMaxLength_shouldBeBadRequest()
        throws Exception {
        Group initGroup = initGroup();
        mockAuthentication(initUser(), ROLE_ADMIN.name());

        GroupUpdateReq dto = initGroupUpdateReq();
        dto.setName(faker.lorem().characters(300));

        restMvc.perform(MockMvcRequestBuilders.patch(baseUrl + "/" + initGroup.getId())
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(dto)))
               .andExpect(MockMvcResultMatchers.status().isBadRequest())
               .andExpect(matchJsonPath("$.errors[0].field", "name"))
               .andExpect(matchJsonPath("$.errors[0].error_code", INVALID_NAME));

        Assertions.assertThat(initGroup.getName()).isNotEqualTo(dto.getName());
    }

    private void fakeData()
        throws RegisterBlueprintException {
        registerBlueprints(GroupBlueprint.class,
                           GroupUpdateReqBlueprint.class,
                           UserBlueprint.class,
                           GroupCreateReqBlueprint.class);

        Long permissionId = getPermissionRepository().save(new Permission("READ")).getId();
        Long roleId = getRoleRepository().save(new Role(ROLE_ADMIN.name())).getId();

        getRolePermissionRepository().save(RolePermission.builder()
                                                         .roleId(roleId)
                                                         .permissionId(permissionId)
                                                         .build());
    }

    private User initUser()
        throws CreateModelException {
        User user = createFakeModel(User.class);

        return getUserRepository().save(user);
    }

    private GroupUpdateReq initGroupUpdateReq()
        throws RegisterBlueprintException, CreateModelException {
        registerBlueprints(GroupUpdateReqBlueprint.class);

        return createFakeModel(GroupUpdateReq.class);
    }

    private Group initGroup()
        throws CreateModelException {
        Group group = createFakeModel(Group.class);
        group.setName(GROUP_NAME);

        return getGroupRepository().save(group);
    }

    private GroupCreateReq initBasicGroupCreateReq()
        throws RegisterBlueprintException, CreateModelException {
        registerBlueprints(GroupCreateReqBlueprint.class);

        return createFakeModel(GroupCreateReq.class);
    }
}
