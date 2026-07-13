package com.example.attendance.attendance.dto;

import jakarta.validation.Valid;

public record ClockOutRequest(
    @Valid MemoRequest memo
) {}
