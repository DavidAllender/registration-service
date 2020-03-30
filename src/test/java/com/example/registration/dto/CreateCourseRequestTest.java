package com.example.registration.dto;

import org.junit.Test;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class CreateCourseRequestTest {
    @Test
    public void testCreateCourseRequest() {
        List<String> prerequisites = new ArrayList<>();
        List<DayOfWeek> meetingDays = new ArrayList<>();

        CreateCourseRequest request = new CreateCourseRequest();
        assertEquals(request, request.setPrerequisites(prerequisites));
        assertEquals(request, request.setMeetingDays(meetingDays));
        assertEquals(request, request.setStartTime("01:02:03"));
        assertEquals(request, request.setEndTime("13:14:15"));
        assertEquals(prerequisites, request.getPrerequisites());
        assertEquals(meetingDays, request.getMeetingDays());
        assertEquals("01:02:03", request.getStartTime());
        assertEquals("13:14:15", request.getEndTime());
    }
}