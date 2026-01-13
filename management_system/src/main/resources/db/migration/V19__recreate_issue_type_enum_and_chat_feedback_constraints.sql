-- ============================================================================
-- V19__recreate_issue_type_enum_and_chat_feedback_constraints.sql
-- Description: Recreate issue_type_enum and related constraints for chat_feedback
-- Strategy  : DROP IF EXISTS → CREATE AGAIN
-- Warning   : This migration modifies schema (enum + constraints)
-- ============================================================================

SET client_encoding = 'UTF8';

-- =========================
-- 1. Drop constraints if exist
-- =========================
ALTER TABLE IF EXISTS chat_feedback
    DROP CONSTRAINT IF EXISTS check_rating_range;

ALTER TABLE IF EXISTS chat_feedback
    DROP CONSTRAINT IF EXISTS check_feedback_content;

-- =========================
-- 2. Revert column type to TEXT (if enum exists)
-- =========================
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_type
        WHERE typname = 'issue_type_enum'
    ) THEN
        ALTER TABLE chat_feedback
        ALTER COLUMN issue_type TYPE TEXT
        USING issue_type::text;
    END IF;
END$$;

-- =========================
-- 3. Drop enum if exists
-- =========================
DROP TYPE IF EXISTS issue_type_enum;

-- =========================
-- 4. Recreate enum
-- =========================
CREATE TYPE issue_type_enum AS ENUM (
    'INACCURATE',
    'IRRELEVANT',
    'INCOMPLETE',
    'RUDE',
    'OTHER'
);

-- =========================
-- 5. Convert column back to enum
-- =========================
ALTER TABLE chat_feedback
ALTER COLUMN issue_type TYPE issue_type_enum
USING issue_type::issue_type_enum;

-- =========================
-- 6. Recreate constraints
-- =========================

-- Rating must be between 1 and 5
ALTER TABLE chat_feedback
ADD CONSTRAINT check_rating_range
CHECK (
    rating IS NULL
    OR (rating >= 1 AND rating <= 5)
);

-- At least one feedback field must be provided
ALTER TABLE chat_feedback
ADD CONSTRAINT check_feedback_content
CHECK (
    is_helpful IS NOT NULL
    OR rating IS NOT NULL
    OR feedback_text IS NOT NULL
    OR issue_type IS NOT NULL
);

-- =========================
-- 7. Add comment
-- =========================
COMMENT ON TYPE issue_type_enum IS
'Enum for feedback issue types:
- INACCURATE  (Không chính xác)
- IRRELEVANT  (Không liên quan)
- INCOMPLETE (Không đầy đủ)
- RUDE        (Thô lỗ)
- OTHER       (Khác)';
