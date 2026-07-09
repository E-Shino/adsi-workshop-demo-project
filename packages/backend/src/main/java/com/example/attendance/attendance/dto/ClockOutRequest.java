package com.example.attendance.attendance.dto;

import jakarta.validation.constraints.Size;

public record ClockOutRequest(
    @Size(max = 100) String memo
) {}
