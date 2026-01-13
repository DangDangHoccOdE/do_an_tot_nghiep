-- V18: Add issue_type enum and constraint for chat_feedback

-- Create ENUM type for issue_type
CREATE TYPE issue_type_enum AS ENUM (
    'INACCURATE',
    'IRRELEVANT', 
    'INCOMPLETE',
    'RUDE',
    'OTHER'
);

-- Update chat_feedback table to use enum
ALTER TABLE chat_feedback 
ALTER COLUMN issue_type TYPE issue_type_enum USING issue_type::issue_type_enum;

-- Add constraint for rating
ALTER TABLE chat_feedback
ADD CONSTRAINT check_rating_range CHECK (rating IS NULL OR (rating >= 1 AND rating <= 5));

-- Add constraint to ensure at least one feedback is provided
ALTER TABLE chat_feedback
ADD CONSTRAINT check_feedback_content CHECK (
    is_helpful IS NOT NULL OR 
    rating IS NOT NULL OR 
    feedback_text IS NOT NULL OR
    issue_type IS NOT NULL
);

-- Create comment for clarity
COMMENT ON TYPE issue_type_enum IS 'Enum for feedback issue types: INACCURATE (Không chính xác), IRRELEVANT (Không liên quan), INCOMPLETE (Không đầy đủ), RUDE (Thô lỗ), OTHER (Khác)';

