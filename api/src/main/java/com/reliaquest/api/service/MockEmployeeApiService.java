package com.reliaquest.api.service;

import com.reliaquest.api.dto.*;
import com.reliaquest.api.dto.EmployeeListResponseWrapper;
import com.reliaquest.api.dto.EmployeeResponseWrapper;
import com.reliaquest.api.exception.ServiceUnavailableException;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;

@Service
@Slf4j
@EnableRetry
public class MockEmployeeApiService {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public MockEmployeeApiService(@Value("${mock.api.base-url}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    @Retryable(
            value = {HttpServerErrorException.class, HttpClientErrorException.TooManyRequests.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    public List<EmployeeResponseDTO> getAllEmployees() {
        log.info("Fetching all employees from mock API...");
        ResponseEntity<EmployeeListResponseWrapper> response =
                restTemplate.getForEntity(baseUrl + "/employee", EmployeeListResponseWrapper.class);
        return response.getBody().getData();
    }

    @Retryable(
            value = {HttpServerErrorException.class, HttpClientErrorException.TooManyRequests.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    public EmployeeResponseDTO getEmployeeById(String id) {
        log.info("Fetching employee by ID: {}", id);
        try {
            ResponseEntity<EmployeeResponseWrapper> response =
                    restTemplate.getForEntity(baseUrl + "/employee/" + id, EmployeeResponseWrapper.class);
            return response.getBody().getData();
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Employee ID {} not found in mock API", id);
            return null;
        }
    }

    @Retryable(
            value = {HttpServerErrorException.class, HttpClientErrorException.TooManyRequests.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    public EmployeeResponseDTO createEmployee(EmployeeInputDTO employee) {
        log.info("Creating employee: {}", employee.getName());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmployeeInputDTO> request = new HttpEntity<>(employee, headers);

        ResponseEntity<EmployeeResponseWrapper> response =
                restTemplate.postForEntity(baseUrl + "/employee", request, EmployeeResponseWrapper.class);
        return response.getBody().getData();
    }

    @Retryable(
            value = {HttpServerErrorException.class, HttpClientErrorException.TooManyRequests.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    public boolean deleteEmployeeByName(String name) {
        log.info("Deleting employee by name: {}", name);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("name", name);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.exchange(baseUrl + "/employee", HttpMethod.DELETE, request, Void.class);
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Employee name {} not found for deletion", name);
            return false;
        }
    }

    @Recover
    public List<EmployeeResponseDTO> recoverFromGetAllEmployees(Throwable ex) {
        log.error("All retries failed for getAllEmployees: {}", ex.getMessage());
        throw new ServiceUnavailableException("Service is unavailable after multiple attempts", ex);
    }

    @Recover
    public EmployeeResponseDTO recoverFromCreateEmployee(Throwable ex, EmployeeInputDTO employee) {
        log.error("All retries failed while creating employee '{}': {}", employee.getName(), ex.getMessage());
        throw new ServiceUnavailableException("Failed to create employee after multiple attempts", ex);
    }

    @Recover
    public EmployeeResponseDTO recoverFromGetEmployeeById(Throwable ex, String id) {
        log.error("All retries failed for getEmployeeById({}): {}", id, ex.getMessage());
        throw new ServiceUnavailableException("Service is unavailable after multiple attempts", ex);
    }

    @Recover
    public boolean recoverFromDeleteEmployeeByName(Throwable ex, String name) {
        log.error("All retries failed for deleteEmployeeByName('{}'): {}", name, ex.getMessage());
        throw new ServiceUnavailableException("Service is unavailable after multiple attempts", ex);
    }
}
