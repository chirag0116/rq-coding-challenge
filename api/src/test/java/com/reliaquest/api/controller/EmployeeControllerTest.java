package com.reliaquest.api.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.dto.EmployeeInputDTO;
import com.reliaquest.api.dto.EmployeeResponseDTO;
import com.reliaquest.api.exception.ResourceNotFoundException;
import com.reliaquest.api.service.MockEmployeeApiService;
import java.util.List;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

class EmployeeControllerUnitTest {

    @Mock
    MockEmployeeApiService employeeService;

    @InjectMocks
    EmployeeController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllEmployees() {
        List<EmployeeResponseDTO> employees = List.of(new EmployeeResponseDTO());
        when(employeeService.getAllEmployees()).thenReturn(employees);

        ResponseEntity<List<EmployeeResponseDTO>> response = controller.getAllEmployees();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(employees, response.getBody());
    }

    @Test
    void testGetEmployeesByNameSearch() {
        EmployeeResponseDTO employee1 = new EmployeeResponseDTO();
        employee1.setEmployee_name("Chirag");
        EmployeeResponseDTO employee2 = new EmployeeResponseDTO();
        employee2.setEmployee_name("John");

        when(employeeService.getAllEmployees()).thenReturn(List.of(employee1, employee2));

        ResponseEntity<List<EmployeeResponseDTO>> response = controller.getEmployeesByNameSearch("chi");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(List.of(employee1), response.getBody());
    }

    @Test
    void testGetEmployeeByIdFound() {
        EmployeeResponseDTO employee = new EmployeeResponseDTO();
        employee.setEmployee_name("Chirag");
        when(employeeService.getEmployeeById("123")).thenReturn(employee);

        ResponseEntity<EmployeeResponseDTO> response = controller.getEmployeeById("123");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(employee, response.getBody());
    }

    @Test
    void testGetEmployeeByIdNotFound() {
        when(employeeService.getEmployeeById("999")).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> controller.getEmployeeById("999"));
    }

    @Test
    void testGetHighestSalaryOfEmployees() {
        EmployeeResponseDTO employee1 = new EmployeeResponseDTO();
        employee1.setEmployee_salary(100);
        EmployeeResponseDTO employee2 = new EmployeeResponseDTO();
        employee2.setEmployee_salary(500);
        when(employeeService.getAllEmployees()).thenReturn(List.of(employee1, employee2));

        ResponseEntity<Integer> response = controller.getHighestSalaryOfEmployees();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(500, response.getBody());
    }

    @Test
    void testGetTopTenHighestEarningEmployeeNames() {
        EmployeeResponseDTO employee1 = new EmployeeResponseDTO();
        employee1.setEmployee_name("Chirag");
        employee1.setEmployee_salary(2000);
        EmployeeResponseDTO employee2 = new EmployeeResponseDTO();
        employee2.setEmployee_name("John");
        employee2.setEmployee_salary(1000);
        when(employeeService.getAllEmployees()).thenReturn(List.of(employee1, employee2));

        ResponseEntity<List<String>> response = controller.getTopTenHighestEarningEmployeeNames();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(List.of("Chirag", "John"), response.getBody());
    }

    @Test
    void testCreateEmployee() {
        EmployeeInputDTO input = new EmployeeInputDTO();
        input.setName("Chirag");
        input.setSalary(123);
        input.setAge(24);
        input.setTitle("Test");

        EmployeeResponseDTO responseDto = new EmployeeResponseDTO();
        responseDto.setEmployee_name("Chirag");
        when(employeeService.createEmployee(input)).thenReturn(responseDto);

        ResponseEntity<EmployeeResponseDTO> response = controller.createEmployee(input);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(responseDto, response.getBody());
    }

    @Test
    void testDeleteEmployeeByIdFoundAndDeleted() {
        EmployeeResponseDTO responseDto = new EmployeeResponseDTO();
        responseDto.setEmployee_name("Chirag");
        when(employeeService.getEmployeeById("123")).thenReturn(responseDto);
        when(employeeService.deleteEmployeeByName("Chirag")).thenReturn(true);
        ResponseEntity<String> response = controller.deleteEmployeeById("123");
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Deleted employee: Chirag", response.getBody());
    }

    @Test
    void testDeleteEmployeeByIdNotFound() {
        when(employeeService.getEmployeeById("123")).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> controller.deleteEmployeeById("123"));
    }

    @Test
    void testDeleteEmployeeByIdDeleteFails() {
        EmployeeResponseDTO responseDto = new EmployeeResponseDTO();
        responseDto.setEmployee_name("John");

        when(employeeService.getEmployeeById("123")).thenReturn(responseDto);
        when(employeeService.deleteEmployeeByName("John")).thenReturn(false);
        assertThrows(RuntimeException.class, () -> controller.deleteEmployeeById("123"));
    }
}
