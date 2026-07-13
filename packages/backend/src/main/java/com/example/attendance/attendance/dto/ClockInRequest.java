package com.example.attendance.attendance.dto;

import jakarta.validation.Valid;

public record ClockInRequest(
    @Valid MemoRequest memo
) {}
