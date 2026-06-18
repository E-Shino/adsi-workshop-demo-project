package com.example.attendance.attendance.dto;

public record MonthlySummaryResponse(
    int workDays,
    int totalWorkMinutes,
    int totalOvertimeMinutes,
    int absentDays
) {
}
