package com.reliaquest.api.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeListResponseWrapper {
    private String status;
    private List<EmployeeResponseDTO> data;
}
