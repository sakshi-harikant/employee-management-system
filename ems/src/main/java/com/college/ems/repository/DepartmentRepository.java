package com.college.ems.repository;

import com.college.ems.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    // This gives us default CRUD methods like save(), findAll(), findById(), and deleteById()
}