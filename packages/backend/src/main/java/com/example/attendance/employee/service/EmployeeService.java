package com.example.attendance.employee.service;

import com.example.attendance.employee.dto.EmployeeCreateRequest;
import com.example.attendance.employee.dto.EmployeeResponse;
import com.example.attendance.employee.dto.EmployeeUpdateRequest;
import com.example.attendance.employee.dto.ManagerRequest;
import com.example.attendance.employee.dto.RetireRequest;
import com.example.attendance.employee.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface EmployeeService {

    Page<EmployeeResponse> findAll(Pageable pageable, UUID departmentId, Role role, boolean includeRetired);

    EmployeeResponse findById(UUID id);

    EmployeeResponse create(EmployeeCreateRequest request);

    EmployeeResponse update(UUID id, EmployeeUpdateRequest request);

    EmployeeResponse retire(UUID id, RetireRequest request);

    EmployeeResponse setManager(UUID id, ManagerRequest request);
}
