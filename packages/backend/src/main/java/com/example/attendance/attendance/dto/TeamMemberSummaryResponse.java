package com.example.attendance.attendance.dto;

import java.util.UUID;

public record TeamMemberSummaryResponse(
    UUID employeeId,
    String employeeName,
    int workDays,
    int totalWorkMinutes,
    int totalOvertimeMinutes,
    int absentDays
) {
}
