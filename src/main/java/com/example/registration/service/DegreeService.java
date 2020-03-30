package com.example.registration.service;

import com.example.registration.dto.CreateDegreeRequest;
import com.example.registration.dto.Degree;
import com.example.registration.repository.DegreeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DegreeService {

    @Autowired
    private DegreeRepository repository;

    @Autowired
    private DegreeMapper mapper;

    public Degree saveOrUpdateDegree(String name, CreateDegreeRequest request) {
        return mapper.map(repository.save(mapper.map(request).setName(name)));
    }

    public Degree getDegree(String name) {
        return repository.findById(name).map(mapper::map).orElse(null);
    }
}
