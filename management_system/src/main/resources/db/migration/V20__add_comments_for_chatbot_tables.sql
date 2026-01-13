-- ============================================================================
-- V19__add_comments_for_chatbot_tables.sql
-- Description: Add comments for chatbot-related tables, columns and enums
-- Purpose    : Improve schema clarity, maintainability, and analytics understanding
-- Safe       : COMMENT ONLY (no schema/data changes)
-- ============================================================================

-- =========================
-- Enum comments
-- =========================
COMMENT ON TYPE issue_type_enum IS
'Enum for feedback issue types:
INACCURATE (Không chính xác),
IRRELEVANT (Không liên quan),
INCOMPLETE (Không đầy đủ),
RUDE (Thô lỗ),
OTHER (Khác)';

-- =========================
-- FAQ entries
-- =========================
COMMENT ON TABLE faq_entries IS
'Danh sách câu hỏi thường gặp (FAQ) dùng cho chatbot, có hỗ trợ embedding để semantic search';

COMMENT ON COLUMN faq_entries.id IS 'ID duy nhất của FAQ';
COMMENT ON COLUMN faq_entries.question IS 'Câu hỏi người dùng thường hỏi';
COMMENT ON COLUMN faq_entries.answer IS 'Câu trả lời chuẩn của hệ thống';
COMMENT ON COLUMN faq_entries.category IS 'Nhóm câu hỏi: PRICING, TECHNOLOGY, TIMELINE, PROCESS, GENERAL';
COMMENT ON COLUMN faq_entries.language IS 'Ngôn ngữ của FAQ (vi, en, ...)';
COMMENT ON COLUMN faq_entries.embedding IS 'Vector embedding phục vụ tìm kiếm ngữ nghĩa (semantic search)';
COMMENT ON COLUMN faq_entries.view_count IS 'Số lần FAQ được hiển thị';
COMMENT ON COLUMN faq_entries.helpful_count IS 'Số lần người dùng đánh giá FAQ là hữu ích';
COMMENT ON COLUMN faq_entries.created_at IS 'Thời điểm tạo FAQ';
COMMENT ON COLUMN faq_entries.updated_at IS 'Thời điểm cập nhật FAQ';
COMMENT ON COLUMN faq_entries.delete_flag IS 'Cờ xóa mềm (true = đã xóa)';

-- =========================
-- Technology stacks
-- =========================
COMMENT ON TABLE technology_stacks IS
'Danh sách stack công nghệ dùng để chatbot gợi ý giải pháp kỹ thuật';

COMMENT ON COLUMN technology_stacks.id IS 'ID duy nhất của technology stack';
COMMENT ON COLUMN technology_stacks.name IS 'Tên stack công nghệ';
COMMENT ON COLUMN technology_stacks.description IS 'Mô tả tổng quan về stack';
COMMENT ON COLUMN technology_stacks.category IS
'Nhóm công nghệ: FRONTEND, BACKEND, DATABASE, MOBILE, DEVOPS, TESTING, FULL_STACK';
COMMENT ON COLUMN technology_stacks.use_cases IS
'Danh sách use case phù hợp (E-commerce, CMS, Real-time, ...)';
COMMENT ON COLUMN technology_stacks.pros IS 'Ưu điểm của stack';
COMMENT ON COLUMN technology_stacks.cons IS 'Nhược điểm của stack';
COMMENT ON COLUMN technology_stacks.learning_curve IS 'Độ khó học: LOW, MEDIUM, HIGH';
COMMENT ON COLUMN technology_stacks.cost_level IS
'Chi phí triển khai: FREE, AFFORDABLE, EXPENSIVE';
COMMENT ON COLUMN technology_stacks.popularity_score IS 'Mức độ phổ biến (0-100)';
COMMENT ON COLUMN technology_stacks.language IS 'Ngôn ngữ nội dung mô tả';
COMMENT ON COLUMN technology_stacks.embedding IS
'Vector embedding để chatbot recommend theo ngữ cảnh';
COMMENT ON COLUMN technology_stacks.created_at IS 'Thời điểm tạo';
COMMENT ON COLUMN technology_stacks.updated_at IS 'Thời điểm cập nhật';
COMMENT ON COLUMN technology_stacks.delete_flag IS 'Cờ xóa mềm';

-- =========================
-- Chat feedback
-- =========================
COMMENT ON TABLE chat_feedback IS
'Phản hồi của người dùng về chất lượng câu trả lời của chatbot';

COMMENT ON COLUMN chat_feedback.id IS 'ID phản hồi';
COMMENT ON COLUMN chat_feedback.conversation_id IS 'ID cuộc hội thoại';
COMMENT ON COLUMN chat_feedback.message_id IS 'ID message được đánh giá';
COMMENT ON COLUMN chat_feedback.user_id IS
'ID người dùng gửi phản hồi (nullable nếu ẩn danh)';
COMMENT ON COLUMN chat_feedback.rating IS 'Đánh giá sao từ 1 đến 5';
COMMENT ON COLUMN chat_feedback.is_helpful IS
'Người dùng thấy câu trả lời có hữu ích hay không';
COMMENT ON COLUMN chat_feedback.feedback_text IS 'Nội dung góp ý chi tiết';
COMMENT ON COLUMN chat_feedback.issue_type IS
'Loại vấn đề: INACCURATE, IRRELEVANT, INCOMPLETE, RUDE, OTHER';
COMMENT ON COLUMN chat_feedback.created_at IS 'Thời điểm gửi feedback';

-- =========================
-- Chat intents
-- =========================
COMMENT ON TABLE chat_intents IS
'Intent người dùng được chatbot phát hiện để phục vụ analytics và cải thiện mô hình';

COMMENT ON COLUMN chat_intents.id IS 'ID intent record';
COMMENT ON COLUMN chat_intents.conversation_id IS 'ID cuộc hội thoại';
COMMENT ON COLUMN chat_intents.detected_intent IS
'Intent phát hiện được (PRICING_INQUIRY, TECH_RECOMMENDATION, ...)';
COMMENT ON COLUMN chat_intents.confidence_score IS
'Độ tin cậy của intent (0-1)';
COMMENT ON COLUMN chat_intents.extracted_entities IS
'Entity trích xuất từ câu hỏi người dùng (JSON)';
COMMENT ON COLUMN chat_intents.created_at IS 'Thời điểm ghi nhận intent';
