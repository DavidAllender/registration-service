package com.example.registration.controller;

import com.example.registration.dto.Degree;
import com.example.registration.model.DegreeEntity;
import com.example.registration.service.DegreeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DegreeControllerTest {
    @Mock
    private DegreeService degreeService;

    @InjectMocks
    private DegreeController degreeController;

    @Test
    public void testCreateDegree() {
        Degree expected = new Degree();
        when(degreeService.saveOrUpdateDegree(eq("name"), any(Degree.class))).thenReturn(expected);
        Degree actual = degreeController.createDegree("name", new Degree());
        assertEquals(expected, actual);
    }

    @Test
    public void testGetDegree() {
        Degree expected = new Degree();
        when(degreeService.getDegree("name")).thenReturn(expected);
        Degree actual = degreeController.getDegree("name");
        assertEquals(expected, actual);
    }
}