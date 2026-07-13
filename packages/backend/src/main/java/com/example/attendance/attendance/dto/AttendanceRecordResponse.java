package com.example.attendance.attendance.dto;

import com.example.attendance.attendance.entity.AttendanceMemo;
import com.example.attendance.attendance.entity.AttendanceRecord;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AttendanceRecordResponse(
    UUID id,
    LocalDate workDate,
    Instant clockIn,
    Instant clockOut,
    boolean corrected,
    MemoResponse clockInMemo,
    MemoResponse clockOutMemo
) {
    public static AttendanceRecordResponse from(AttendanceRecord record) {
        return new AttendanceRecordResponse(
            record.getId(),
            record.getWorkDate(),
            record.getClockIn(),
            record.getClockOut(),
            record.isCorrected(),
            null,
            null
        );
    }

    public static AttendanceRecordResponse from(AttendanceRecord record, List<AttendanceMemo> memos) {
        MemoResponse clockInMemo = null;
        MemoResponse clockOutMemo = null;
        for (var memo : memos) {
            switch (memo.getMemoType()) {
                case CLOCK_IN -> clockInMemo = MemoResponse.from(memo);
                case CLOCK_OUT -> clockOutMemo = MemoResponse.from(memo);
            }
        }
        return new AttendanceRecordResponse(
            record.getId(),
            record.getWorkDate(),
            record.getClockIn(),
            record.getClockOut(),
            record.isCorrected(),
            clockInMemo,
            clockOutMemo
        );
    }
}
