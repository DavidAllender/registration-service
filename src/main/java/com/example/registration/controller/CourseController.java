package com.example.registration.controller;

import com.example.registration.dto.Course;
import com.example.registration.dto.CreateCourseRequest;
import com.example.registration.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @PutMapping("/{name}")
    public Course createCourse(@PathVariable String name, @RequestBody CreateCourseRequest course) {
        return courseService.saveOrUpdate(name, course);
    }

    @GetMapping("/{name}")
    public Course getCourse(@PathVariable String name) {
        return courseService.getCourse(name);
    }
}
