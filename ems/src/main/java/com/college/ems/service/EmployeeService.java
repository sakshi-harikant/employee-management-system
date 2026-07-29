package com.college.ems.service;

import com.college.ems.model.Employee;
import com.college.ems.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    // Simple plain-text login validation for your college project
 // Simple plain-text login validation for your college project
    public Employee authenticate(String email, String password) {
        Optional<Employee> employeeOpt = employeeRepository.findByEmail(email);
        
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            
            // --- DEBUG PRINT START ---
            System.out.println("================ LOGIN DEBUG ================");
            System.out.println("Typed Email:    [" + email + "]");
            System.out.println("DB Email:       [" + employee.getEmail() + "]");
            System.out.println("Typed Password: [" + password + "]");
            System.out.println("DB Password:    [" + employee.getPassword() + "]");
            System.out.println("=============================================");
            // --- DEBUG PRINT END ---

            if (employee.getPassword().equals(password)) {
                return employee;
            } else {
                System.out.println(">>> PASSWORD MISMATCH ERROR! <<<");
            }
        } else {
            System.out.println(">>> EMAIL NOT FOUND IN DATABASE: [" + email + "] <<<");
        }
        return null; // Login failed
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public void saveEmployee(Employee employee) {
        employeeRepository.save(employee);
    }

    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id).orElse(null);
    }

    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }

    public List<Employee> searchEmployees(String keyword) {
        return employeeRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword);
    }

    public long getTotalEmployeesCount() {
        // Counts only the actual employees, excluding admin accounts
        return employeeRepository.countByRole("EMPLOYEE");
    }

    public List<Employee> getRecentEmployees() {
        return employeeRepository.findTop5ByOrderByJoiningDateDesc();
    }
}