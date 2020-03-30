package com.example.registration.dto;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class CreateDegreeRequestTest {
    @Test
    public void testCreateDegreeRequest() {
        List<String> requirements = new ArrayList<>();
        CreateDegreeRequest request = new CreateDegreeRequest();
        assertEquals(request, request.setRequirements(requirements));
        assertEquals(requirements, request.getRequirements());
    }
}