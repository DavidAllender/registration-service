package com.example.registration.controller;

import com.example.registration.dto.Degree;
import com.example.registration.service.DegreeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/degree")
public class DegreeController {

    @Autowired
    private DegreeService degreeService;

    @SuppressWarnings("squid:S4684")
    @PutMapping("/{name}")
    public Degree createDegree(@PathVariable String name, @RequestBody Degree degreeEntity) {
        return degreeService.saveOrUpdateDegree(name, degreeEntity.setName(name));
    }

    @GetMapping("/{name}")
    public Degree getDegree(@PathVariable String name) {
        return degreeService.getDegree(name);
    }
}
