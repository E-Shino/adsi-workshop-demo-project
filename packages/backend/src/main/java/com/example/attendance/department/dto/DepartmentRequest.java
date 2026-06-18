package com.example.attendance.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(
    @NotBlank(message = "部署名は必須です")
    @Size(max = 100, message = "部署名は100文字以内で入力してください")
    String name
) {
}
