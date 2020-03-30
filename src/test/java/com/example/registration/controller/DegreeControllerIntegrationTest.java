package com.example.registration.controller;

import com.example.registration.dto.Course;
import com.example.registration.dto.Degree;
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

import static com.example.registration.controller.CourseControllerIntegrationTest.COURSE_JSON;
import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
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
@ContextConfiguration(initializers = {DegreeControllerIntegrationTest.Initializer.class})
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
public class DegreeControllerIntegrationTest {
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

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testCreateAndGetDegreeWithNoRequirements() throws Exception {
        mockMvc.perform(put("/degree/d1").contentType(APPLICATION_JSON).content("{\"requirements\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value("d1"))
                .andExpect(jsonPath("requirements").exists())
                .andExpect(jsonPath("requirements").isEmpty());

        mockMvc.perform(get("/degree/d1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value("d1"))
                .andExpect(jsonPath("requirements").exists())
                .andExpect(jsonPath("requirements").isEmpty());
    }

    @Test
    public void testCreateAndGetADegreeWithRequirements() throws Exception {
        mockMvc.perform(put("/course/c1").contentType(APPLICATION_JSON).content(COURSE_JSON));
        mockMvc.perform(put("/course/c2").contentType(APPLICATION_JSON).content(COURSE_JSON));

        mockMvc.perform(put("/degree/d2").contentType(APPLICATION_JSON).content("{\"requirements\":[\"c1\", \"c2\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value("d2"))
                .andExpect(jsonPath("requirements").exists())
                .andExpect(jsonPath("requirements").isNotEmpty());

        mockMvc.perform(get("/degree/d2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value("d2"))
                .andExpect(jsonPath("requirements").exists())
                .andExpect(jsonPath("requirements").isNotEmpty());
    }

    @Test
    public void testThatAddingRequirementsDoesNotOverwriteThoseCourses() throws Exception {
        mockMvc.perform(put("/course/c3").contentType(APPLICATION_JSON).content(COURSE_JSON));
        mockMvc.perform(put("/course/c4").contentType(APPLICATION_JSON).content(COURSE_JSON));

        mockMvc.perform(put("/degree/d3").contentType(APPLICATION_JSON).content("{\"requirements\":[\"c3\", \"c4\"]}"))
                .andExpect(status().isOk());

        Course course4 = new ObjectMapper().readValue(
                mockMvc.perform(get("/course/c4")).andReturn().getResponse().getContentAsString(),
                Course.class
        );

        assertEquals("c4", course4.getName());
        assertTrue(course4.getPrerequisites().isEmpty());
        assertTrue(course4.getMeetingDays().contains(MONDAY));
        assertTrue(course4.getMeetingDays().contains(WEDNESDAY));
        assertTrue(course4.getMeetingDays().contains(FRIDAY));
        assertEquals("01:02:03", course4.getStartTime());
        assertEquals("13:14:15", course4.getEndTime());
    }

    @Test
    public void testCreatingAndThenOverwritingADegree() throws Exception {
        mockMvc.perform(put("/course/c5").contentType(APPLICATION_JSON).content(COURSE_JSON));
        mockMvc.perform(put("/course/c6").contentType(APPLICATION_JSON).content(COURSE_JSON));

        mockMvc.perform(put("/degree/d4").contentType(APPLICATION_JSON).content("{\"requirements\":[\"c5\", \"c6\"]}"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/course/c7").contentType(APPLICATION_JSON).content(COURSE_JSON));
        mockMvc.perform(put("/course/c8").contentType(APPLICATION_JSON).content(COURSE_JSON));

        mockMvc.perform(put("/degree/d4").contentType(APPLICATION_JSON).content("{\"requirements\":[\"c7\", \"c8\"]}"))
                .andExpect(status().isOk());

        Degree degree = new ObjectMapper().readValue(
                mockMvc.perform(get("/degree/d4")).andReturn().getResponse().getContentAsString(),
                Degree.class
        );

        assertEquals("d4", degree.getName());
        assertTrue(degree.getRequirements().contains("c7"));
        assertTrue(degree.getRequirements().contains("c8"));
    }
}
