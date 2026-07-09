package com.example.attendance.attendance.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MemoUpdateRequest(
    @Size(max = 100) String clockInMemo,
    @Size(max = 100) String clockOutMemo,
    @NotNull Long version
) {}
