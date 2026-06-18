package com.example.attendance.auth.dto;

import com.example.attendance.employee.entity.Employee;
import com.example.attendance.employee.entity.Role;

import java.util.UUID;

public record AuthUserResponse(
    UUID id,
    String name,
    String email,
    UUID departmentId,
    String departmentName,
    Role role,
    boolean isManager
) {
    public static AuthUserResponse from(Employee employee) {
        return new AuthUserResponse(
            employee.getId(),
            employee.getName(),
            employee.getEmail(),
            employee.getDepartment().getId(),
            employee.getDepartment().getName(),
            employee.getRole(),
            employee.isManager()
        );
    }
}
