package com.example.attendance.department.service;

import com.example.attendance.department.dto.DepartmentRequest;
import com.example.attendance.department.dto.DepartmentResponse;

import java.util.List;
import java.util.UUID;

public interface DepartmentService {

    List<DepartmentResponse> findAll();

    DepartmentResponse create(DepartmentRequest request);

    DepartmentResponse update(UUID id, DepartmentRequest request);
}
