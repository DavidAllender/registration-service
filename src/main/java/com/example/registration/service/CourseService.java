package com.example.registration.service;

import com.example.registration.dto.Course;
import com.example.registration.dto.CreateCourseRequest;
import com.example.registration.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CourseService {

    @Autowired
    private CourseRepository repository;

    @Autowired
    private CourseMapper mapper;

    public Course saveOrUpdate(String name, CreateCourseRequest course) {
        return mapper.map(repository.save(mapper.map(course).setName(name)));
    }

    public Course getCourse(String name) {
        return repository.findById(name).map(mapper::map).orElse(null);
    }
}
