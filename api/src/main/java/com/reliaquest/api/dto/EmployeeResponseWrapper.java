package com.reliaquest.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeResponseWrapper {
    private String status;
    private EmployeeResponseDTO data;
}
