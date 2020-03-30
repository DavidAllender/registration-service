package com.example.registration.controller;

import com.example.registration.dto.Course;
import com.example.registration.service.CourseService;
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
public class CourseControllerTest {
    @Mock
    private CourseService courseService;

    @InjectMocks
    private CourseController courseController;

    @Test
    public void testCreateCourse() {
        Course expected = new Course();
        when(courseService.saveOrUpdate(eq("name"), any(Course.class))).thenReturn(expected);
        Course actual = courseController.createCourse("name", new Course());
        assertEquals(expected, actual);
    }

    @Test
    public void testGetCourse() {
        Course expected = new Course();
        when(courseService.getCourse("name")).thenReturn(expected);
        Course actual = courseService.getCourse("name");
        assertEquals(expected, actual);
    }
}