package com.nals.rw360;

import com.github.javafaker.Faker;
import com.nals.rw360.bloc.v1.UserCrudBloc;
import com.nals.rw360.config.ApplicationProperties;
import com.nals.rw360.domain.Permission;
import com.nals.rw360.domain.Role;
import com.nals.rw360.domain.RolePermission;
import com.nals.rw360.domain.User;
import com.nals.rw360.domain.UserRole;
import com.nals.rw360.errors.ExceptionTranslator;
import com.nals.rw360.repository.AssessmentRepository;
import com.nals.rw360.repository.GroupRepository;
import com.nals.rw360.repository.GroupTypeRepository;
import com.nals.rw360.repository.PermissionRepository;
import com.nals.rw360.repository.RolePermissionRepository;
import com.nals.rw360.repository.RoleRepository;
import com.nals.rw360.repository.SubGroupRepository;
import com.nals.rw360.repository.UserRepository;
import com.nals.rw360.repository.UserRoleRepository;
import com.nals.rw360.repository.UserSubGroupRepository;
import com.nals.rw360.security.DomainUserDetails;
import com.nals.rw360.service.v1.FileService;
import com.tobedevoured.modelcitizen.CreateModelException;
import com.tobedevoured.modelcitizen.ModelFactory;
import com.tobedevoured.modelcitizen.RegisterBlueprintException;
import lombok.Getter;
import lombok.Setter;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.JsonPathResultMatchers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Component
public class AbstractTest {

    public static final String ACCOUNT_PASSWORD = "rw360123#@!";
    public static final Long INVALID_ID = -1L;
    public static final Long CURRENT_USER_ID = 99L;
    public static final String CURRENT_USER_USERNAME = "username";
    private static final String DEFAULT_TIME_ZONE = "Asia/Ho_Chi_Minh";
    private static final String FILE_SERVICE = "fileService";

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private HttpMessageConverter<?>[] httpMessageConverters;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Mock
    private ApplicationProperties mockApplicationProperties;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private SubGroupRepository subGroupRepository;

    @Autowired
    private UserSubGroupRepository userSubGroupRepository;

    @Autowired
    private GroupTypeRepository groupTypeRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private FileService fileService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserCrudBloc userCrudBloc;

    private ModelFactory modelFactory;
    private Faker faker;
    private ZoneId zoneId;

    @Before
    public void before() {
        this.modelFactory = new ModelFactory();
        this.faker = new Faker();

        Mockito.when(mockApplicationProperties.getTimezone())
               .thenReturn(DEFAULT_TIME_ZONE);

        zoneId = ZoneId.of(mockApplicationProperties.getTimezone());
    }

    protected void createUserAndRoles(final String... roles)
        throws CreateModelException {
        User user = createFakeModel(User.class);
        userRepository.save(user);

        for (String role : roles) {
            Long roleId = roleRepository.findByName(role)
                                        .orElseGet(() -> roleRepository.save(Role.builder().name(role).build()))
                                        .getId();

            userRoleRepository.save(UserRole.builder()
                                            .userId(user.getId())
                                            .roleId(roleId)
                                            .build());
        }
    }

    protected void mockFileService(final Object bloc)
        throws IOException {
        ReflectionTestUtils.setField(bloc, FILE_SERVICE, fileService);
    }

    protected void createRoleAndPermissions(final User user,
                                            final String role,
                                            final String... permissions) {

        Long roleId = roleRepository.findByName(role)
                                    .orElseGet(() -> roleRepository.save(Role.builder().name(role).build()))
                                    .getId();

        userRoleRepository.save(UserRole.builder()
                                        .userId(user.getId())
                                        .roleId(roleId)
                                        .build());

        for (String permission : permissions) {
            Long permissionId = permissionRepository.findByName(permission)
                                                    .orElseGet(() -> permissionRepository.save(Permission.builder()
                                                                                                         .name(role)
                                                                                                         .build()))
                                                    .getId();
            rolePermissionRepository.save(RolePermission.builder()
                                                        .roleId(roleId)
                                                        .permissionId(permissionId)
                                                        .build());
        }
    }

    protected void mockAuthentication(final User user,
                                      final String role,
                                      final String... permissions) {

        Set<GrantedAuthority> authorities = new HashSet<>();
        Set<GrantedAuthority> roles = Set.of(new SimpleGrantedAuthority(role));

        Long userId = user.getId();
        Long roleId = roleRepository.findByName(role)
                                    .orElseGet(() -> roleRepository.save(Role.builder().name(role).build()))
                                    .getId();

        userRoleRepository.save(UserRole.builder()
                                        .userId(userId)
                                        .roleId(roleId)
                                        .build());

        for (String permission : permissions) {
            authorities.add(new SimpleGrantedAuthority(permission));
            Long permissionId = permissionRepository.findByName(permission)
                                                    .orElseGet(() -> permissionRepository.save(Permission.builder()
                                                                                                         .name(role)
                                                                                                         .build()))
                                                    .getId();
            rolePermissionRepository.save(RolePermission.builder()
                                                        .roleId(roleId)
                                                        .permissionId(permissionId)
                                                        .build());
        }

        DomainUserDetails principal = new DomainUserDetails(userId,
                                                            user.getUsername(),
                                                            user.getPassword(),
                                                            roles, authorities);

        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public void registerBlueprints(final Class<?>... classes)
        throws RegisterBlueprintException {
        modelFactory.setRegisterBlueprints(Arrays.asList(classes));
    }

    public <T> T createFakeModel(final Class<T> clazz)
        throws CreateModelException {
        return modelFactory.createModel(clazz, true);
    }

    public JsonPathResultMatchers jsonPath(final String expression) {
        return MockMvcResultMatchers.jsonPath(expression);
    }

    public ResultMatcher matchJsonPath(final String expression, final Object expectedValue) {
        return jsonPath(expression).value(expectedValue);
    }
}
