package com.example.registration.dto;

import java.util.List;

public class Degree extends CreateDegreeRequest {
    private String name;

    public String getName() {
        return name;
    }

    public Degree setName(String name) {
        this.name = name;
        return this;
    }
}
