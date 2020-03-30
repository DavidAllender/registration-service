package com.example.registration.controller;

import com.example.registration.dto.Course;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SqlGroup({
        @Sql(
                scripts = {"classpath:integration-teardown.sql"},
                executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
        )
})
@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = {CourseControllerIntegrationTest.Initializer.class})
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
public class CourseControllerIntegrationTest {

    @ClassRule
    public static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres")
            .withDatabaseName("registration")
            .withUsername("registration")
            .withPassword("registration");

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgres.getJdbcUrl(),
                    "spring.datasource.username=" + postgres.getUsername(),
                    "spring.datasource.password=" + postgres.getPassword()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    public static final String COURSE_JSON = "{" +
            "\"prerequisites\":[]," +
            "\"meetingDays\":[\"MONDAY\", \"WEDNESDAY\",\"FRIDAY\"]," +
            "\"startTime\":\"01:02:03\"," +
            "\"endTime\":\"13:14:15\"" +
            "}";

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testCreateAndGetCourseWithNoPrerequisite() throws Exception {
        mockMvc.perform(put("/course/course1").contentType(APPLICATION_JSON).content(COURSE_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value("course1"))
                .andExpect(jsonPath("meetingDays[0]").value("MONDAY"))
                .andExpect(jsonPath("meetingDays[1]").value("WEDNESDAY"))
                .andExpect(jsonPath("meetingDays[2]").value("FRIDAY"))
                .andExpect(jsonPath("startTime").value("01:02:03"))
                .andExpect(jsonPath("endTime").value("13:14:15"));

        mockMvc.perform(get("/course/course1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value("course1"))
                .andExpect(jsonPath("meetingDays[0]").value("MONDAY"))
                .andExpect(jsonPath("meetingDays[1]").value("WEDNESDAY"))
                .andExpect(jsonPath("meetingDays[2]").value("FRIDAY"))
                .andExpect(jsonPath("startTime").value("01:02:03"))
                .andExpect(jsonPath("endTime").value("13:14:15"));
    }

    @Test
    public void testCreateAndGetCourseWithPrerequisite() throws Exception {
        mockMvc.perform(put("/course/course1").contentType(APPLICATION_JSON).content(COURSE_JSON));
        mockMvc.perform(put("/course/course2").contentType(APPLICATION_JSON).content(COURSE_JSON));

        String course3 = "{" +
                "\"prerequisites\":[\"course1\", \"course2\"]," +
                "\"meetingDays\":[\"MONDAY\", \"WEDNESDAY\",\"FRIDAY\"]," +
                "\"startTime\":\"01:02:03\"," +
                "\"endTime\":\"13:14:15\"" +
                "}";

        mockMvc.perform(put("/course/course3").contentType(APPLICATION_JSON).content(course3))
                .andExpect(status().isOk());

        mockMvc.perform(get("/course/course3"))
                .andExpect(jsonPath("prerequisites").isNotEmpty());

    }

    @Test
    public void testThatAddingPrerequisitesDoesNotOverwriteThoseCourses() throws Exception {
        mockMvc.perform(put("/course/course4").contentType(APPLICATION_JSON).content(COURSE_JSON));
        mockMvc.perform(put("/course/course5").contentType(APPLICATION_JSON).content(COURSE_JSON));

        String course6 = "{" +
                "\"prerequisites\":[\"course4\", \"course5\"]," +
                "\"meetingDays\":[\"MONDAY\", \"WEDNESDAY\",\"FRIDAY\"]," +
                "\"startTime\":\"01:02:03\"," +
                "\"endTime\":\"13:14:15\"" +
                "}";

        mockMvc.perform(put("/course/course6").contentType(APPLICATION_JSON).content(course6))
                .andExpect(status().isOk());

        Course course4 = new ObjectMapper().readValue(
                mockMvc.perform(get("/course/course4")).andReturn().getResponse().getContentAsString(),
                Course.class
        );

        assertEquals("course4", course4.getName());
        assertTrue(course4.getPrerequisites().isEmpty());
        assertTrue(course4.getMeetingDays().contains(MONDAY));
        assertTrue(course4.getMeetingDays().contains(WEDNESDAY));
        assertTrue(course4.getMeetingDays().contains(FRIDAY));
        assertEquals("01:02:03", course4.getStartTime());
        assertEquals("13:14:15", course4.getEndTime());
    }

    @Test
    public void testCreatingAndThenOverwritingACourse() throws Exception {
        mockMvc.perform(put("/course/course7").contentType(APPLICATION_JSON).content(COURSE_JSON));

        String json = "{" +
                "\"prerequisites\":[]," +
                "\"meetingDays\":[\"TUESDAY\", \"THURSDAY\"]," +
                "\"startTime\":\"03:04:05\"," +
                "\"endTime\":\"13:14:15\"" +
                "}";

        mockMvc.perform(put("/course/course7").contentType(APPLICATION_JSON).content(json));

        Course course7 = new ObjectMapper().readValue(
                mockMvc.perform(get("/course/course7")).andReturn().getResponse().getContentAsString(),
                Course.class
        );

        assertEquals("course7", course7.getName());
        assertTrue(course7.getPrerequisites().isEmpty());
        assertTrue(course7.getMeetingDays().contains(TUESDAY));
        assertTrue(course7.getMeetingDays().contains(THURSDAY));
        assertEquals("03:04:05", course7.getStartTime());
        assertEquals("13:14:15", course7.getEndTime());
    }

    @Test
    public void testCourseCanBeAPrerequisiteOfItself() throws Exception {
        String json = "{" +
                "\"prerequisites\":[\"course8\"]," +
                "\"meetingDays\":[\"TUESDAY\", \"THURSDAY\"]," +
                "\"startTime\":\"03:04:05\"," +
                "\"endTime\":\"13:14:15\"" +
                "}";

        mockMvc.perform(put("/course/course8").contentType(APPLICATION_JSON).content(json))
                .andExpect(status().isOk());

        Course course8 = new ObjectMapper().readValue(
                mockMvc.perform(get("/course/course8")).andReturn().getResponse().getContentAsString(),
                Course.class
        );

        assertEquals("course8", course8.getName());
        assertEquals("course8", course8.getPrerequisites().get(0));
    }

    @Test
    public void testCourseCanBeATransitivePrerequisiteOfItself() throws Exception {
        String json9 = "{" +
                "\"prerequisites\":[\"course10\"]," +
                "\"meetingDays\":[\"TUESDAY\", \"THURSDAY\"]," +
                "\"startTime\":\"03:04:05\"," +
                "\"endTime\":\"13:14:15\"" +
                "}";

        String json10 = "{" +
                "\"prerequisites\":[\"course9\"]," +
                "\"meetingDays\":[\"TUESDAY\", \"THURSDAY\"]," +
                "\"startTime\":\"03:04:05\"," +
                "\"endTime\":\"13:14:15\"" +
                "}";


        //In order to create a loop you have to first create course10 so that it can be a prerequisite of 9 and then recreate 10 so it can have 9 added as a prerequisite
        mockMvc.perform(put("/course/course10").contentType(APPLICATION_JSON).content(COURSE_JSON)).andExpect(status().isOk());
        mockMvc.perform(put("/course/course9").contentType(APPLICATION_JSON).content(json9)).andExpect(status().isOk());
        mockMvc.perform(put("/course/course10").contentType(APPLICATION_JSON).content(json10)).andExpect(status().isOk());

        Course course9 = new ObjectMapper().readValue(
                mockMvc.perform(get("/course/course9")).andReturn().getResponse().getContentAsString(),
                Course.class
        );

        Course course10 = new ObjectMapper().readValue(
                mockMvc.perform(get("/course/course10")).andReturn().getResponse().getContentAsString(),
                Course.class
        );

        assertEquals("course9", course9.getName());
        assertEquals("course10", course9.getPrerequisites().get(0));
        assertEquals("course10", course10.getName());
        assertEquals("course9", course10.getPrerequisites().get(0));
    }
}