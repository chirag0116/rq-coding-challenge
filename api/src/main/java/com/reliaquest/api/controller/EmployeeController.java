package com.reliaquest.api.controller;

import com.reliaquest.api.dto.EmployeeInputDTO;
import com.reliaquest.api.dto.EmployeeResponseDTO;
import com.reliaquest.api.exception.ResourceNotFoundException;
import com.reliaquest.api.service.MockEmployeeApiService;
import jakarta.validation.*;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/employees")
@Slf4j
public class EmployeeController implements IEmployeeController<EmployeeResponseDTO, EmployeeInputDTO> {

    private final MockEmployeeApiService employeeService;

    @Autowired
    public EmployeeController(MockEmployeeApiService employeeService) {
        this.employeeService = employeeService;
    }

    @Override
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployees() {
        log.info("Request: Get all employees");
        List<EmployeeResponseDTO> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    @Override
    public ResponseEntity<List<EmployeeResponseDTO>> getEmployeesByNameSearch(@PathVariable String searchString) {
        log.info("Request: Search employees by name fragment '{}'", searchString);
        List<EmployeeResponseDTO> all = employeeService.getAllEmployees();
        List<EmployeeResponseDTO> filtered = all.stream()
                .filter(e -> e.getEmployee_name() != null
                        && e.getEmployee_name().toLowerCase().contains(searchString.toLowerCase()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(filtered);
    }

    @Override
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(@PathVariable String id) {
        log.info("Request: Get employee by ID '{}'", id);
        EmployeeResponseDTO employee = employeeService.getEmployeeById(id);
        if (employee == null) {
            log.warn("Employee not found: {}", id);
            throw new ResourceNotFoundException("Employee with ID '" + id + "' not found");
        }
        return ResponseEntity.ok(employee);
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        log.info("Request: Get highest salary of employees");
        List<EmployeeResponseDTO> employees = employeeService.getAllEmployees();
        int max = employees.stream()
                .mapToInt(EmployeeResponseDTO::getEmployee_salary)
                .max()
                .orElse(0);
        return ResponseEntity.ok(max);
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        log.info("Request: Get top 10 highest earning employee names");
        List<EmployeeResponseDTO> employees = employeeService.getAllEmployees();
        List<String> top10 = employees.stream()
                .sorted(Comparator.comparingInt(EmployeeResponseDTO::getEmployee_salary)
                        .reversed())
                .limit(10)
                .map(EmployeeResponseDTO::getEmployee_name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(top10);
    }

    @Override
    public ResponseEntity<EmployeeResponseDTO> createEmployee(@Valid @RequestBody EmployeeInputDTO employeeInput) {
        log.info("Request: Create new employee '{}'", employeeInput.getName());
        EmployeeResponseDTO created = employeeService.createEmployee(employeeInput);
        return ResponseEntity.ok(created);
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(@PathVariable String id) {
        log.info("Request: Delete employee by ID '{}'", id);
        EmployeeResponseDTO employee = employeeService.getEmployeeById(id);
        if (employee == null) {
            log.warn("Delete failed — employee ID '{}' not found", id);
            throw new ResourceNotFoundException("Employee with ID '" + id + "' not found");
        }

        boolean deleted = employeeService.deleteEmployeeByName(employee.getEmployee_name());
        if (!deleted) {
            log.error("Failed to delete employee '{}'", employee.getEmployee_name());
            throw new RuntimeException("Failed to delete employee with ID " + id);
        }

        return ResponseEntity.ok("Deleted employee: " + employee.getEmployee_name());
    }
}
