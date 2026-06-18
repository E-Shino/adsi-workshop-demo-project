package com.example.attendance.employee.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RetireRequest(
    @NotNull(message = "退職日は必須です")
    LocalDate retireDate
) {
}
