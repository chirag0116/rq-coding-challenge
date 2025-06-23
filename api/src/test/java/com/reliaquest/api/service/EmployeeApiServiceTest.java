package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.dto.*;
import com.reliaquest.api.dto.EmployeeListResponseWrapper;
import com.reliaquest.api.dto.EmployeeResponseWrapper;
import com.reliaquest.api.exception.ServiceUnavailableException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.web.client.*;

class EmployeeApiServiceTest {

    @Mock
    RestTemplate restTemplate;

    MockEmployeeApiService service;

    final String baseUrl = "http://mock-api";

    @BeforeEach
    void setup() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);
        service = new MockEmployeeApiService(baseUrl);
        var field = MockEmployeeApiService.class.getDeclaredField("restTemplate");
        field.setAccessible(true);
        field.set(service, restTemplate);
    }

    @Test
    void testGetAllEmployees_success() {
        List<EmployeeResponseDTO> employees = List.of(new EmployeeResponseDTO(), new EmployeeResponseDTO());
        EmployeeListResponseWrapper wrapper = new EmployeeListResponseWrapper();
        wrapper.setData(employees);

        when(restTemplate.getForEntity(eq(baseUrl + "/employee"), eq(EmployeeListResponseWrapper.class)))
                .thenReturn(new ResponseEntity<>(wrapper, HttpStatus.OK));

        List<EmployeeResponseDTO> result = service.getAllEmployees();

        assertEquals(employees, result);
        verify(restTemplate).getForEntity(baseUrl + "/employee", EmployeeListResponseWrapper.class);
    }

    @Test
    void testGetEmployeeById_found() {
        EmployeeResponseDTO employee = new EmployeeResponseDTO();
        employee.setId("123");
        EmployeeResponseWrapper wrapper = new EmployeeResponseWrapper();
        wrapper.setData(employee);

        when(restTemplate.getForEntity(baseUrl + "/employee/123", EmployeeResponseWrapper.class))
                .thenReturn(new ResponseEntity<>(wrapper, HttpStatus.OK));

        EmployeeResponseDTO result = service.getEmployeeById("123");

        assertNotNull(result);
        assertEquals("123", result.getId());
    }

    @Test
    void testGetEmployeeById_notFoundReturnsNull() {
        String employeeId = "999";
        HttpHeaders headers = mock(HttpHeaders.class);

        HttpClientErrorException notFoundException = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", headers, new byte[0], StandardCharsets.UTF_8);

        when(restTemplate.getForEntity(eq(baseUrl + "/employee/" + employeeId), eq(EmployeeResponseWrapper.class)))
                .thenThrow(notFoundException);

        EmployeeResponseDTO employee = service.getEmployeeById(employeeId);

        assertNull(employee);
        verify(restTemplate, times(1))
                .getForEntity(eq(baseUrl + "/employee/" + employeeId), eq(EmployeeResponseWrapper.class));
    }

    @Test
    void testCreateEmployee_success() {
        EmployeeInputDTO input = new EmployeeInputDTO();
        input.setName("Chirag");

        EmployeeResponseDTO created = new EmployeeResponseDTO();
        created.setEmployee_name("Chirag");
        EmployeeResponseWrapper wrapper = new EmployeeResponseWrapper();
        wrapper.setData(created);

        when(restTemplate.postForEntity(
                        eq(baseUrl + "/employee"), any(HttpEntity.class), eq(EmployeeResponseWrapper.class)))
                .thenReturn(new ResponseEntity<>(wrapper, HttpStatus.CREATED));

        EmployeeResponseDTO result = service.createEmployee(input);

        assertNotNull(result);
        assertEquals("Chirag", result.getEmployee_name());
    }

    @Test
    void testDeleteEmployeeByName_success() {
        when(restTemplate.exchange(
                        eq(baseUrl + "/employee"), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        boolean result = service.deleteEmployeeByName("Chirag");

        assertTrue(result);
        verify(restTemplate, times(1))
                .exchange(eq(baseUrl + "/employee"), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class));
    }

    @Test
    void testDeleteEmployeeByName_notFoundReturnsFalse() {
        HttpHeaders headers = mock(HttpHeaders.class);

        HttpClientErrorException notFoundException = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", headers, new byte[0], StandardCharsets.UTF_8);

        when(restTemplate.exchange(
                        eq(baseUrl + "/employee"), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class)))
                .thenThrow(notFoundException);

        boolean result = service.deleteEmployeeByName("NonExistent");

        assertFalse(result);
        verify(restTemplate, times(1))
                .exchange(eq(baseUrl + "/employee"), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class));
    }

    @Test
    void testRecoverFromGetAllEmployees_throws() {
        Throwable cause = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);

        ServiceUnavailableException ex =
                assertThrows(ServiceUnavailableException.class, () -> service.recoverFromGetAllEmployees(cause));

        assertTrue(ex.getMessage().contains("Service is unavailable"));
        assertEquals(cause, ex.getCause());
    }

    @Test
    void testRecoverFromCreateEmployee_throws() {
        Throwable cause = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        EmployeeInputDTO input = new EmployeeInputDTO();
        input.setName("Chirag");
        input.setAge(24);
        input.setSalary(1000);
        input.setTitle("Test");

        ServiceUnavailableException ex =
                assertThrows(ServiceUnavailableException.class, () -> service.recoverFromCreateEmployee(cause, input));

        assertTrue(ex.getMessage().contains("Failed to create employee"));
        assertEquals(cause, ex.getCause());
    }

    @Test
    void testRecoverFromGetEmployeeById_throws() {
        Throwable cause = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);

        ServiceUnavailableException ex =
                assertThrows(ServiceUnavailableException.class, () -> service.recoverFromGetEmployeeById(cause, "42"));

        assertTrue(ex.getMessage().contains("Service is unavailable"));
        assertEquals(cause, ex.getCause());
    }

    @Test
    void testRecoverFromDeleteEmployeeByName_throws() {
        Throwable cause = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);

        ServiceUnavailableException ex = assertThrows(
                ServiceUnavailableException.class, () -> service.recoverFromDeleteEmployeeByName(cause, "Chirag"));

        assertTrue(ex.getMessage().contains("Service is unavailable"));
        assertEquals(cause, ex.getCause());
    }
}
