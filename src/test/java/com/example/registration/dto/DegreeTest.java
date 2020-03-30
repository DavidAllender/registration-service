package com.example.registration.dto;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class DegreeTest {
    @Test
    public void testDegree() {
        List<String> requirements = new ArrayList<>();
        Degree degree = new Degree();
        assertEquals(degree, degree.setName("name"));
        assertEquals(degree, degree.setRequirements(requirements));
        assertEquals("name", degree.getName());
        assertEquals(requirements, degree.getRequirements());
    }
}