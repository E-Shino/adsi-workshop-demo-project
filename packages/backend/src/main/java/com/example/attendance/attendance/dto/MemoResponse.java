package com.example.attendance.attendance.dto;

import com.example.attendance.attendance.domain.MemoCategory;
import com.example.attendance.attendance.entity.AttendanceMemo;

import java.util.UUID;

public record MemoResponse(
    UUID id,
    MemoCategory category,
    String categoryLabel,
    String note
) {
    public static MemoResponse from(AttendanceMemo memo) {
        return new MemoResponse(
            memo.getId(),
            memo.getCategory(),
            memo.getCategory().getLabel(),
            memo.getNote()
        );
    }
}
