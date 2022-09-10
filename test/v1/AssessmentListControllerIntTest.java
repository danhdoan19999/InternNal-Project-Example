package com.nals.rw360.v1;

import com.nals.rw360.AbstractTest;
import com.nals.rw360.Review360App;
import com.nals.rw360.api.v1.AssessmentListController;
import com.nals.rw360.blueprints.AssessmentBlueprint;
import com.nals.rw360.blueprints.UserBlueprint;
import com.nals.rw360.domain.Assessment;
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
public class AssessmentListControllerIntTest
    extends AbstractTest {

    @Autowired
    private AssessmentListController assessmentListController;

    private MockMvc restMvc;

    private String baseUrl;

    private Assessment assessment1;
    private Assessment assessment2;

    @Before
    public void setup()
        throws RegisterBlueprintException, CreateModelException {
        this.restMvc = MockMvcBuilders.standaloneSetup(assessmentListController)
                                      .setMessageConverters(getHttpMessageConverters())
                                      .setControllerAdvice(getExceptionTranslator())
                                      .build();

        fakeData();
        this.baseUrl = "/api/v1/assessments";
    }

    @Test
    @Transactional
    public void test_searchAssessments_shouldBeOk()
        throws Exception {
        mockAuthentication(initUser(), ROLE_ADMIN.name());

        String keyword = "assessment";

        restMvc.perform(MockMvcRequestBuilders.get(this.baseUrl)
                                              .param("keyword", keyword))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(matchJsonPath("$.data.items.length()", 2));
    }

    private void fakeData()
        throws RegisterBlueprintException, CreateModelException {
        registerBlueprints(UserBlueprint.class,
                           AssessmentBlueprint.class);

        assessment1 = initAssessment();
        assessment2 = initAssessment();
    }

    private User initUser()
        throws CreateModelException {
        User user = createFakeModel(User.class);

        return getUserRepository().save(user);
    }

    private Assessment initAssessment()
        throws CreateModelException {
        Assessment assessment = createFakeModel(Assessment.class);

        return getAssessmentRepository().save(assessment);
    }
}
