package com.example.registration.service;

import com.example.registration.dto.CreateDegreeRequest;
import com.example.registration.dto.Degree;
import com.example.registration.model.CourseEntity;
import com.example.registration.model.DegreeEntity;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DegreeMapperTest {
    @Test
    public void testMapDTOToEntity() {
        DegreeEntity degreeEntity = new DegreeMapper().map(
                new CreateDegreeRequest().setRequirements(Arrays.asList("C1", "C2"))
        );

        assertTrue(degreeEntity.getRequirements().stream().anyMatch(course -> course.getName().equals("C1")));
        assertTrue(degreeEntity.getRequirements().stream().anyMatch(course -> course.getName().equals("C2")));
    }

    @Test
    public void testMapEntityToDTO() {
        Degree degree = new DegreeMapper().map(new DegreeEntity().setName("name").setRequirements(Arrays.asList(
                new CourseEntity().setName("C1"), new CourseEntity().setName("C2")
        )));

        assertEquals("name", degree.getName());
        assertTrue(degree.getRequirements().contains("C1"));
        assertTrue(degree.getRequirements().contains("C2"));
    }
}