package com.example.registration.repository;

import com.example.registration.model.DegreeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DegreeRepository extends JpaRepository<DegreeEntity, String> {

}
