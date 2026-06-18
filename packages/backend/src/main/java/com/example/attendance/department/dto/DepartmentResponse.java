package com.example.attendance.department.dto;

import com.example.attendance.department.entity.Department;

import java.util.UUID;

public record DepartmentResponse(
    UUID id,
    String name
) {
    public static DepartmentResponse from(Department department) {
        return new DepartmentResponse(department.getId(), department.getName());
    }
}
