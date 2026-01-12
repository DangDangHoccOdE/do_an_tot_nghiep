-- ============================================================================
-- V16__create_chat_vector_tables.sql
-- Description: Chat conversations, messages, and knowledge base chunks
-- Notes: Embeddings are stored as double precision arrays for portability.
--        If pgvector extension is available, columns can be migrated later to
--        `vector` type for faster similarity search.
-- ============================================================================

-- =========================
-- Bảng lưu các cuộc hội thoại chat
-- =========================
CREATE TABLE IF NOT EXISTS chat_conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL UNIQUE,
    user_id UUID NULL,
    locale VARCHAR(10) NOT NULL DEFAULT 'en',
    title VARCHAR(255),
    last_message_at TIMESTAMP WITH TIME ZONE,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    delete_flag BOOLEAN DEFAULT FALSE NOT NULL
);

COMMENT ON TABLE chat_conversations IS 'Bảng lưu thông tin các cuộc hội thoại chat giữa người dùng và hệ thống AI';

COMMENT ON COLUMN chat_conversations.id IS 'Khóa chính của cuộc hội thoại';
COMMENT ON COLUMN chat_conversations.user_id IS 'ID người dùng (có thể null với chat ẩn danh)';
COMMENT ON COLUMN chat_conversations.locale IS 'Ngôn ngữ của cuộc hội thoại (ví dụ: en, vi)';
COMMENT ON COLUMN chat_conversations.title IS 'Tiêu đề cuộc hội thoại';
COMMENT ON COLUMN chat_conversations.last_message_at IS 'Thời điểm tin nhắn cuối cùng trong cuộc hội thoại';
COMMENT ON COLUMN chat_conversations.metadata IS 'Thông tin mở rộng (JSON) cho cuộc hội thoại';
COMMENT ON COLUMN chat_conversations.created_at IS 'Thời điểm tạo cuộc hội thoại';
COMMENT ON COLUMN chat_conversations.updated_at IS 'Thời điểm cập nhật cuộc hội thoại';
COMMENT ON COLUMN chat_conversations.delete_flag IS 'Cờ xóa mềm (true = đã xóa)';

-- =========================
-- Bảng lưu tin nhắn trong cuộc hội thoại
-- =========================
CREATE TABLE IF NOT EXISTS chat_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL UNIQUE,
    conversation_id UUID NOT NULL REFERENCES chat_conversations(id) ON DELETE CASCADE,
    role VARCHAR(30) NOT NULL,
    content TEXT NOT NULL,
    token_count INT,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    delete_flag BOOLEAN DEFAULT FALSE NOT NULL
);

COMMENT ON TABLE chat_messages IS 'Bảng lưu các tin nhắn trong mỗi cuộc hội thoại chat';

COMMENT ON COLUMN chat_messages.id IS 'Khóa chính của tin nhắn';
COMMENT ON COLUMN chat_messages.conversation_id IS 'ID cuộc hội thoại mà tin nhắn thuộc về';
COMMENT ON COLUMN chat_messages.role IS 'Vai trò gửi tin nhắn (user, assistant, system)';
COMMENT ON COLUMN chat_messages.content IS 'Nội dung tin nhắn';
COMMENT ON COLUMN chat_messages.token_count IS 'Số token của tin nhắn (phục vụ thống kê / billing)';
COMMENT ON COLUMN chat_messages.metadata IS 'Thông tin mở rộng (JSON) của tin nhắn';
COMMENT ON COLUMN chat_messages.created_at IS 'Thời điểm tạo tin nhắn';
COMMENT ON COLUMN chat_messages.updated_at IS 'Thời điểm cập nhật tin nhắn';
COMMENT ON COLUMN chat_messages.delete_flag IS 'Cờ xóa mềm (true = đã xóa)';

CREATE INDEX IF NOT EXISTS idx_chat_messages_conversation_time
ON chat_messages(conversation_id, created_at);

-- =========================
-- Bảng lưu các đoạn kiến thức (chunk) và vector embedding
-- =========================
CREATE TABLE IF NOT EXISTS ai_knowledge_chunks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL UNIQUE,
    source VARCHAR(50) NOT NULL,
    source_id VARCHAR(100),
    title VARCHAR(255),
    content TEXT NOT NULL,
    language VARCHAR(10) NOT NULL DEFAULT 'en',
    embedding DOUBLE PRECISION[] NOT NULL,
    chunk_index INT NOT NULL DEFAULT 0,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    delete_flag BOOLEAN DEFAULT FALSE NOT NULL
);

COMMENT ON TABLE ai_knowledge_chunks IS 'Bảng lưu các đoạn kiến thức (knowledge chunk) kèm vector embedding để tìm kiếm ngữ nghĩa';

COMMENT ON COLUMN ai_knowledge_chunks.id IS 'Khóa chính của knowledge chunk';
COMMENT ON COLUMN ai_knowledge_chunks.source IS 'Nguồn dữ liệu (vd: document, faq, manual, web)';
COMMENT ON COLUMN ai_knowledge_chunks.source_id IS 'ID tham chiếu tới nguồn gốc dữ liệu';
COMMENT ON COLUMN ai_knowledge_chunks.title IS 'Tiêu đề của đoạn kiến thức';
COMMENT ON COLUMN ai_knowledge_chunks.content IS 'Nội dung văn bản của đoạn kiến thức';
COMMENT ON COLUMN ai_knowledge_chunks.language IS 'Ngôn ngữ của nội dung';
COMMENT ON COLUMN ai_knowledge_chunks.embedding IS 'Vector embedding phục vụ tìm kiếm tương đồng';
COMMENT ON COLUMN ai_knowledge_chunks.chunk_index IS 'Thứ tự chunk trong tài liệu gốc';
COMMENT ON COLUMN ai_knowledge_chunks.metadata IS 'Thông tin mở rộng (JSON) cho knowledge chunk';
COMMENT ON COLUMN ai_knowledge_chunks.created_at IS 'Thời điểm tạo knowledge chunk';
COMMENT ON COLUMN ai_knowledge_chunks.updated_at IS 'Thời điểm cập nhật knowledge chunk';
COMMENT ON COLUMN ai_knowledge_chunks.delete_flag IS 'Cờ xóa mềm (true = đã xóa)';

CREATE INDEX IF NOT EXISTS idx_ai_knowledge_language
ON ai_knowledge_chunks(language);

CREATE INDEX IF NOT EXISTS idx_ai_knowledge_source
ON ai_knowledge_chunks(source, source_id);

-- Optional: ensure pgcrypto exists for UUID generation (already created in V1)
-- CREATE EXTENSION IF NOT EXISTS "pgcrypto";
