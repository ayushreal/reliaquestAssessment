package com.reliaquest.api.model;


import jakarta.validation.constraints.*;


public record CreateEmployeeRequest(
@NotBlank String name,
@NotNull @Positive Integer salary,
@NotNull @Min(16) @Max(75) Integer age,
@NotBlank String title) {}