package com.example.attendance.employee.dto;

import com.example.attendance.employee.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record EmployeeCreateRequest(
    @NotBlank(message = "氏名は必須です")
    @Size(max = 100, message = "氏名は100文字以内で入力してください")
    String name,

    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "メールアドレスの形式が正しくありません")
    @Size(max = 255, message = "メールアドレスは255文字以内で入力してください")
    String email,

    @NotBlank(message = "パスワードは必須です")
    @Size(min = 8, max = 100, message = "パスワードは8文字以上100文字以内で入力してください")
    String password,

    @NotNull(message = "部署IDは必須です")
    UUID departmentId,

    @NotNull(message = "ロールは必須です")
    Role role,

    @NotNull(message = "入社日は必須です")
    LocalDate hireDate
) {
}
