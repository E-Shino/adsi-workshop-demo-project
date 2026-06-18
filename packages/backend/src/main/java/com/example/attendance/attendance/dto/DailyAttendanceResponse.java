package com.example.attendance.attendance.dto;

import java.time.LocalDate;
import java.util.List;

public record DailyAttendanceResponse(
    LocalDate date,
    List<AttendanceRecordResponse> records,
    int totalWorkMinutes,
    int breakMinutes,
    int workMinutes,
    int overtimeMinutes
) {
}
