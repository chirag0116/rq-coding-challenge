package com.reliaquest.api.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeResponseDTO {
    private String id;

    private String employee_name;

    private Integer employee_salary;

    private Integer employee_age;

    private String employee_title;

    private String employee_email;
}
