package com.example.registration.model;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class DegreeEntityTest {
    @Test
    public void testDegree() {
        List<CourseEntity> requirements = new ArrayList<>();
        DegreeEntity degreeEntity = new DegreeEntity();

        assertEquals(degreeEntity, degreeEntity.setName("name"));
        assertEquals(degreeEntity, degreeEntity.setRequirements(requirements));

        assertEquals("name", degreeEntity.getName());
        assertEquals(requirements, degreeEntity.getRequirements());
    }
}