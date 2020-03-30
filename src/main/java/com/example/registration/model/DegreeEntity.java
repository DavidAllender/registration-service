package com.example.registration.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import java.util.List;

@Entity(name = "degree")
public class DegreeEntity {

    @Id
    private String name;

    @ManyToMany
    @JoinTable(
            name = "degree_requirement",
            joinColumns = @JoinColumn(name = "degree"),
            inverseJoinColumns = @JoinColumn(name = "course")
    )
    private List<CourseEntity> requirements;

    public String getName() {
        return name;
    }

    public DegreeEntity setName(String name) {
        this.name = name;
        return this;
    }

    public List<CourseEntity> getRequirements() {
        return requirements;
    }

    public DegreeEntity setRequirements(List<CourseEntity> requirements) {
        this.requirements = requirements;
        return this;
    }
}
