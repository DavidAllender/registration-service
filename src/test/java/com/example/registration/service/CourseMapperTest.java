package com.example.registration.service;

import com.example.registration.dto.Course;
import com.example.registration.model.CourseEntity;
import org.junit.Test;

import java.time.DayOfWeek;
import java.util.Arrays;

import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CourseMapperTest {
    @Test
    public void testMapDTOToEntity() {
        CourseEntity entity = new CourseMapper().map(new Course()
                .setPrerequisites(Arrays.asList("CLASS123", "CLASS456"))
                .setMeetingDays(Arrays.asList(MONDAY, WEDNESDAY))
                .setStartTime("01:02:03")
                .setEndTime("13:14:15")
        );

        assertTrue(entity.getPrerequisites().stream().anyMatch(course -> course.getName().equals("CLASS123")));
        assertTrue(entity.getPrerequisites().stream().anyMatch(course -> course.getName().equals("CLASS456")));
        assertTrue(Arrays.asList(entity.getMeetingDays()).contains(MONDAY));
        assertTrue(Arrays.asList(entity.getMeetingDays()).contains(WEDNESDAY));
        assertEquals("01:02:03", entity.getStartTime());
        assertEquals("13:14:15", entity.getEndTime());
    }

    @Test
    public void testMapEntityToDTO() {
        Course course = new CourseMapper().map(new CourseEntity()
                .setName("name")
                .setPrerequisites(Arrays.asList(
                        new CourseEntity().setName("CLASS123"),
                        new CourseEntity().setName("CLASS456")
                )).setMeetingDays(new DayOfWeek[]{MONDAY, WEDNESDAY})
                .setStartTime("01:02:03")
                .setEndTime("13:14:15")
        );

        assertEquals("name", course.getName());
        assertTrue(course.getPrerequisites().contains("CLASS123"));
        assertTrue(course.getPrerequisites().contains("CLASS456"));
        assertTrue(course.getMeetingDays().contains(MONDAY));
        assertTrue(course.getMeetingDays().contains(WEDNESDAY));
        assertEquals("01:02:03", course.getStartTime());
        assertEquals("13:14:15", course.getEndTime());
    }
}