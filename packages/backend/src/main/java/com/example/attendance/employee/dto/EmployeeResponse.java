package com.example.attendance.employee.dto;

import com.example.attendance.employee.entity.Employee;
import com.example.attendance.employee.entity.Role;

import java.time.LocalDate;
import java.util.UUID;

public record EmployeeResponse(
    UUID id,
    String name,
    String email,
    UUID departmentId,
    String departmentName,
    Role role,
    boolean isManager,
    LocalDate hireDate,
    LocalDate retireDate
) {
    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(
            employee.getId(),
            employee.getName(),
            employee.getEmail(),
            employee.getDepartment().getId(),
            employee.getDepartment().getName(),
            employee.getRole(),
            employee.isManager(),
            employee.getHireDate(),
            employee.getRetireDate()
        );
    }
}
