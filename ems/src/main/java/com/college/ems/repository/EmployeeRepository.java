package com.college.ems.repository;

import com.college.ems.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    // Custom method to look up a user by email (crucial for our login module later)
    Optional<Employee> findByEmail(String email);
    
    // Custom method to search employees by name or email for our admin search bar
    List<Employee> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);
    
    // Custom method to count total employees with a specific role
    long countByRole(String role);
    
    // Custom method to pull the 5 most recently hired employees for our dashboard
    List<Employee> findTop5ByOrderByJoiningDateDesc();
}