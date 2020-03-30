package com.example.registration.controller;

import com.example.registration.dto.CreateCourseRequest;
import com.example.registration.dto.CreateDegreeRequest;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
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

@SuppressWarnings("squid:S1192")
@SqlGroup({
        @Sql(
                scripts = {"classpath:integration-teardown.sql"},
                executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
        )
})
@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = {StudentControllerIntegrationTest.Initializer.class})
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
public class StudentControllerIntegrationTest {
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

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test //1
    public void testCreatStudentWithNoMajorOrTranscriptReturns200() throws Exception {
        mockMvc.perform(put("/student/s1").contentType(APPLICATION_JSON).content("{}")).andExpect(status().isOk());
    }

    @Test //2
    public void testCreateStudentWithNoTranscriptReturns200() throws Exception {
        createDegree("D2");
        mockMvc.perform(put("/student/s2").contentType(APPLICATION_JSON).content(student("D2")))
                .andExpect(status().isOk());
    }

    @Test //3
    public void testCreateStudentWithMajorAndTranscriptReturns200() throws Exception {
        createDegree("D3");
        createCourse("D3001");
        createCourse("D3002");

        mockMvc.perform(put("/student/s3").contentType(APPLICATION_JSON).content(student("D3", "D3001", "D3002")))
                .andExpect(status().isOk());
    }

    @Test //4
    public void testGetStudentReturnsAStudent() throws Exception {
        createDegree("D4");
        createCourse("D4001");
        createCourse("D4002");

        mockMvc.perform(put("/student/s4").contentType(APPLICATION_JSON).content(student("D4", "D4001", "D4002")));

        StudentPOJO student = mapper.readValue(mockMvc.perform(get("/student/s4")).andReturn().getResponse().getContentAsString(), StudentPOJO.class);
        assertEquals("s4", student.getName());
        assertEquals("D4", student.getMajor());
        assertTrue(student.getTranscript().contains("D4001"));
        assertTrue(student.getTranscript().contains("D4002"));
    }

    @Test //5
    public void testCreatingTheSameStudentTwiceOverwrites() throws Exception {
        createDegree("D5");
        createCourse("D5001");
        createCourse("D5002");

        mockMvc.perform(put("/student/s5").contentType(APPLICATION_JSON).content(student("D5", "D5001", "D5002")));

        createDegree("D5B");
        createCourse("D5001B");
        createCourse("D5002B");

        mockMvc.perform(put("/student/s5").contentType(APPLICATION_JSON).content(student("D5B", "D5001B", "D5002B")));

        StudentPOJO student = mapper.readValue(mockMvc.perform(get("/student/s5")).andReturn().getResponse().getContentAsString(), StudentPOJO.class);
        assertEquals("s5", student.getName());
        assertEquals("D5B", student.getMajor());
        assertTrue(student.getTranscript().contains("D5001B"));
        assertTrue(student.getTranscript().contains("D5002B"));
    }

    @Test //6
    public void testStudentWithNoMajorHasNeg1SemestersRemaining() throws Exception {
        mockMvc.perform(put("/student/s6").contentType(APPLICATION_JSON).content("{}"));
        mockMvc.perform(get("/student/s6")).andExpect(jsonPath("semestersRemaining").value(-1));
    }

    @Test //7
    public void testStudentWithUnsatisfiedLoopOf1HasNeg1SemestersRemaining() throws Exception {
        createCourse("D7001", prerequisites("D7001"));
        createDegree("D7", requirements("D7001"));
        mockMvc.perform(put("/student/s7").contentType(APPLICATION_JSON).content(student("D7")));
        mockMvc.perform(get("/student/s7")).andExpect(jsonPath("semestersRemaining").value(-1));
    }

    @Test //8
    public void testStudentWithUnsatisfiedLoopOf2HasNeg1SemestersRemaining() throws Exception {
        createCourse("D8002"); //need to pre-create 2
        createCourse("D8001", prerequisites("D8002"));
        createCourse("D8002", prerequisites("D8001"));
        createDegree("D8", requirements("D8001", "D8002"));

        mockMvc.perform(put("/student/s8").contentType(APPLICATION_JSON).content(student("D8")));
        mockMvc.perform(get("/student/s8")).andExpect(jsonPath("semestersRemaining").value(-1));
    }

    @Test //9
    public void testStudentWithUnsatisfiedLoopOf3HasNeg1SemestersRemaining() throws Exception {
        createCourse("D9003");
        createCourse("D9001", prerequisites("D9003"));
        createCourse("D9002", prerequisites("D9001"));
        createCourse("D9003", prerequisites("D9002"));
        createDegree("D9", requirements("D9001", "D9002", "D9003"));

        mockMvc.perform(put("/student/s9").contentType(APPLICATION_JSON).content(student("D9")));
        mockMvc.perform(get("/student/s9")).andExpect(jsonPath("semestersRemaining").value(-1));
    }

    @Test //10
    public void testStudentWithUnsatisfiedCycleContainingGraphHasNeg1SemestersRemaining() throws Exception {
        createCourse("D10.05");
        createCourse("D10.04");
        createCourse("D10.03", prerequisites("D10.04", "D10.05"));
        createCourse("D10.06");
        createCourse("D10.07");
        createCourse("D10.01", prerequisites("D10.03", "D10.06", "D10.07"));
        createCourse("D10.02", prerequisites("D10.01"));
        createCourse("D10.03", prerequisites("D10.02"));
        createDegree("D10", requirements("D10.01", "D10.02", "D10.03", "D10.04", "D10.05", "D10.06", "D10.07"));

        mockMvc.perform(put("/student/s10").contentType(APPLICATION_JSON).content(student("D10")));
        mockMvc.perform(get("/student/s10")).andExpect(jsonPath("semestersRemaining").value(-1));
    }

    @Test //11
    public void testStudentWithSatisfiedLoopOf1Has0SemestersRemaining() throws Exception {
        createCourse("D11.01", prerequisites("D11.01"));
        createDegree("D11", requirements("D11.01"));
        mockMvc.perform(put("/student/s11").contentType(APPLICATION_JSON).content(student("D11", "D11.01")));
        mockMvc.perform(get("/student/s11")).andExpect(jsonPath("semestersRemaining").value(0));
    }

    @Test //12
    public void testStudentWithSatisfiedLoopOf2Has1SemestersRemaining() throws Exception {
        createCourse("D12.02"); //need to pre-create 2
        createCourse("D12.01", prerequisites("D12.02"));
        createCourse("D12.02", prerequisites("D12.01"));
        createDegree("D12", requirements("D12.01", "D12.02"));

        mockMvc.perform(put("/student/s12").contentType(APPLICATION_JSON).content(student("D12", "D12.01")));
        mockMvc.perform(get("/student/s12")).andExpect(jsonPath("semestersRemaining").value(1));
    }

    @Test //13
    public void testStudentWithSatisfiedLoopOf3Has2SemestersRemaining() throws Exception {
        createCourse("D13.03");
        createCourse("D13.01", prerequisites("D13.03"));
        createCourse("D13.02", prerequisites("D13.01"));
        createCourse("D13.03", prerequisites("D13.02"));
        createDegree("D13", requirements("D13.01", "D13.02", "D13.03"));

        mockMvc.perform(put("/student/s13").contentType(APPLICATION_JSON).content(student("D13", "D13.01")));
        mockMvc.perform(get("/student/s13")).andExpect(jsonPath("semestersRemaining").value(2));
    }

    @Test //14
    public void testStudentWithSatisfiedCycleContainingGraphHas2SemestersRemaining() throws Exception {
        createCourse("D14.05");
        createCourse("D14.04");
        createCourse("D14.03", prerequisites("D14.04", "D14.05"));
        createCourse("D14.06");
        createCourse("D14.07");
        createCourse("D14.01", prerequisites("D14.03", "D14.06", "D14.07"));
        createCourse("D14.02", prerequisites("D14.01"));
        createCourse("D14.03", prerequisites("D14.02"));
        createDegree("D14", requirements("D14.01", "D14.02", "D14.03", "D14.04", "D14.05", "D14.06", "D14.07"));

        mockMvc.perform(put("/student/s14").contentType(APPLICATION_JSON).content(student("D14", "D14.03", "D14.05", "D14.04", "D14.06", "D14.07")));
        mockMvc.perform(get("/student/s14")).andExpect(jsonPath("semestersRemaining").value(2));
    }

    @Test //15
    public void testStudentWithSimpleLinearGraphHasGraphLengthSemestersRemaining() throws Exception {
        createCourse("D15.01");
        createCourse("D15.02", prerequisites("D15.01"));
        createCourse("D15.03", prerequisites("D15.02"));
        createCourse("D15.04", prerequisites("D15.03"));
        createDegree("D15", requirements("D15.01", "D15.02", "D15.03", "D15.04"));

        mockMvc.perform(put("/student/s15").contentType(APPLICATION_JSON).content(student("D15")));
        mockMvc.perform(get("/student/s15")).andExpect(jsonPath("semestersRemaining").value(4));
    }

    @Test //16
    public void testStudentWithSimpleLinearGraphHasGraphLengthMinusTranscriptSemestersRemaining() throws Exception {
        createCourse("D16.01");
        createCourse("D16.02", prerequisites("D16.01"));
        createCourse("D16.03", prerequisites("D16.02"));
        createCourse("D16.04", prerequisites("D16.03"));
        createDegree("D16", requirements("D16.01", "D16.02", "D16.03", "D16.04"));

        mockMvc.perform(put("/student/s16").contentType(APPLICATION_JSON).content(student("D16", "D16.01", "D16.02")));
        mockMvc.perform(get("/student/s16")).andExpect(jsonPath("semestersRemaining").value(2));
    }

    @Test //17
    public void testStudentWithSimpleLinearGraphHasGraphLengthMinusTranscriptSemestersRemainingEvenWhenTheyCompleteClassesOutOfOrder() throws Exception {
        createCourse("D17.01");
        createCourse("D17.02", prerequisites("D17.01"));
        createCourse("D17.03", prerequisites("D17.02"));
        createCourse("D17.04", prerequisites("D17.03"));
        createDegree("D17", requirements("D17.01", "D17.02", "D17.03", "D17.04"));

        mockMvc.perform(put("/student/s17").contentType(APPLICATION_JSON).content(student("D17", "D17.01", "D17.03")));
        mockMvc.perform(get("/student/s17")).andExpect(jsonPath("semestersRemaining").value(2));
    }

    @Test //18
    public void testBinaryTreeHasNodeCountSemesterRemaining() throws Exception {
        createCourse("D18.01");
        createCourse("D18.02");
        createCourse("D18.03");
        createCourse("D18.04");
        createCourse("D18.05", prerequisites("D18.01", "D18.02"));
        createCourse("D18.06", prerequisites("D18.03", "D18.04"));
        createCourse("D18.07", prerequisites("D18.05", "D18.06"));
        createDegree("D18", requirements("D18.01", "D18.02", "D18.03", "D18.04", "D18.05", "D18.06", "D18.07"));

        mockMvc.perform(put("/student/s18").contentType(APPLICATION_JSON).content(student("D18")));
        mockMvc.perform(get("/student/s18")).andExpect(jsonPath("semestersRemaining").value(7));
    }

    @Test //19
    public void testBinaryTreeHasHeightSemesterRemainingWhenClassesOnDifferentDays() throws Exception {
        createCourse("D19.01", noPrerequisites(), meets(MONDAY));
        createCourse("D19.02", noPrerequisites(), meets(TUESDAY));
        createCourse("D19.03", noPrerequisites(), meets(WEDNESDAY));
        createCourse("D19.04", noPrerequisites(), meets(THURSDAY));
        createCourse("D19.05", prerequisites("D19.01", "D19.02"), meets(MONDAY));
        createCourse("D19.06", prerequisites("D19.03", "D19.04"), meets(TUESDAY));
        createCourse("D19.07", prerequisites("D19.05", "D19.06"), meets(MONDAY));
        createDegree("D19", requirements("D19.01", "D19.02", "D19.03", "D19.04", "D19.05", "D19.06", "D19.07"));

        mockMvc.perform(put("/student/s19").contentType(APPLICATION_JSON).content(student("D19")));
        mockMvc.perform(get("/student/s19")).andExpect(jsonPath("semestersRemaining").value(3));
    }

    @Test //20
    public void testSemestersRemainingOnlyCaresAboutThingsThatMatterToAStudentsMajor() throws Exception {
        createCourse("D20.01");
        createCourse("D20.02", prerequisites("D20.01"));
        createCourse("D20.03");
        createCourse("D20.04", prerequisites("D20.03"));
        createCourse("D20.05", prerequisites("D20.02", "D20.04"));
        createCourse("D20.06", prerequisites("D20.05"));
        createCourse("D20.07", prerequisites("D20.06"));
        createCourse("D20.08", prerequisites("D20.06", "D20.07"));
        createDegree("D20", requirements("D20.06"));

        mockMvc.perform(put("/student/s20").contentType(APPLICATION_JSON).content(student("D20", "D20.05")));
        mockMvc.perform(get("/student/s20")).andExpect(jsonPath("semestersRemaining").value(1));
    }

    @Test //21
    public void testSemestersRemainingIsNodeCountWhenEachDegreeRequirementHasIndependentTree() throws Exception {
        createCourse("D21.02");
        createCourse("D21.01");
        createCourse("D21.03", prerequisites("D21.01", "D21.02"));
        createCourse("D21.04");
        createCourse("D21.05", prerequisites("D21.04"));
        createCourse("D21.06", prerequisites("D21.05"));
        createCourse("D21.07", prerequisites("D21.06"));
        createCourse("D21.08");
        createDegree("D21", requirements("D21.03", "D21.07", "D21.08"));

        mockMvc.perform(put("/student/s21").contentType(APPLICATION_JSON).content(student("D21")));
        mockMvc.perform(get("/student/s21")).andExpect(jsonPath("semestersRemaining").value(8));
    }

    @Test //22
    public void testSemestersRemainingIsNodeCountWithUpsideDownTriangle() throws Exception {
        createCourse("D22.01");
        createCourse("D22.02", prerequisites("D22.01"));
        createCourse("D22.03", prerequisites("D22.01"));
        createCourse("D22.04", prerequisites("D22.02"));
        createCourse("D22.05", prerequisites("D22.02"));
        createCourse("D22.06", prerequisites("D22.03"));
        createCourse("D22.07", prerequisites("D22.03"));
        createDegree("D22", requirements("D22.07", "D22.06", "D22.05", "D22.04"));

        mockMvc.perform(put("/student/s22").contentType(APPLICATION_JSON).content(student("D22")));
        mockMvc.perform(get("/student/s22")).andExpect(jsonPath("semestersRemaining").value(7));
    }

    @Test //23
    public void testSemestersRemainingIsDepthWithUpsideDownTriangleClassesOnDifferentDays() throws Exception {
        createCourse("D23.01", noPrerequisites(), meets(MONDAY));
        createCourse("D23.02", prerequisites("D23.01"), meets(MONDAY));
        createCourse("D23.03", prerequisites("D23.01"), meets(TUESDAY));
        createCourse("D23.04", prerequisites("D23.02"), meets(MONDAY));
        createCourse("D23.05", prerequisites("D23.02"), meets(TUESDAY));
        createCourse("D23.06", prerequisites("D23.03"), meets(WEDNESDAY));
        createCourse("D23.07", prerequisites("D23.03"), meets(THURSDAY));
        createDegree("D23", requirements("D23.07", "D23.06", "D23.05", "D23.04"));

        mockMvc.perform(put("/student/s23").contentType(APPLICATION_JSON).content(student("D23")));
        mockMvc.perform(get("/student/s23")).andExpect(jsonPath("semestersRemaining").value(3));
    }

    @Test //24
    public void testSemestersRemainingIsMaxDepthWhenEachDegreeRequirementHasIndependentTreeButClassesOnDifferentDays() throws Exception {
        createCourse("D24.00", noPrerequisites(), meets(MONDAY));
        createCourse("D24.01", prerequisites("D24.00"), meets(MONDAY));
        createCourse("D24.02", prerequisites("D24.01"), meets(MONDAY));
        createCourse("D24.03", prerequisites("D24.02"), meets(MONDAY));
        createCourse("D24.04", prerequisites("D24.04"), meets(TUESDAY));
        createCourse("D24.05", noPrerequisites(), meets(WEDNESDAY));
        createCourse("D24.06", noPrerequisites(), meets(THURSDAY));
        createCourse("D24.07", noPrerequisites(), meets(FRIDAY));
        createCourse("D24.08", noPrerequisites(), meets(SATURDAY));
        createCourse("D24.09", prerequisites("D24.05", "D24.06"), meets(WEDNESDAY));
        createCourse("D24.10", prerequisites("D24.07", "D24.08"), meets(THURSDAY));
        createCourse("D24.10", prerequisites("D24.08", "D24.09"), meets(WEDNESDAY));

        createDegree("D24", requirements("D24.02", "D24.03", "D24.04", "D24.10"));

        mockMvc.perform(put("/student/s24").contentType(APPLICATION_JSON).content(student("D24", "D24.04")));
        mockMvc.perform(get("/student/s24")).andExpect(jsonPath("semestersRemaining").value(4));
    }

    @Test //25
    public void testSemestersRemainingIsMaxDepthWhenEachDegreeRequirementHasIndependentTreeButClassesAtDifferentTimes() throws Exception {
        createCourse("D25.00", noPrerequisites(), meets(NINE, TEN, MONDAY));
        createCourse("D25.01", prerequisites("D25.00"), meets(NINE, TEN, MONDAY));
        createCourse("D25.02", prerequisites("D25.01"), meets(NINE, TEN, MONDAY));
        createCourse("D25.03", prerequisites("D25.02"), meets(NINE, TEN, MONDAY));
        createCourse("D25.04", prerequisites("D25.04"), meets(ELEVEN, TWELVE, MONDAY));
        createCourse("D25.05", noPrerequisites(), meets(FOURTEEN, FIFTEEN, MONDAY));
        createCourse("D25.06", noPrerequisites(), meets(SIXTEEN, SEVENTEEN, MONDAY));
        createCourse("D25.07", noPrerequisites(), meets(EIGHTEEN, NINETEEN, MONDAY));
        createCourse("D25.08", noPrerequisites(), meets(TWENTY, TWENTY_ONE));
        createCourse("D25.09", prerequisites("D25.05", "D25.06"), meets(FOURTEEN, FIFTEEN, MONDAY));
        createCourse("D25.10", prerequisites("D25.07", "D25.08"), meets(SIXTEEN, SEVENTEEN, MONDAY));
        createCourse("D25.10", prerequisites("D25.08", "D25.09"), meets(FOURTEEN, FIFTEEN, MONDAY));

        createDegree("D25", requirements("D25.02", "D25.03", "D25.04", "D25.10"));

        mockMvc.perform(put("/student/s25").contentType(APPLICATION_JSON).content(student("D25", "D25.04")));
        mockMvc.perform(get("/student/s25")).andExpect(jsonPath("semestersRemaining").value(4));
    }

    @Test //26
    public void testCanCreateMultipleStudentsWithTheSameMajor() throws Exception {
        createDegree("D26");

        mockMvc.perform(put("/student/s26A").contentType(APPLICATION_JSON).content(student("D26")));
        mockMvc.perform(put("/student/s26B").contentType(APPLICATION_JSON).content(student("D26")));

        mockMvc.perform(get("/student/s26A")).andExpect(jsonPath("major").value("D26"));
        mockMvc.perform(get("/student/s26B")).andExpect(jsonPath("major").value("D26"));
    }

    @Test //27
    public void testStudentWhoHasAlreadyCompletedDegreeHasZeroSemestersRemaining() throws Exception {
        createCourse("D27.00", noPrerequisites(), meets(NINE, TEN, MONDAY));
        createCourse("D27.01", prerequisites("D27.00"), meets(NINE, TEN, MONDAY));
        createCourse("D27.02", prerequisites("D27.01"), meets(NINE, TEN, MONDAY));
        createCourse("D27.03", prerequisites("D27.02"), meets(NINE, TEN, MONDAY));
        createCourse("D27.04", prerequisites("D27.04"), meets(ELEVEN, TWELVE, MONDAY));
        createCourse("D27.05", noPrerequisites(), meets(FOURTEEN, FIFTEEN, MONDAY));
        createCourse("D27.06", noPrerequisites(), meets(SIXTEEN, SEVENTEEN, MONDAY));
        createCourse("D27.07", noPrerequisites(), meets(EIGHTEEN, NINETEEN, MONDAY));
        createCourse("D27.08", noPrerequisites(), meets(TWENTY, TWENTY_ONE));
        createCourse("D27.09", prerequisites("D27.05", "D27.06"), meets(FOURTEEN, FIFTEEN, MONDAY));
        createCourse("D27.10", prerequisites("D27.07", "D27.08"), meets(SIXTEEN, SEVENTEEN, MONDAY));
        createCourse("D27.10", prerequisites("D27.08", "D27.09"), meets(FOURTEEN, FIFTEEN, MONDAY));

        createDegree("D27", requirements("D27.02", "D27.03", "D27.04", "D27.10"));

        mockMvc.perform(put("/student/s27").contentType(APPLICATION_JSON).content(student("D27", "D27.02", "D27.03", "D27.04", "D27.10")));
        mockMvc.perform(get("/student/s27")).andExpect(jsonPath("semestersRemaining").value(0));
    }

    @Test //28
    public void testSemestersRemainingIs1WhenIndependentClassesAreAtDifferentTimesOnTheSameDay() throws Exception {
        createCourse("D28.01", noPrerequisites(), meets(NINE, TEN, MONDAY));
        createCourse("D28.02", noPrerequisites(), meets(ELEVEN, TWELVE, MONDAY));
        createCourse("D28.03", noPrerequisites(), meets(THIRTEEN, FOURTEEN, MONDAY));
        createDegree("D28", requirements("D28.01", "D28.02", "D28.03"));

        mockMvc.perform(put("/student/s28").contentType(APPLICATION_JSON).content(student("D28")));
        mockMvc.perform(get("/student/s28")).andExpect(jsonPath("semestersRemaining").value(1));
    }

    @Test //DS9
    public void testSemestersRemainingAccountsForOverlappingClasses() throws Exception {
        createCourse("DS9.01", noPrerequisites(), meets(NINE, ELEVEN, MONDAY));
        createCourse("DS9.02", noPrerequisites(), meets(TEN, TWELVE, MONDAY));
        createDegree("DS9", requirements("DS9.01", "DS9.02"));

        mockMvc.perform(put("/student/sisko").contentType(APPLICATION_JSON).content(student("DS9")));
        mockMvc.perform(get("/student/sisko")).andExpect(jsonPath("semestersRemaining").value(2));
    }

    @Test //30
    public void testSemestersRemainingAccountsForEndTimeBeingExclusive_1() throws Exception {
        createCourse("D30.01", noPrerequisites(), meets(NINE, TEN, MONDAY));
        createCourse("D30.02", noPrerequisites(), meets(TEN, ELEVEN, MONDAY));
        createDegree("D30", requirements("D30.01", "D30.02"));

        mockMvc.perform(put("/student/s30").contentType(APPLICATION_JSON).content(student("D30")));
        mockMvc.perform(get("/student/s30")).andExpect(jsonPath("semestersRemaining").value(1));
    }

    @Test //31
    public void testSemestersRemainingAccountsForEndTimeBeingExclusive_2() throws Exception {
        createCourse("D31.01", noPrerequisites(), meets(NINE, TEN, MONDAY));
        createCourse("D31.02", noPrerequisites(), meets(TEN, ELEVEN, MONDAY));
        createCourse("D31.03", noPrerequisites(), meets(ELEVEN, TWELVE, MONDAY));
        createDegree("D31", requirements("D31.01", "D31.02", "D31.02"));

        mockMvc.perform(put("/student/s31").contentType(APPLICATION_JSON).content(student("D31")));
        mockMvc.perform(get("/student/s31")).andExpect(jsonPath("semestersRemaining").value(1));
    }

    @Test //32
    public void testSemestersRemainingAccountsForComplexOverlap() throws Exception {
        createCourse("D32.01", noPrerequisites(), meets(NINE, TEN, MONDAY));
        createCourse("D32.02", noPrerequisites(), meets(NINE_THIRTY, TEN_THIRTY, MONDAY));
        createCourse("D32.03", noPrerequisites(), meets(TEN, ELEVEN, MONDAY));
        createCourse("D32.04", noPrerequisites(), meets(TEN_THIRTY, ELEVEN_THIRTY, MONDAY));
        createCourse("D33.05", noPrerequisites(), meets(ELEVEN, TWELVE, MONDAY));

        createDegree("D32", requirements("D32.01", "D32.02", "D32.03"));

        mockMvc.perform(put("/student/s32").contentType(APPLICATION_JSON).content(student("D32")));
        mockMvc.perform(get("/student/s32")).andExpect(jsonPath("semestersRemaining").value(2));
    }

    @Test //33
    public void testSemestersRemainingOptimizesForEscapingCourseOverlap() throws Exception {
        createCourse("D33.01", noPrerequisites(), meets(NINE, TEN, MONDAY));
        createCourse("D33.02", noPrerequisites(), meets(NINE, TEN, MONDAY));
        createCourse("D33.03", noPrerequisites(), meets(ELEVEN, TWELVE, MONDAY));
        createCourse("D33.04", noPrerequisites(), meets(ELEVEN, TWELVE, MONDAY));
        createCourse("D33.05", prerequisites("D33.02", "D33.03"), meets(THIRTEEN, FOURTEEN, TUESDAY));
        createDegree("D33", requirements("D33.01", "D33.02", "D33.03", "D33.04", "D33.05"));

        mockMvc.perform(put("/student/s33").contentType(APPLICATION_JSON).content(student("D33")));
        mockMvc.perform(get("/student/s33")).andExpect(jsonPath("semestersRemaining").value(2));
    }

    @Test //34
    public void testStudentWithTranscriptButNoMajorHasNeg1SemestersRemaining() throws Exception {
        createCourse("D34.01", noPrerequisites(), meets(NINE, TEN, MONDAY));
        createCourse("D34.02", noPrerequisites(), meets(NINE, TEN, MONDAY));
        createCourse("D34.03", noPrerequisites(), meets(ELEVEN, TWELVE, MONDAY));
        createCourse("D34.04", noPrerequisites(), meets(ELEVEN, TWELVE, MONDAY));
        createCourse("D34.05", prerequisites("D34.02", "D34.03"), meets(THIRTEEN, FOURTEEN, TUESDAY));
        createDegree("D34", requirements("D34.01", "D34.02", "D34.03", "D34.04", "D34.05"));

        mockMvc.perform(put("/student/s34").contentType(APPLICATION_JSON).content(student(null, "D34.01", "D34.02", "D34.03", "D34.04", "D34.05")));
        mockMvc.perform(get("/student/s34")).andExpect(jsonPath("semestersRemaining").value(-1));
    }

    @Test //35
    public void testRealisticallyComplicatedDegreeAndClasses_1() throws Exception {
        createCourse("FLUF101", noPrerequisites(), meets(THIRTEEN, FOURTEEN, MONDAY, WEDNESDAY, FRIDAY));
        createCourse("FLUF102", noPrerequisites(), meets(THIRTEEN, FOURTEEN, TUESDAY, THURSDAY));
        createCourse("FLUF103", noPrerequisites(), meets(MONDAY));
        createCourse("DEBT101", noPrerequisites(), meets("00:00:00", "23:59:59", MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY));
        createCourse("BS102", noPrerequisites(), meets(FIFTEEN, SEVENTEEN, MONDAY, TUESDAY));
        createCourse("ENG101", noPrerequisites(), meets(NINE, ELEVEN, MONDAY, WEDNESDAY));
        createCourse("PSYCH101", noPrerequisites(), meets(TEN_THIRTY, TWELVE, THURSDAY, FRIDAY));
        createCourse("CS130", noPrerequisites(), meets(NINE, TEN_THIRTY, MONDAY, WEDNESDAY, FRIDAY));
        createCourse("MATH120", noPrerequisites(), meets(NINE, TEN_THIRTY, MONDAY, WEDNESDAY, FRIDAY));
        createCourse("MATH130", prerequisites("MATH120"), meets(NINE, TEN_THIRTY, MONDAY, WEDNESDAY, FRIDAY));
        createCourse("MATH140", prerequisites("MATH130"), meets(NINE, TEN_THIRTY, MONDAY, WEDNESDAY, FRIDAY));
        createCourse("MATH220", noPrerequisites(), meets(THIRTEEN, SIXTEEN, THURSDAY));
        createCourse("MATH340", prerequisites("MATH140", "MATH220"), meets(FIFTEEN, SIXTEEN, TUESDAY, THURSDAY));
        createCourse("CS230", prerequisites("CS130", "MATH130"), meets(FOURTEEN, FIFTEEN, MONDAY, TUESDAY, WEDNESDAY));
        createCourse("CS231", prerequisites("CS230"), meets(NINE, ELEVEN, THURSDAY, FRIDAY));
        createCourse("CS340", prerequisites("CS130", "CS230", "MATH120"), meets(FIFTEEN, SEVENTEEN, THURSDAY, FRIDAY));
        createCourse("CS350", prerequisites("CS231", "MATH130"), meets(TEN, ELEVEN_THIRTY, MONDAY, WEDNESDAY, FRIDAY));
        createCourse("CS380", prerequisites("CS231", "CS230", "MATH140"), meets(NINE, TEN, TUESDAY, THURSDAY, FRIDAY));
        createCourse("CS400", prerequisites("CS380"), meets(NINE, TEN, TUESDAY));

        createDegree("CS", requirements("FLUF101", "FLUF102", "ENG101", "PSYCH101", "CS130", "CS230", "MATH140", "CS231", "CS340", "CS350", "CS380", "CS400"));

        mockMvc.perform(put("/student/s35").contentType(APPLICATION_JSON).content(student("CS",
                "FLUF101", "FLUF103", "DEBT101", "BS102", "MATH120", "MATH130"
        )));
        mockMvc.perform(get("/student/s35")).andExpect(jsonPath("semestersRemaining").value(4));
    }

    private String student(String major, String... transcript) throws JsonProcessingException {
        return mapper.writeValueAsString(new StudentPOJO().setMajor(major).setTranscript(Arrays.asList(transcript)));
    }

    private void createDegree(String name) throws Exception {
        createDegree(name, Collections.emptyList());
    }

    private void createDegree(String name, List<String> requirements) throws Exception {
        String json = mapper.writeValueAsString(new CreateDegreeRequest().setRequirements(requirements));
        mockMvc.perform(put("/degree/" + name).contentType(APPLICATION_JSON).content(json));
    }

    private void createCourse(String name) throws Exception {
        createCourse(name, noPrerequisites(), Collections.singletonList(MONDAY), "14:00:00", "16:00:00");
    }

    private void createCourse(String name, List<String> prerequisites) throws Exception {
        createCourse(name, prerequisites, Collections.singletonList(MONDAY), "14:00:00", "16:00:00");
    }

    private void createCourse(String name, List<String> prerequisites, ClassSchedule schedule) throws Exception {
        createCourse(name, prerequisites, schedule.getDays(), schedule.getStartTime(), schedule.getEndTime());
    }

    private void createCourse(String name, List<String> prerequisites, List<DayOfWeek> meetingDays, String startTime, String endTime) throws Exception {
        String json = mapper.writeValueAsString(new CreateCourseRequest()
                .setPrerequisites(prerequisites)
                .setMeetingDays(meetingDays)
                .setStartTime(startTime)
                .setEndTime(endTime)
        );
        mockMvc.perform(put("/course/" + name).contentType(APPLICATION_JSON).content(json));
    }

    private List<String> noPrerequisites() {
        return Collections.emptyList();
    }

    private List<String> prerequisites(String... prerequisites) {
        return Arrays.asList(prerequisites);
    }

    private List<String> requirements(String... prerequisites) {
        return Arrays.asList(prerequisites);
    }

    private ClassSchedule meets(DayOfWeek day) {
        return new ClassSchedule().setDays(Collections.singletonList(day));
    }

    private ClassSchedule meets(String startTime, String endTime, DayOfWeek... days) {
        return new ClassSchedule().setStartTime(startTime).setEndTime(endTime).setDays(Arrays.asList(days));
    }

    private static final String NINE = "09:00:00";
    private static final String NINE_THIRTY = "09:30:00";
    private static final String TEN = "10:00:00";
    private static final String TEN_THIRTY = "10:30:00";
    private static final String ELEVEN = "11:00:00";
    private static final String ELEVEN_THIRTY = "11:30:00";
    private static final String TWELVE = "12:00:00";
    private static final String THIRTEEN = "13:00:00";
    private static final String FOURTEEN = "14:00:00";
    private static final String FIFTEEN = "15:00:00";
    private static final String SIXTEEN = "16:00:00";
    private static final String SEVENTEEN = "17:00:00";
    private static final String EIGHTEEN = "18:00:00";
    private static final String NINETEEN = "19:00:00";
    private static final String TWENTY = "20:00:00";
    private static final String TWENTY_ONE = "21:00:00";


    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class StudentPOJO {
        private String name;
        private String major;
        private List<String> transcript;
        private Integer semestersRemaining;

        public String getName() {
            return name;
        }

        public StudentPOJO setName(String name) {
            this.name = name;
            return this;
        }

        public String getMajor() {
            return major;
        }

        public StudentPOJO setMajor(String major) {
            this.major = major;
            return this;
        }

        public List<String> getTranscript() {
            return transcript;
        }

        public StudentPOJO setTranscript(List<String> transcript) {
            this.transcript = transcript;
            return this;
        }

        public Integer getSemestersRemaining() {
            return semestersRemaining;
        }

        public StudentPOJO setSemestersRemaining(Integer semestersRemaining) {
            this.semestersRemaining = semestersRemaining;
            return this;
        }
    }

    private static class ClassSchedule {
        private String startTime = "15:00:00";
        private String endTime = "16:00:00";
        private List<DayOfWeek> days;

        public String getStartTime() {
            return startTime;
        }

        public ClassSchedule setStartTime(String startTime) {
            this.startTime = startTime;
            return this;
        }

        public String getEndTime() {
            return endTime;
        }

        public ClassSchedule setEndTime(String endTime) {
            this.endTime = endTime;
            return this;
        }

        public List<DayOfWeek> getDays() {
            return days;
        }

        public ClassSchedule setDays(List<DayOfWeek> days) {
            this.days = days;
            return this;
        }
    }
}
