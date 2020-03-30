package com.example.registration.dto;

import org.junit.Test;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class CourseTest {
    @Test
    public void testCourse() {
        List<String> prerequisites = new ArrayList<>();
        List<DayOfWeek> meetingDays = new ArrayList<>();

        Course course = new Course();
        assertEquals(course, course.setName("name"));
        assertEquals(course, course.setPrerequisites(prerequisites));
        assertEquals(course, course.setMeetingDays(meetingDays));
        assertEquals(course, course.setStartTime("01:02:03"));
        assertEquals(course, course.setEndTime("13:14:15"));
        assertEquals("name", course.getName());
        assertEquals(prerequisites, course.getPrerequisites());
        assertEquals(meetingDays, course.getMeetingDays());
        assertEquals("01:02:03", course.getStartTime());
        assertEquals("13:14:15", course.getEndTime());
    }
}