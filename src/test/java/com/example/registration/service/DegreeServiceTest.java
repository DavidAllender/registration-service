package com.example.registration.service;

import com.example.registration.dto.Degree;
import com.example.registration.model.DegreeEntity;
import com.example.registration.repository.DegreeRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DegreeServiceTest {

    @Mock
    private DegreeRepository degreeRepository;

    @Mock
    private DegreeMapper degreeMapper;

    @InjectMocks
    private DegreeService degreeService;

    @Test
    public void testSaveOrUpdateDegree() {
        Degree expected = new Degree();
        when(degreeMapper.map(any(Degree.class))).thenReturn(new DegreeEntity());
        when(degreeRepository.save(any(DegreeEntity.class))).thenReturn(new DegreeEntity());
        when(degreeMapper.map(any(DegreeEntity.class))).thenReturn(expected);
        Degree actual = degreeService.saveOrUpdateDegree("name", new Degree());
        assertEquals(expected, actual);
    }

    @Test
    public void testGetDegree() {
        Degree expected = new Degree();
        when(degreeRepository.findById("name")).thenReturn(Optional.of(new DegreeEntity()));
        when(degreeMapper.map(any(DegreeEntity.class))).thenReturn(expected);
        Degree actual = degreeService.getDegree("name");
        assertEquals(expected, actual);
    }

    @Test
    public void testGetDegreeReturnsNullWhenNotFound() {
        when(degreeRepository.findById("name")).thenReturn(Optional.empty());
        assertNull(degreeService.getDegree("name"));
    }
}