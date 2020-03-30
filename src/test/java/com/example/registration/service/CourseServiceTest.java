package com.example.registration.service;

import com.example.registration.dto.Course;
import com.example.registration.model.CourseEntity;
import com.example.registration.repository.CourseRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseMapper courseMapper;

    @InjectMocks
    private CourseService courseService;

    @Test
    public void testCreateOrUpdateCourse() {
        Course expected = new Course();
        when(courseMapper.map(any(Course.class))).thenReturn(new CourseEntity());
        when(courseRepository.save(any(CourseEntity.class))).thenReturn(new CourseEntity());
        when(courseMapper.map(any(CourseEntity.class))).thenReturn(expected);
        Course actual = courseService.saveOrUpdate("name", new Course());
        assertEquals(expected, actual);
    }

    @Test
    public void testGetCourse() {
        Course expected = new Course();
        when(courseRepository.findById("name")).thenReturn(Optional.of(new CourseEntity()));
        when(courseMapper.map(any(CourseEntity.class))).thenReturn(expected);
        Course actual = courseService.getCourse("name");
        assertEquals(expected, actual);
    }

    @Test
    public void testGetCourseReturnsNullWhenNotFound() {
        when(courseRepository.findById("name")).thenReturn(Optional.empty());
        assertNull(courseService.getCourse("name"));
    }
}