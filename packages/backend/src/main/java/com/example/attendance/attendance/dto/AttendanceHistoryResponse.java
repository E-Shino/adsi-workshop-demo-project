package com.example.attendance.attendance.dto;

import java.util.List;

public record AttendanceHistoryResponse(
    String month,
    List<DailyAttendanceResponse> days,
    MonthlySummaryResponse summary
) {
}
