-- V17: Create tables for chatbot enhancement (FAQ, Tech Stacks, Feedback)

-- FAQ entries with embeddings for common questions
CREATE TABLE faq_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question TEXT NOT NULL,
    answer TEXT NOT NULL,
    category VARCHAR(50) NOT NULL DEFAULT 'GENERAL', -- PRICING, TECHNOLOGY, TIMELINE, PROCESS, GENERAL
    language VARCHAR(10) NOT NULL DEFAULT 'vi',
    embedding DOUBLE PRECISION[],
    view_count INTEGER DEFAULT 0,
    helpful_count INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    delete_flag BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_faq_entries_category ON faq_entries(category);
CREATE INDEX idx_faq_entries_language ON faq_entries(language);
CREATE INDEX idx_faq_entries_delete_flag ON faq_entries(delete_flag);

-- Technology stacks for recommendations
CREATE TABLE technology_stacks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL, -- FRONTEND, BACKEND, DATABASE, MOBILE, DEVOPS, TESTING
    use_cases TEXT[], -- e.g., ['E-commerce', 'CMS', 'Real-time']
    pros TEXT,
    cons TEXT,
    learning_curve VARCHAR(20), -- LOW, MEDIUM, HIGH
    cost_level VARCHAR(20), -- FREE, AFFORDABLE, EXPENSIVE
    popularity_score INTEGER DEFAULT 0, -- 0-100
    language VARCHAR(10) DEFAULT 'vi',
    embedding DOUBLE PRECISION[],
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    delete_flag BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_technology_stacks_category ON technology_stacks(category);
CREATE INDEX idx_technology_stacks_delete_flag ON technology_stacks(delete_flag);

-- Chat feedback for quality improvement
CREATE TABLE chat_feedback (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES chat_conversations(id),
    message_id UUID NOT NULL,
    user_id UUID,
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    is_helpful BOOLEAN,
    feedback_text TEXT,
    issue_type VARCHAR(50), -- INACCURATE, IRRELEVANT, INCOMPLETE, RUDE, OTHER
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_chat_feedback_conversation ON chat_feedback(conversation_id);
CREATE INDEX idx_chat_feedback_rating ON chat_feedback(rating);
CREATE INDEX idx_chat_feedback_created_at ON chat_feedback(created_at);

-- Chat intents for analytics
CREATE TABLE chat_intents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES chat_conversations(id),
    detected_intent VARCHAR(50) NOT NULL, -- PRICING_INQUIRY, TECH_RECOMMENDATION, PROJECT_TIMELINE, FEATURE_REQUEST, GENERAL_INFO
    confidence_score DOUBLE PRECISION,
    extracted_entities JSONB, -- e.g., {"budget": "50M", "timeline": "3 months", "type": "web"}
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_chat_intents_conversation ON chat_intents(conversation_id);
CREATE INDEX idx_chat_intents_intent ON chat_intents(detected_intent);

-- Insert sample FAQ entries
INSERT INTO faq_entries (question, answer, category, language) VALUES
('Chi phí làm website thường là bao nhiêu?', 'Chi phí làm website dao động từ 10-100 triệu VNĐ tùy thuộc vào độ phức tạp. Website landing page đơn giản: 10-30 triệu. Website doanh nghiệp: 30-80 triệu. Website bán hàng: 50-150 triệu. Hệ thống quản lý phức tạp: 150-500 triệu trở lên.', 'PRICING', 'vi'),
('Thời gian làm một dự án web thường mất bao lâu?', 'Thời gian phát triển trung bình: Landing page: 1-2 tuần. Website công ty: 4-8 tuần. Website bán hàng: 8-16 tuần. Hệ thống lớn: 3-6 tháng trở lên. Tùy thuộc vào yêu cầu cụ thể và mức độ phức tạp của tính năng.', 'TIMELINE', 'vi'),
('Công nghệ nào phù hợp cho website bán hàng?', 'Đề xuất: Frontend: React/Vue.js cho UX tốt. Backend: Node.js (Express) hoặc Java (Spring Boot). Database: PostgreSQL/MySQL. Payment: VNPay, MoMo, ZaloPay. Hosting: AWS, Google Cloud hoặc VPS. Stack này đảm bảo hiệu năng, bảo mật và khả năng mở rộng.', 'TECHNOLOGY', 'vi'),
('Quy trình làm việc như thế nào?', 'Quy trình 5 bước: 1. Trao đổi yêu cầu & báo giá (1-3 ngày). 2. Ký hợp đồng & cọc 30-50%. 3. Thiết kế UI/UX & phát triển tính năng (60% thời gian). 4. Testing & chỉnh sửa (20% thời gian). 5. Bàn giao & hỗ trợ sau bán hàng.', 'PROCESS', 'vi'),
('Có hỗ trợ bảo hành không?', 'Chúng tôi cung cấp: Bảo hành miễn phí 6-12 tháng cho lỗi kỹ thuật. Hỗ trợ vận hành 24/7 trong 3 tháng đầu. Đào tạo sử dụng hệ thống cho team khách hàng. Bảo trì định kỳ theo gói (tùy chọn).', 'GENERAL', 'vi');

-- Insert sample technology stacks
INSERT INTO technology_stacks (name, description, category, use_cases, pros, cons, learning_curve, cost_level, popularity_score, language) VALUES
('React + Node.js + PostgreSQL', 'Stack phổ biến cho web applications hiện đại với khả năng mở rộng tốt', 'FULL_STACK', ARRAY['E-commerce', 'Social Network', 'Dashboard'], 'Hiệu năng cao, cộng đồng lớn, nhiều thư viện', 'Cần học nhiều công cụ, configuration phức tạp', 'MEDIUM', 'FREE', 95, 'vi'),
('Vue.js + Spring Boot + MySQL', 'Stack ổn định cho dự án enterprise với độ bảo mật cao', 'FULL_STACK', ARRAY['ERP', 'CRM', 'Enterprise App'], 'Bảo mật tốt, dễ bảo trì, phù hợp dự án lớn', 'Nặng hơn, thời gian phát triển lâu', 'MEDIUM', 'FREE', 88, 'vi'),
('Next.js + Prisma + PostgreSQL', 'Stack modern cho SEO-friendly websites và web apps', 'FULL_STACK', ARRAY['Landing Page', 'Blog', 'Marketing Site'], 'SEO tốt, performance cao, DX tốt', 'Phụ thuộc React ecosystem', 'MEDIUM', 'FREE', 90, 'vi'),
('React Native', 'Framework đa nền tảng cho mobile apps (iOS & Android)', 'MOBILE', ARRAY['Mobile App', 'Cross-platform'], 'Một code chạy 2 nền tảng, tiết kiệm chi phí', 'Performance không bằng native', 'MEDIUM', 'FREE', 92, 'vi'),
('Flutter', 'UI framework của Google cho mobile, web, desktop', 'MOBILE', ARRAY['Mobile App', 'Multi-platform'], 'UI đẹp, performance tốt, hot reload', 'Cộng đồng nhỏ hơn React Native', 'MEDIUM', 'FREE', 85, 'vi');

COMMENT ON TABLE faq_entries IS 'Frequently asked questions with embeddings for quick retrieval';
COMMENT ON TABLE technology_stacks IS 'Technology recommendations with pros/cons for chatbot suggestions';
COMMENT ON TABLE chat_feedback IS 'User feedback on chat responses for quality improvement';
COMMENT ON TABLE chat_intents IS 'Detected user intents for analytics and improvement';
