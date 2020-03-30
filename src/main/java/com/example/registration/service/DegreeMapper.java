package com.example.registration.service;

import com.example.registration.dto.CreateDegreeRequest;
import com.example.registration.dto.Degree;
import com.example.registration.model.CourseEntity;
import com.example.registration.model.DegreeEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class DegreeMapper {
    public DegreeEntity map(CreateDegreeRequest degree) {
        DegreeEntity entity = new DegreeEntity().setRequirements(new ArrayList<>());
        for (String requirement : degree.getRequirements()) {
            entity.getRequirements().add(new CourseEntity().setName(requirement));
        }
        return entity;
    }

    public Degree map(DegreeEntity entity) {
        Degree degree = (Degree) new Degree().setName(entity.getName()).setRequirements(new ArrayList<>());
        for (CourseEntity requirement : entity.getRequirements()) {
            degree.getRequirements().add(requirement.getName());
        }
        return degree;
    }
}
