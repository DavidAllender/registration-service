package com.example.registration.dto;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

public class CreateCourseRequest {
    private List<String> prerequisites = new ArrayList<>();
    private List<DayOfWeek> meetingDays = new ArrayList<>();
    private String startTime;
    private String endTime;

    public List<String> getPrerequisites() {
        return prerequisites;
    }

    public CreateCourseRequest setPrerequisites(List<String> prerequisites) {
        this.prerequisites = prerequisites;
        return this;
    }

    public List<DayOfWeek> getMeetingDays() {
        return meetingDays;
    }

    public CreateCourseRequest setMeetingDays(List<DayOfWeek> meetingDays) {
        this.meetingDays = meetingDays;
        return this;
    }

    public String getStartTime() {
        return startTime;
    }

    public CreateCourseRequest setStartTime(String startTime) {
        this.startTime = startTime;
        return this;
    }

    public String getEndTime() {
        return endTime;
    }

    public CreateCourseRequest setEndTime(String endTime) {
        this.endTime = endTime;
        return this;
    }
}
