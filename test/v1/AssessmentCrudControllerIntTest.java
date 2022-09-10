package com.nals.rw360.v1;

import com.nals.rw360.AbstractTest;
import com.nals.rw360.Review360App;
import com.nals.rw360.api.v1.AssessmentCrudController;
import com.nals.rw360.blueprints.AssessmentBlueprint;
import com.nals.rw360.blueprints.UserBlueprint;
import com.nals.rw360.blueprints.request.AssessmentCreateReqBlueprint;
import com.nals.rw360.domain.Assessment;
import com.nals.rw360.domain.User;
import com.nals.rw360.dto.v1.request.assessment.AssessmentCreateReq;
import com.nals.rw360.dto.v1.response.DataRes;
import com.nals.rw360.helpers.DateHelper;
import com.nals.rw360.helpers.JsonHelper;
import com.nals.rw360.helpers.StringHelper;
import com.nals.rw360.helpers.TestHelper;
import com.nals.rw360.service.v1.FileService;
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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.transaction.Transactional;

import static com.nals.rw360.enums.RoleType.ROLE_ADMIN;
import static com.nals.rw360.errors.ErrorCodes.ASSESSMENT_NAME_ALREADY_USED;
import static com.nals.rw360.errors.ErrorCodes.INVALID_END_DATE;
import static com.nals.rw360.helpers.DateHelper.ASIA_HCM_ZONE_ID;
import static com.nals.rw360.helpers.TestHelper.APPLICATION_JSON_UTF8;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Review360App.class)
public class AssessmentCrudControllerIntTest
    extends AbstractTest {

    @Autowired
    private AssessmentCrudController assessmentCrudController;

    private FileService fileService;

    private MockMvc restMvc;

    private String baseUrl;

    @Before
    public void setup()
        throws RegisterBlueprintException {
        this.restMvc = MockMvcBuilders.standaloneSetup(assessmentCrudController)
                                      .setMessageConverters(getHttpMessageConverters())
                                      .setControllerAdvice(getExceptionTranslator())
                                      .build();

        this.baseUrl = "/api/v1/assessments";

        fakeData();
    }

    @Test
    @Transactional
    public void test_createAssessment_shouldBeOk()
        throws Exception {
        mockAuthentication(initUser(), ROLE_ADMIN.name());

        AssessmentCreateReq req = initAssessmentCreateReq();
        req.setStartDate(DateHelper.toLongWithTimeAtStartOfDay(DateHelper.toInstant(req.getStartDate())));
        req.setEndDate(DateHelper.toMillis(DateHelper.toInstantWithTimeAtEndOfDay(req.getEndDate())));

        MvcResult mvcResult = restMvc.perform(MockMvcRequestBuilders.post(baseUrl)
                                                                    .contentType(APPLICATION_JSON_UTF8)
                                                                    .content(TestHelper.convertObjectToJsonBytes(req)))
                                     .andExpect(MockMvcResultMatchers.status().isCreated())
                                     .andReturn();

        DataRes response = JsonHelper.readValue(mvcResult.getResponse().getContentAsString(), DataRes.class);

        Long assessmentId = StringHelper.toNumber(response.getData().toString(), Long.class);
        Assessment assessment = getAssessmentRepository().findOneById(assessmentId).orElse(null);

        Assertions.assertThat(assessment).isNotNull();
        Assertions.assertThat(assessment.getName()).isEqualTo(req.getName());
        Assertions.assertThat(assessment.getDescription()).isEqualTo(req.getDescription());
        Assertions.assertThat(assessment.getStartDate()).isEqualTo(DateHelper.toInstant(req.getStartDate()));
        Assertions.assertThat(assessment.getEndDate()).isEqualTo(DateHelper.toInstant(req.getEndDate()));

        Assertions.assertThat(getAssessmentRepository().findAllById(assessmentId)).hasSize(1);
    }

    @Test
    @Transactional
    public void test_createAssessment_assessmentNameAlreadyUsed_shouldBeBadRequest()
        throws Exception {
        mockAuthentication(initUser(), ROLE_ADMIN.name());

        String existedName = "NALS";

        var assessment = createFakeModel(Assessment.class);
        assessment.setName(existedName);
        getAssessmentRepository().save(assessment);

        var req = initAssessmentCreateReq();
        req.setName(existedName);

        restMvc.perform(MockMvcRequestBuilders.post(baseUrl)
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(req)))
               .andExpect(MockMvcResultMatchers.status().isBadRequest())
               .andExpect(matchJsonPath("$.errors[0].error_code", ASSESSMENT_NAME_ALREADY_USED))
               .andExpect(matchJsonPath("$.errors[0].field", "name"))
               .andExpect(matchJsonPath("$.errors[0].message", "Assessment name already used"));
    }

    @Test
    @Transactional
    public void test_createAssessment_startDateGreaterThanEndDate_shouldBeBadRequest()
        throws Exception {
        mockAuthentication(initUser(), ROLE_ADMIN.name());

        var req = initAssessmentCreateReq();
        Long dateEndMillis = DateHelper.toMillis(DateHelper.getStartTimeOfCurrentYear());
        req.setEndDate(dateEndMillis);

        restMvc.perform(MockMvcRequestBuilders.post(baseUrl)
                                              .contentType(APPLICATION_JSON_UTF8)
                                              .content(TestHelper.convertObjectToJsonBytes(req)))
               .andDo(MockMvcResultHandlers.print())
               .andExpect(MockMvcResultMatchers.status().isBadRequest())
               .andExpect(matchJsonPath("$.errors[0].error_code", INVALID_END_DATE))
               .andExpect(matchJsonPath("$.errors[0].field", "end_date"))
               .andExpect(matchJsonPath("$.errors[0].message",
                                        "End date must be greater than start date"));
    }

    private void fakeData()
        throws RegisterBlueprintException {
        registerBlueprints(UserBlueprint.class,
                           AssessmentCreateReqBlueprint.class,
                           AssessmentBlueprint.class);
    }

    private User initUser()
        throws CreateModelException {
        User user = createFakeModel(User.class);
        return getUserRepository().save(user);
    }

    private AssessmentCreateReq initAssessmentCreateReq()
        throws RegisterBlueprintException, CreateModelException {
        registerBlueprints(AssessmentCreateReqBlueprint.class);
        return createFakeModel(AssessmentCreateReq.class);
    }
}
