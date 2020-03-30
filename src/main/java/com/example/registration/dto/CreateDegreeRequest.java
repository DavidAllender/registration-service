package com.example.registration.dto;

import java.util.List;

public class CreateDegreeRequest {
    private List<String> requirements;

    public List<String> getRequirements() {
        return requirements;
    }

    public CreateDegreeRequest setRequirements(List<String> requirements) {
        this.requirements = requirements;
        return this;
    }
}
