package com.example.attendance.attendance.repository;

import com.example.attendance.attendance.domain.MemoType;
import com.example.attendance.attendance.entity.AttendanceMemo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceMemoRepository extends JpaRepository<AttendanceMemo, UUID> {

    Optional<AttendanceMemo> findByAttendanceRecordIdAndMemoType(UUID attendanceRecordId, MemoType memoType);

    List<AttendanceMemo> findByAttendanceRecordId(UUID attendanceRecordId);
}
