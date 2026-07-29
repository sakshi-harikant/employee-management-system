package com.college.ems.controller;

import com.college.ems.model.Department;
import com.college.ems.model.Employee;
import com.college.ems.service.DepartmentService;
import com.college.ems.service.EmployeeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
public class MainController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private DepartmentService departmentService;

    // ================= 1. LOGIN MODULE =================
 // ================= 1. HOME & LOGIN MODULE =================
    @GetMapping("/")
    public String showHomePage(HttpSession session) {
        if (session.getAttribute("user") != null) {
            String role = (String) session.getAttribute("role");
            return "ADMIN".equals(role) ? "redirect:/admin/dashboard" : "redirect:/employee/dashboard";
        }
        return "index"; // Serves our brand-new premium landing page
    }

    @GetMapping("/login")
    public String showLoginPage(HttpSession session) {
        if (session.getAttribute("user") != null) {
            String role = (String) session.getAttribute("role");
            return "ADMIN".equals(role) ? "redirect:/admin/dashboard" : "redirect:/employee/dashboard";
        }
        return "login"; // Serves the login form card
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password, HttpSession session, Model model) {
        // Hardcoded Master Admin for easy college project evaluation
        if ("admin@ems.com".equals(email) && "admin123".equals(password)) {
            session.setAttribute("user", "Admin");
            session.setAttribute("role", "ADMIN");
            return "redirect:/admin/dashboard";
        }

        // Database check for regular employees or custom admins
        Employee employee = employeeService.authenticate(email, password);
        if (employee != null) {
            session.setAttribute("user", employee);
            session.setAttribute("role", employee.getRole());
            return "ADMIN".equals(employee.getRole()) ? "redirect:/admin/dashboard" : "redirect:/employee/dashboard";
        }

        model.addAttribute("error", "Invalid Email or Password!");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/?logout=true";
    }

    // ================= 2. DASHBOARDS =================
    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        if (!"ADMIN".equals(session.getAttribute("role"))) return "redirect:/";
        
        model.addAttribute("totalEmployees", employeeService.getTotalEmployeesCount());
        model.addAttribute("totalDepartments", departmentService.getCount());
        model.addAttribute("recentEmployees", employeeService.getRecentEmployees());
        return "admin_dashboard";
    }

    @GetMapping("/employee/dashboard")
    public String employeeDashboard(HttpSession session, Model model) {
        Employee currentEmployee = (Employee) session.getAttribute("user");
        if (currentEmployee == null || !"EMPLOYEE".equals(session.getAttribute("role"))) return "redirect:/";

        // Refresh from DB to ensure updated details show up
        Employee updatedEmployee = employeeService.getEmployeeById(currentEmployee.getId());
        model.addAttribute("employee", updatedEmployee);
        return "employee_dashboard";
    }

    // ================= 3. EMPLOYEE CRUD =================
    @GetMapping("/admin/employees")
    public String viewEmployees(@RequestParam(required = false) String search, HttpSession session, Model model) {
        if (!"ADMIN".equals(session.getAttribute("role"))) return "redirect:/";
        
        List<Employee> employees;
        if (search != null && !search.trim().isEmpty()) {
            employees = employeeService.searchEmployees(search);
            model.addAttribute("searchKeyword", search);
        } else {
            employees = employeeService.getAllEmployees();
        }
        model.addAttribute("employees", employees);
        return "manage_employees";
    }

    @GetMapping("/admin/employees/add")
    public String showAddEmployeeForm(HttpSession session, Model model) {
        if (!"ADMIN".equals(session.getAttribute("role"))) return "redirect:/";
        model.addAttribute("employee", new Employee());
        model.addAttribute("departments", departmentService.getAllDepartments());
        return "employee_form";
    }

    @PostMapping("/admin/employees/save")
    public String saveEmployee(@ModelAttribute Employee employee, HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("role"))) return "redirect:/";
        if (employee.getJoiningDate() == null) {
            employee.setJoiningDate(LocalDate.now());
        }
        employeeService.saveEmployee(employee);
        return "redirect:/admin/employees";
    }

    @GetMapping("/admin/employees/edit/{id}")
    public String showEditEmployeeForm(@PathVariable Long id, HttpSession session, Model model) {
        if (!"ADMIN".equals(session.getAttribute("role"))) return "redirect:/";
        model.addAttribute("employee", employeeService.getEmployeeById(id));
        model.addAttribute("departments", departmentService.getAllDepartments());
        return "employee_form";
    }

    @GetMapping("/admin/employees/delete/{id}")
    public String deleteEmployee(@PathVariable Long id, HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("role"))) return "redirect:/";
        employeeService.deleteEmployee(id);
        return "redirect:/admin/employees";
    }

    // ================= 4. DEPARTMENT CRUD =================
    @GetMapping("/admin/departments")
    public String viewDepartments(HttpSession session, Model model) {
        if (!"ADMIN".equals(session.getAttribute("role"))) return "redirect:/";
        model.addAttribute("departments", departmentService.getAllDepartments());
        return "manage_departments";
    }

    @GetMapping("/admin/departments/add")
    public String showAddDepartmentForm(HttpSession session, Model model) {
        if (!"ADMIN".equals(session.getAttribute("role"))) return "redirect:/";
        model.addAttribute("department", new Department());
        return "department_form";
    }

    @PostMapping("/admin/departments/save")
    public String saveDepartment(@ModelAttribute Department department, HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("role"))) return "redirect:/";
        departmentService.saveDepartment(department);
        return "redirect:/admin/departments";
    }

    @GetMapping("/admin/departments/edit/{id}")
    public String showEditDepartmentForm(@PathVariable Long id, HttpSession session, Model model) {
        if (!"ADMIN".equals(session.getAttribute("role"))) return "redirect:/";
        model.addAttribute("department", departmentService.getDepartmentById(id));
        return "department_form";
    }

    @GetMapping("/admin/departments/delete/{id}")
    public String deleteDepartment(@PathVariable Long id, HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("role"))) return "redirect:/";
        departmentService.deleteDepartment(id);
        return "redirect:/admin/departments";
    }

    // ================= 5. EMPLOYEE PROFILE SELF-UPDATE =================
    @PostMapping("/employee/profile/update")
    public String employeeSelfUpdate(@RequestParam String phone, @RequestParam String email, HttpSession session) {
        Employee currentEmployee = (Employee) session.getAttribute("user");
        if (currentEmployee == null) return "redirect:/";

        Employee employee = employeeService.getEmployeeById(currentEmployee.getId());
        employee.setPhone(phone);
        employee.setEmail(email);
        employeeService.saveEmployee(employee);

        return "redirect:/employee/dashboard?updated=true";
    }
}