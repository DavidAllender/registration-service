package com.example.registration.model;

import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class CourseEntityTest {
    @Test
    public void testCourse() {
        List<CourseEntity> prerequisites = new ArrayList<>();
        DayOfWeek[] meetingDays = new DayOfWeek[0];
        LocalTime start = LocalTime.of(1,2,3);
        LocalTime end = LocalTime.of(13,14, 15);


        CourseEntity courseEntity = new CourseEntity();
        assertEquals(courseEntity, courseEntity.setName("name"));
        assertEquals(courseEntity, courseEntity.setPrerequisites(prerequisites));
        assertEquals(courseEntity, courseEntity.setMeetingDays(meetingDays));
        assertEquals(courseEntity, courseEntity.setStartTime("01:02:03"));
        assertEquals(courseEntity, courseEntity.setEndTime("13:14:15"));

        assertEquals("name", courseEntity.getName());
        assertEquals(prerequisites, courseEntity.getPrerequisites());
        assertArrayEquals(meetingDays, courseEntity.getMeetingDays());
        assertEquals("01:02:03", courseEntity.getStartTime());
        assertEquals("13:14:15", courseEntity.getEndTime());
        assertEquals(start, courseEntity.getLocalStartTime());
        assertEquals(end, courseEntity.getLocalEndTime());
    }
}