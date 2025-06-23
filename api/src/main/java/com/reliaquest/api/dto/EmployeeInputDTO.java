package com.reliaquest.api.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeInputDTO {
    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Salary is required") @Min(value = 1, message = "Salary must be greater than 0")
    private Integer salary;

    @NotNull(message = "Age is required") private Integer age;

    @NotBlank(message = "Title is required")
    private String title;
}
