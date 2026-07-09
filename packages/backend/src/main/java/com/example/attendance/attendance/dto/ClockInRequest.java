package com.example.attendance.attendance.dto;

import jakarta.validation.constraints.Size;

public record ClockInRequest(
    @Size(max = 100) String memo
) {}
