package com.example.attendance.attendance.dto;

import com.example.attendance.attendance.domain.AttendanceStatus;

import java.util.List;

public record TodayStatusResponse(
    AttendanceStatus status,
    List<AttendanceRecordResponse> records
) {
}
