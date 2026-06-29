CREATE TABLE attendance_corrections (
    id UUID PRIMARY KEY,
    attendance_record_id UUID REFERENCES attendance_records(id),
    requester_id UUID NOT NULL REFERENCES employees(id),
    approver_id UUID REFERENCES employees(id),
    target_date DATE NOT NULL,
    corrected_clock_in TIMESTAMP WITH TIME ZONE NOT NULL,
    corrected_clock_out TIMESTAMP WITH TIME ZONE NOT NULL,
    reason VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    reject_reason VARCHAR(500),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_attendance_corrections_requester ON attendance_corrections(requester_id);
CREATE INDEX idx_attendance_corrections_status ON attendance_corrections(status);
