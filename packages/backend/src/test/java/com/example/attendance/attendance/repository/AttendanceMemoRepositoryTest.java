package com.example.attendance.attendance.repository;

import com.example.attendance.attendance.domain.MemoCategory;
import com.example.attendance.attendance.domain.MemoType;
import com.example.attendance.attendance.entity.AttendanceMemo;
import com.example.attendance.attendance.entity.AttendanceRecord;
import com.example.attendance.department.entity.Department;
import com.example.attendance.employee.entity.Employee;
import com.example.attendance.employee.entity.Role;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.example.attendance.common.config.JpaAuditingConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
class AttendanceMemoRepositoryTest {

    @Autowired
    private AttendanceMemoRepository memoRepository;

    @Autowired
    private EntityManager em;

    private AttendanceRecord record;

    @BeforeEach
    void setUp() {
        var department = Department.builder()
                .id(UUID.randomUUID())
                .name("Engineering")
                .build();
        em.persist(department);

        var employee = Employee.builder()
                .id(UUID.randomUUID())
                .name("田中太郎")
                .email("tanaka@example.com")
                .password("hashed-password")
                .department(department)
                .role(Role.EMPLOYEE)
                .isManager(false)
                .hireDate(LocalDate.of(2024, 4, 1))
                .build();
        em.persist(employee);

        record = AttendanceRecord.builder()
                .id(UUID.randomUUID())
                .employee(employee)
                .workDate(LocalDate.of(2025, 1, 15))
                .clockIn(Instant.parse("2025-01-14T23:00:00Z"))
                .corrected(false)
                .build();
        em.persist(record);
        em.flush();
    }

    @Test
    @DisplayName("打刻レコードIDとメモタイプでメモが検索できる")
    void findByAttendanceRecordIdAndMemoType_existing_returnsMemo() {
        // Arrange
        var memo = AttendanceMemo.builder()
                .id(UUID.randomUUID())
                .attendanceRecord(record)
                .memoType(MemoType.CLOCK_IN)
                .category(MemoCategory.DIRECT_GO)
                .note("客先訪問")
                .build();
        em.persist(memo);
        em.flush();

        // Act
        var result = memoRepository.findByAttendanceRecordIdAndMemoType(record.getId(), MemoType.CLOCK_IN);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getCategory()).isEqualTo(MemoCategory.DIRECT_GO);
        assertThat(result.get().getNote()).isEqualTo("客先訪問");
    }

    @Test
    @DisplayName("存在しないメモタイプで検索すると空が返る")
    void findByAttendanceRecordIdAndMemoType_notExisting_returnsEmpty() {
        // Act
        var result = memoRepository.findByAttendanceRecordIdAndMemoType(record.getId(), MemoType.CLOCK_OUT);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("打刻レコードIDでメモ一覧が検索できる")
    void findByAttendanceRecordId_withMemos_returnsList() {
        // Arrange
        var clockInMemo = AttendanceMemo.builder()
                .id(UUID.randomUUID())
                .attendanceRecord(record)
                .memoType(MemoType.CLOCK_IN)
                .category(MemoCategory.DIRECT_GO)
                .note("客先訪問")
                .build();
        var clockOutMemo = AttendanceMemo.builder()
                .id(UUID.randomUUID())
                .attendanceRecord(record)
                .memoType(MemoType.CLOCK_OUT)
                .category(MemoCategory.DIRECT_RETURN)
                .note(null)
                .build();
        em.persist(clockInMemo);
        em.persist(clockOutMemo);
        em.flush();

        // Act
        var result = memoRepository.findByAttendanceRecordId(record.getId());

        // Assert
        assertThat(result).hasSize(2);
    }
}
