package com.example.attendance.attendance.dto;

import com.example.attendance.attendance.domain.MemoCategory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MemoRequest(
    @NotNull MemoCategory category,
    @Size(max = 200) String note
) {}
