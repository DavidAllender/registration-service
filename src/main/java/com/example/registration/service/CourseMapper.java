package com.example.registration.service;

import com.example.registration.dto.Course;
import com.example.registration.dto.CreateCourseRequest;
import com.example.registration.model.CourseEntity;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;

@Component
public class CourseMapper {
    public CourseEntity map(CreateCourseRequest course) {
        CourseEntity courseEntity = new CourseEntity()
                .setMeetingDays(course.getMeetingDays().toArray(new DayOfWeek[0]))
                .setStartTime(course.getStartTime())
                .setEndTime(course.getEndTime())
                .setPrerequisites(new ArrayList<>());

        for (String prerequisite : course.getPrerequisites()) {
            courseEntity.getPrerequisites().add(new CourseEntity().setName(prerequisite));
        }

        return courseEntity;
    }

    public Course map(CourseEntity entity) {
        Course course = (Course) new Course().setName(entity.getName())
                .setMeetingDays(Arrays.asList(entity.getMeetingDays()))
                .setStartTime(entity.getStartTime())
                .setEndTime(entity.getEndTime());

        for (CourseEntity prerequisite : entity.getPrerequisites()) {
            course.getPrerequisites().add(prerequisite.getName());
        }

        return course;
    }
}
