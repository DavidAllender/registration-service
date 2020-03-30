package com.example.registration.dto;

public class Course extends CreateCourseRequest {
    private String name;

    public String getName() {
        return name;
    }

    public Course setName(String name) {
        this.name = name;
        return this;
    }
}
