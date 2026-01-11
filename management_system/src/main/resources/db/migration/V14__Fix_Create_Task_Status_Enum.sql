-- Create task_status enum if it doesn't exist
-- This migration recreates the task_status enum that was supposed to be created in V11

DO $$ 
BEGIN
    -- Check if task_status type exists
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'task_status') THEN
        -- Create the task_status enum type with correct values
        CREATE TYPE task_status AS ENUM ('TODO', 'IN_PROGRESS', 'COMPLETED', 'BLOCKED', 'CANCELLED');
        
        RAISE NOTICE 'Created task_status ENUM type successfully';
    ELSE
        RAISE NOTICE 'task_status ENUM type already exists - skipping creation';
    END IF;
END $$;
