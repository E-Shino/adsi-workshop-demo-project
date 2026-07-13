CREATE TABLE attendance_memos (
    id UUID PRIMARY KEY,
    attendance_record_id UUID NOT NULL REFERENCES attendance_records(id),
    memo_type VARCHAR(20) NOT NULL CHECK (memo_type IN ('CLOCK_IN', 'CLOCK_OUT')),
    category VARCHAR(30) NOT NULL,
    note VARCHAR(200),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX idx_attendance_memos_record_type
    ON attendance_memos(attendance_record_id, memo_type);
