package com.example.registration.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vladmihalcea.hibernate.type.array.StringArrayType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Entity(name = "course")
@TypeDef(name = "string-array", typeClass = StringArrayType.class)
public class CourseEntity {
    @Id
    private String name;

    @ManyToMany
    @JoinTable(
            name = "course_prerequisite",
            joinColumns = @JoinColumn(name = "is_prerequisite_of"),
            inverseJoinColumns = @JoinColumn(name = "name")
    )
    private List<CourseEntity> prerequisites;

    @Type(type = "string-array")
    @Column(columnDefinition = "text[]", name = "meeting_days")
    private DayOfWeek[] meetingDays;

    //hh:mm:ss
    @Column(name = "start_time")
    private String startTime;

    //hh:mm:ss
    @Column(name = "end_time")
    private String endTime;

    public String getName() {
        return name;
    }

    public CourseEntity setName(String name) {
        this.name = name;
        return this;
    }

    public List<CourseEntity> getPrerequisites() {
        return prerequisites;
    }

    public CourseEntity setPrerequisites(List<CourseEntity> prerequisites) {
        this.prerequisites = prerequisites;
        return this;
    }

    public DayOfWeek[] getMeetingDays() {
        return meetingDays;
    }

    public CourseEntity setMeetingDays(DayOfWeek[] meetingDays) {
        this.meetingDays = meetingDays;
        return this;
    }

    public String getStartTime() {
        return startTime;
    }

    public CourseEntity setStartTime(String startTime) {
        this.startTime = startTime;
        return this;
    }

    public String getEndTime() {
        return endTime;
    }

    public CourseEntity setEndTime(String endTime) {
        this.endTime = endTime;
        return this;
    }

    @JsonIgnore
    public LocalTime getLocalStartTime() {
        return LocalTime.parse(startTime);
    }

    @JsonIgnore
    public LocalTime getLocalEndTime() {
        return LocalTime.parse(endTime);
    }
}
