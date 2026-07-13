package com.example.attendance.attendance.service;

import com.example.attendance.attendance.domain.MemoCategory;
import com.example.attendance.attendance.domain.MemoType;
import com.example.attendance.attendance.dto.MemoRequest;
import com.example.attendance.attendance.entity.AttendanceMemo;
import com.example.attendance.attendance.entity.AttendanceRecord;
import com.example.attendance.attendance.repository.AttendanceMemoRepository;
import com.example.attendance.attendance.repository.AttendanceRecordRepository;
import com.example.attendance.department.entity.Department;
import com.example.attendance.employee.entity.Employee;
import com.example.attendance.employee.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemoServiceTest {

    @Mock
    private AttendanceMemoRepository memoRepository;

    @Mock
    private AttendanceRecordRepository attendanceRecordRepository;

    private MemoServiceImpl service;

    private Employee employee;
    private AttendanceRecord record;

    @BeforeEach
    void setUp() {
        service = new MemoServiceImpl(memoRepository, attendanceRecordRepository);

        var department = Department.builder()
                .id(UUID.randomUUID())
                .name("Engineering")
                .build();

        employee = Employee.builder()
                .id(UUID.randomUUID())
                .name("田中太郎")
                .email("tanaka@example.com")
                .password("hashed")
                .department(department)
                .role(Role.EMPLOYEE)
                .isManager(false)
                .hireDate(LocalDate.of(2024, 4, 1))
                .build();

        record = AttendanceRecord.builder()
                .id(UUID.randomUUID())
                .employee(employee)
                .workDate(LocalDate.of(2025, 1, 15))
                .clockIn(Instant.parse("2025-01-14T23:00:00Z"))
                .corrected(false)
                .build();
    }

    @Nested
    @DisplayName("メモ保存")
    class SaveMemo {

        @Test
        @DisplayName("出勤メモが正常に保存できる")
        void saveMemo_clockIn_validCategory_createsMemo() {
            // Arrange
            var request = new MemoRequest(MemoCategory.DIRECT_GO, "客先訪問");
            when(attendanceRecordRepository.findById(record.getId())).thenReturn(Optional.of(record));
            when(memoRepository.findByAttendanceRecordIdAndMemoType(record.getId(), MemoType.CLOCK_IN))
                    .thenReturn(Optional.empty());
            when(memoRepository.save(any(AttendanceMemo.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            var result = service.saveMemo(record.getId(), MemoType.CLOCK_IN, request, employee.getId());

            // Assert
            assertThat(result.category()).isEqualTo(MemoCategory.DIRECT_GO);
            assertThat(result.categoryLabel()).isEqualTo("直行");
            assertThat(result.note()).isEqualTo("客先訪問");

            var captor = ArgumentCaptor.forClass(AttendanceMemo.class);
            verify(memoRepository).save(captor.capture());
            assertThat(captor.getValue().getMemoType()).isEqualTo(MemoType.CLOCK_IN);
        }

        @Test
        @DisplayName("退勤カテゴリを出勤メモに指定すると400エラー")
        void saveMemo_clockIn_invalidCategory_throwsBadRequest() {
            // Arrange
            var request = new MemoRequest(MemoCategory.DIRECT_RETURN, "");
            when(attendanceRecordRepository.findById(record.getId())).thenReturn(Optional.of(record));

            // Act & Assert
            assertThatThrownBy(() -> service.saveMemo(record.getId(), MemoType.CLOCK_IN, request, employee.getId()))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("400");
        }

        @Test
        @DisplayName("「その他」カテゴリでnoteが空の場合は400エラー")
        void saveMemo_otherCategory_emptyNote_throwsBadRequest() {
            // Arrange
            var request = new MemoRequest(MemoCategory.OTHER, "");
            when(attendanceRecordRepository.findById(record.getId())).thenReturn(Optional.of(record));

            // Act & Assert
            assertThatThrownBy(() -> service.saveMemo(record.getId(), MemoType.CLOCK_IN, request, employee.getId()))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("400");
        }

        @Test
        @DisplayName("「その他」カテゴリでnoteがnullの場合は400エラー")
        void saveMemo_otherCategory_nullNote_throwsBadRequest() {
            // Arrange
            var request = new MemoRequest(MemoCategory.OTHER, null);
            when(attendanceRecordRepository.findById(record.getId())).thenReturn(Optional.of(record));

            // Act & Assert
            assertThatThrownBy(() -> service.saveMemo(record.getId(), MemoType.CLOCK_IN, request, employee.getId()))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("400");
        }

        @Test
        @DisplayName("他人のレコードにメモを保存すると403エラー")
        void saveMemo_differentEmployee_throwsForbidden() {
            // Arrange
            var request = new MemoRequest(MemoCategory.DIRECT_GO, "");
            when(attendanceRecordRepository.findById(record.getId())).thenReturn(Optional.of(record));
            var otherEmployeeId = UUID.randomUUID();

            // Act & Assert
            assertThatThrownBy(() -> service.saveMemo(record.getId(), MemoType.CLOCK_IN, request, otherEmployeeId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("403");
        }

        @Test
        @DisplayName("存在しない打刻レコードに対して404エラー")
        void saveMemo_recordNotFound_throwsNotFound() {
            // Arrange
            var request = new MemoRequest(MemoCategory.DIRECT_GO, "");
            var nonExistingId = UUID.randomUUID();
            when(attendanceRecordRepository.findById(nonExistingId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> service.saveMemo(nonExistingId, MemoType.CLOCK_IN, request, employee.getId()))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("404");
        }
    }

    @Nested
    @DisplayName("メモ更新")
    class UpdateMemo {

        @Test
        @DisplayName("既存メモが正常に更新できる")
        void updateMemo_existing_updatesFields() {
            // Arrange
            var existingMemo = AttendanceMemo.builder()
                    .id(UUID.randomUUID())
                    .attendanceRecord(record)
                    .memoType(MemoType.CLOCK_IN)
                    .category(MemoCategory.DIRECT_GO)
                    .note("旧メモ")
                    .build();
            var request = new MemoRequest(MemoCategory.REMOTE, "在宅勤務に変更");
            when(attendanceRecordRepository.findById(record.getId())).thenReturn(Optional.of(record));
            when(memoRepository.findByAttendanceRecordIdAndMemoType(record.getId(), MemoType.CLOCK_IN))
                    .thenReturn(Optional.of(existingMemo));
            when(memoRepository.save(any(AttendanceMemo.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            var result = service.updateMemo(record.getId(), "CLOCK_IN", request, employee.getId());

            // Assert
            assertThat(result.category()).isEqualTo(MemoCategory.REMOTE);
            assertThat(result.note()).isEqualTo("在宅勤務に変更");
        }

        @Test
        @DisplayName("存在しないメモを更新すると404エラー")
        void updateMemo_notFound_throwsNotFound() {
            // Arrange
            var request = new MemoRequest(MemoCategory.REMOTE, "");
            when(attendanceRecordRepository.findById(record.getId())).thenReturn(Optional.of(record));
            when(memoRepository.findByAttendanceRecordIdAndMemoType(record.getId(), MemoType.CLOCK_IN))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> service.updateMemo(record.getId(), "CLOCK_IN", request, employee.getId()))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("404");
        }
    }

    @Nested
    @DisplayName("メモ削除")
    class DeleteMemo {

        @Test
        @DisplayName("既存メモが正常に削除できる")
        void deleteMemo_existing_deletesMemo() {
            // Arrange
            var existingMemo = AttendanceMemo.builder()
                    .id(UUID.randomUUID())
                    .attendanceRecord(record)
                    .memoType(MemoType.CLOCK_IN)
                    .category(MemoCategory.DIRECT_GO)
                    .build();
            when(attendanceRecordRepository.findById(record.getId())).thenReturn(Optional.of(record));
            when(memoRepository.findByAttendanceRecordIdAndMemoType(record.getId(), MemoType.CLOCK_IN))
                    .thenReturn(Optional.of(existingMemo));

            // Act
            service.deleteMemo(record.getId(), "CLOCK_IN", employee.getId());

            // Assert
            verify(memoRepository).delete(existingMemo);
        }

        @Test
        @DisplayName("存在しないメモを削除すると404エラー")
        void deleteMemo_notFound_throwsNotFound() {
            // Arrange
            when(attendanceRecordRepository.findById(record.getId())).thenReturn(Optional.of(record));
            when(memoRepository.findByAttendanceRecordIdAndMemoType(record.getId(), MemoType.CLOCK_IN))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> service.deleteMemo(record.getId(), "CLOCK_IN", employee.getId()))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("404");
        }
    }
}
