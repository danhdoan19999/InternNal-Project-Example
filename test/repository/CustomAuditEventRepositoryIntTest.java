package com.nals.rw360.repository;

import com.nals.rw360.Review360App;
import com.nals.rw360.config.audit.AuditEventConverter;
import com.nals.rw360.domain.PersistentAuditEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nals.rw360.config.Constants.ANONYMOUS_USER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for the CustomAuditEventRepository class.
 *
 * @see CustomAuditEventRepository
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Review360App.class)
@Transactional
public class CustomAuditEventRepositoryIntTest {

    @Autowired
    private PersistenceAuditEventRepository persistenceAuditEventRepository;

    @Autowired
    private AuditEventConverter auditEventConverter;

    private CustomAuditEventRepository customAuditEventRepository;

    @Before
    public void setup() {
        customAuditEventRepository = new CustomAuditEventRepository(persistenceAuditEventRepository,
                                                                    auditEventConverter);
        persistenceAuditEventRepository.deleteAll();
        Instant oneHourAgo = Instant.now().minusSeconds(3600);

        PersistentAuditEvent testUserEvent = new PersistentAuditEvent();
        testUserEvent.setPrincipal("test-user");
        testUserEvent.setAuditEventType("test-type");
        testUserEvent.setAuditEventDate(oneHourAgo);
        Map<String, String> data = new HashMap<>();
        data.put("test-key", "test-value");
        testUserEvent.setData(data);

        PersistentAuditEvent testOldUserEvent = new PersistentAuditEvent();
        testOldUserEvent.setPrincipal("test-user");
        testOldUserEvent.setAuditEventType("test-type");
        testOldUserEvent.setAuditEventDate(oneHourAgo.minusSeconds(10000));

        PersistentAuditEvent testOtherUserEvent = new PersistentAuditEvent();
        testOtherUserEvent.setPrincipal("other-test-user");
        testOtherUserEvent.setAuditEventType("test-type");
        testOtherUserEvent.setAuditEventDate(oneHourAgo);
    }

    @Test
    public void test_addAuditEvent() {
        Map<String, Object> data = new HashMap<>();
        data.put("test-key", "test-value");
        AuditEvent event = new AuditEvent("test-user", "test-type", data);
        customAuditEventRepository.add(event);
        List<PersistentAuditEvent> persistentAuditEvents = persistenceAuditEventRepository.findAll();
        assertThat(persistentAuditEvents).hasSize(1);
        PersistentAuditEvent persistentAuditEvent = persistentAuditEvents.get(0);
        assertThat(persistentAuditEvent.getPrincipal()).isEqualTo(event.getPrincipal());
        assertThat(persistentAuditEvent.getAuditEventType()).isEqualTo(event.getType());
        assertThat(persistentAuditEvent.getData()).containsKey("test-key");
        assertThat(persistentAuditEvent.getData().get("test-key")).isEqualTo("test-value");
        assertThat(persistentAuditEvent.getAuditEventDate()).isEqualTo(event.getTimestamp());
    }

    @Test
    public void test_addAuditEventTruncateLargeData() {
        Map<String, Object> data = new HashMap<>();
        String largeData = "a".repeat(CustomAuditEventRepository.EVENT_DATA_COLUMN_MAX_LENGTH + 10);
        data.put("test-key", largeData);
        AuditEvent event = new AuditEvent("test-user", "test-type", data);
        customAuditEventRepository.add(event);
        List<PersistentAuditEvent> persistentAuditEvents = persistenceAuditEventRepository.findAll();
        assertThat(persistentAuditEvents).hasSize(1);
        PersistentAuditEvent persistentAuditEvent = persistentAuditEvents.get(0);
        assertThat(persistentAuditEvent.getPrincipal()).isEqualTo(event.getPrincipal());
        assertThat(persistentAuditEvent.getAuditEventType()).isEqualTo(event.getType());
        assertThat(persistentAuditEvent.getData()).containsKey("test-key");
        String actualData = persistentAuditEvent.getData().get("test-key");
        assertThat(actualData.length()).isEqualTo(CustomAuditEventRepository.EVENT_DATA_COLUMN_MAX_LENGTH);
        assertThat(actualData).isSubstringOf(largeData);
        assertThat(persistentAuditEvent.getAuditEventDate()).isEqualTo(event.getTimestamp());
    }

    @Test
    public void test_testAddEventWithWebAuthenticationDetails() {
        HttpSession session = new MockHttpSession(null, "test-session-id");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(session);
        request.setRemoteAddr("1.2.3.4");
        WebAuthenticationDetails details = new WebAuthenticationDetails(request);
        Map<String, Object> data = new HashMap<>();
        data.put("test-key", details);
        AuditEvent event = new AuditEvent("test-user", "test-type", data);
        customAuditEventRepository.add(event);
        List<PersistentAuditEvent> persistentAuditEvents = persistenceAuditEventRepository.findAll();
        assertThat(persistentAuditEvents).hasSize(1);
        PersistentAuditEvent persistentAuditEvent = persistentAuditEvents.get(0);
        assertThat(persistentAuditEvent.getData().get("remoteAddress")).isEqualTo("1.2.3.4");
        assertThat(persistentAuditEvent.getData().get("sessionId")).isEqualTo("test-session-id");
    }

    @Test
    public void test_addEventWithNullData() {
        Map<String, Object> data = new HashMap<>();
        data.put("test-key", null);
        AuditEvent event = new AuditEvent("test-user", "test-type", data);
        customAuditEventRepository.add(event);
        List<PersistentAuditEvent> persistentAuditEvents = persistenceAuditEventRepository.findAll();
        assertThat(persistentAuditEvents).hasSize(1);
        PersistentAuditEvent persistentAuditEvent = persistentAuditEvents.get(0);
        assertThat(persistentAuditEvent.getData().get("test-key")).isEqualTo("null");
    }

    @Test
    public void test_addAuditEventWithAnonymousUser() {
        Map<String, Object> data = new HashMap<>();
        data.put("test-key", "test-value");
        AuditEvent event = new AuditEvent(ANONYMOUS_USER, "test-type", data);
        customAuditEventRepository.add(event);
        List<PersistentAuditEvent> persistentAuditEvents = persistenceAuditEventRepository.findAll();
        assertThat(persistentAuditEvents).hasSize(0);
    }

    @Test
    public void test_addAuditEventWithAuthorizationFailureType() {
        Map<String, Object> data = new HashMap<>();
        data.put("test-key", "test-value");
        AuditEvent event = new AuditEvent("test-user", "AUTHORIZATION_FAILURE", data);
        customAuditEventRepository.add(event);
        List<PersistentAuditEvent> persistentAuditEvents = persistenceAuditEventRepository.findAll();
        assertThat(persistentAuditEvents).hasSize(0);
    }
}
