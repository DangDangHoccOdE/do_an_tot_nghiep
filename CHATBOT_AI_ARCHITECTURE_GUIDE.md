# Hướng Dẫn Chi Tiết Kiến Trúc Chatbot AI Gợi Ý Sản Phẩm

## Mục Lục

1. [Tổng Quan](#tổng-quan)
2. [Cấu Trúc Cơ Sở Dữ Liệu (Database)](#cấu-trúc-cơ-sở-dữ-liệu)
3. [Các Thành Phần Chính](#các-thành-phần-chính)
4. [Flow Hoạt Động của Chatbot](#flow-hoạt-động-của-chatbot)
5. [Cách Sử Dụng API](#cách-sử-dụng-api)
6. [Dữ Liệu Được Lưu](#dữ-liệu-được-lưu)
7. [Những Cải Thiện Cần Thực Hiện](#những-cải-thiện-cần-thực-hiện)

---

## Tổng Quan

### Chatbot AI Là Gì?

Chatbot AI gợi ý sản phẩm là một hệ thống trí tuệ nhân tạo giúp:

- **Tư vấn dự án** (Project Consulting)
- **Gợi ý công nghệ** (Technology Recommendations)
- **Cung cấp thông tin về Timeline, Budget, Process**
- **Trả lời FAQ** (Frequently Asked Questions)
- **Quản lý từng cuộc hội thoại** với lịch sử chat

### Công Nghệ Sử Dụng

```
┌─────────────────────────────────────────────────────────┐
│                  Frontend (Vue.js)                       │
│           (gọi API /api/v1/chat/ask)                    │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│         Backend (Java Spring Boot)                       │
│  ┌──────────────────────────────────────────────────┐   │
│  │  ChatController → ChatServiceImpl                 │   │
│  └──────────────────────────────────────────────────┘   │
│                         ↓                                 │
│  ┌──────────────────────────────────────────────────┐   │
│  │  AiGatewayService (Kết nối AI)                  │   │
│  │  - Google Gemini (mặc định)                      │   │
│  │  - OpenAI GPT-4o-mini (tùy chọn)                │   │
│  └──────────────────────────────────────────────────┘   │
│                         ↓                                 │
│  ┌──────────────────────────────────────────────────┐   │
│  │  Vector Store (PostgreSQL JSONB)                 │   │
│  │  - Lưu embeddings của tất cả kiến thức           │   │
│  │  - Cosine similarity search                      │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                         ↓
        ┌────────────────┬────────────────┐
        ↓                ↓                ↓
   PostgreSQL        Google Gemini    OpenAI API
   (Database)        (AI Provider)    (AI Provider)
```

---

## Cấu Trúc Cơ Sở Dữ Liệu

### 1. **ChatConversation** - Bảng Cuộc Hội Thoại

```sql
CREATE TABLE chat_conversations (
    id UUID PRIMARY KEY,
    user_id UUID,
    locale VARCHAR(10) DEFAULT 'en',
    title VARCHAR(255),
    last_message_at TIMESTAMP WITH TIME ZONE,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    delete_flag BOOLEAN
);
```

**Mục đích:**

- Lưu các cuộc hội thoại giữa người dùng và chatbot
- Theo dõi ngôn ngữ được sử dụng (en, vi, ja, ...)
- Lưu thời gian tin nhắn cuối cùng

**Ví dụ dữ liệu:**

```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "user_id": "998e4567-e89b-12d3-a456-426614174000",
  "locale": "vi",
  "title": "Tư vấn dự án website thương mại điện tử",
  "last_message_at": "2026-01-13T10:30:00Z",
  "metadata": {
    "source": "website",
    "ip": "192.168.1.1"
  }
}
```

---

### 2. **ChatMessage** - Bảng Tin Nhắn

```sql
CREATE TABLE chat_messages (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL,
    role VARCHAR(30) NOT NULL, -- 'user' hoặc 'assistant'
    content TEXT NOT NULL,
    token_count INTEGER,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    delete_flag BOOLEAN,
    FOREIGN KEY (conversation_id) REFERENCES chat_conversations(id)
);
```

**Mục đích:**

- Lưu từng tin nhắn trong cuộc hội thoại
- Phân biệt tin nhắn từ người dùng (user) hay AI (assistant)
- Tính toán token count cho cost tracking

**Ví dụ dữ liệu:**

```json
{
  "id": "223e4567-e89b-12d3-a456-426614174000",
  "conversation_id": "123e4567-e89b-12d3-a456-426614174000",
  "role": "user",
  "content": "Tôi muốn xây dựng một website thương mại điện tử với budget 50 triệu đồng. Cần bao lâu?",
  "token_count": 25,
  "created_at": "2026-01-13T10:20:00Z"
}
```

---

### 3. **ChatIntent** - Bảng Phát Hiện Ý Định

```sql
CREATE TABLE chat_intents (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL,
    detected_intent VARCHAR(50) NOT NULL,
    confidence_score DOUBLE PRECISION,
    extracted_entities JSONB,
    created_at TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (conversation_id) REFERENCES chat_conversations(id),
    INDEX idx_chat_intents_conversation (conversation_id),
    INDEX idx_chat_intents_intent (detected_intent)
);
```

**Mục đích:**

- Phát hiện ý định của người dùng
- Lưu trữ độ tin cậy của phát hiện
- Trích xuất entities như budget, timeline, type

**Các Ý Định (Intents) Có Thể:**

- `PRICING_INQUIRY` - Hỏi về giá cả
- `TECH_RECOMMENDATION` - Yêu cầu gợi ý công nghệ
- `PROJECT_TIMELINE` - Hỏi về thời gian dự án
- `FEATURE_REQUEST` - Yêu cầu tính năng
- `GENERAL_INFO` - Hỏi thông tin chung

**Ví dụ dữ liệu:**

```json
{
  "id": "323e4567-e89b-12d3-a456-426614174000",
  "conversation_id": "123e4567-e89b-12d3-a456-426614174000",
  "detected_intent": "PRICING_INQUIRY",
  "confidence_score": 0.87,
  "extracted_entities": {
    "budget": "50M",
    "timeline": "3 months",
    "project_type": "website",
    "features": ["ecommerce", "payment_gateway"]
  },
  "created_at": "2026-01-13T10:20:00Z"
}
```

---

### 4. **ChatFeedback** - Bảng Phản Hồi Người Dùng

```sql
CREATE TABLE chat_feedback (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL,
    message_id UUID NOT NULL,
    user_id UUID,
    rating INTEGER, -- 1-5
    is_helpful BOOLEAN,
    feedback_text TEXT,
    issue_type VARCHAR(50), -- INACCURATE, IRRELEVANT, INCOMPLETE, RUDE, OTHER
    created_at TIMESTAMP WITH TIME ZONE,
    INDEX idx_chat_feedback_conversation (conversation_id),
    INDEX idx_chat_feedback_rating (rating),
    INDEX idx_chat_feedback_created_at (created_at),
    FOREIGN KEY (conversation_id) REFERENCES chat_conversations(id)
);
```

**Mục đích:**

- Collect feedback từ người dùng về chất lượng response
- Cải thiện AI models dựa trên feedback
- Theo dõi các vấn đề phổ biến

**Ví dụ dữ liệu:**

```json
{
  "id": "423e4567-e89b-12d3-a456-426614174000",
  "conversation_id": "123e4567-e89b-12d3-a456-426614174000",
  "message_id": "223e4567-e89b-12d3-a456-426614174000",
  "user_id": "998e4567-e89b-12d3-a456-426614174000",
  "rating": 4,
  "is_helpful": true,
  "feedback_text": "Gợi ý công nghệ rất hữu ích, nhưng cần chi tiết hơn về chi phí",
  "issue_type": null,
  "created_at": "2026-01-13T10:35:00Z"
}
```

---

### 5. **AiKnowledgeChunk** - Bảng Lưu Trữ Kiến Thức (Knowledge Base)

```sql
CREATE TABLE ai_knowledge_chunks (
    id UUID PRIMARY KEY,
    source VARCHAR(50) NOT NULL, -- PROJECT, TEAM, TASK, FAQ, DOCUMENT
    source_id VARCHAR(100),
    title VARCHAR(255),
    content TEXT NOT NULL,
    language VARCHAR(10) DEFAULT 'en',
    embedding DOUBLE PRECISION[] NOT NULL, -- Vector embeddings
    chunk_index INTEGER DEFAULT 0,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    delete_flag BOOLEAN
);
```

**Mục đích:**

- Lưu trữ tất cả kiến thức mà chatbot có thể sử dụng
- Lưu embeddings (vector representation) của nội dung
- Cho phép tìm kiếm ngữ nghĩa (semantic search) qua cosine similarity

**Nguồn Dữ Liệu (Sources):**

- `PROJECT` - Thông tin dự án từ bảng Projects
- `TEAM` - Thông tin team từ bảng Teams
- `TASK` - Thông tin task từ bảng DailyTasks
- `FAQ` - FAQ entries từ bảng FaqEntries
- `DOCUMENT` - Tài liệu nội bộ, nội quy công ty

**Ví dụ dữ liệu:**

```json
{
  "id": "523e4567-e89b-12d3-a456-426614174000",
  "source": "PROJECT",
  "source_id": "proj-001",
  "title": "Project: E-commerce Platform Development",
  "content": "Project Name: E-commerce Platform. Description: Complete e-commerce solution with payment gateway...",
  "language": "en",
  "embedding": [0.123, 0.456, 0.789, ...], // 1536 dimensions
  "chunk_index": 0,
  "metadata": {
    "category": "web-development",
    "tech_stack": ["React", "Node.js", "PostgreSQL"]
  }
}
```

---

### 6. **FaqEntry** - Bảng Câu Hỏi Thường Gặp

```sql
CREATE TABLE faq_entries (
    id UUID PRIMARY KEY,
    question TEXT NOT NULL,
    answer TEXT NOT NULL,
    category VARCHAR(50) DEFAULT 'GENERAL',
    language VARCHAR(10) DEFAULT 'vi',
    embedding DOUBLE PRECISION[], -- Vector embeddings
    view_count INTEGER DEFAULT 0,
    helpful_count INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE,
    delete_flag BOOLEAN,
    INDEX idx_faq_entries_category (category),
    INDEX idx_faq_entries_language (language)
);
```

**Mục đích:**

- Lưu các FAQ được đặt nhiều
- Cải thiện tìm kiếm qua embeddings
- Theo dõi FAQs được xem và hữu ích nhất

**Các Danh Mục FAQ:**

- `PRICING` - Về giá cả
- `TECHNOLOGY` - Về công nghệ
- `TIMELINE` - Về thời gian
- `PROCESS` - Về quy trình
- `GENERAL` - Thông tin chung

---

## Các Thành Phần Chính

### 1. **ChatController** - Điểm Vào API

```java
// Endpoint: POST /api/v1/chat/ask
// Người dùng gửi tin nhắn, nhận response từ AI
request: {
    conversationId: UUID (tùy chọn),
    message: "Tôi cần xây dựng...",
    locale: "vi"
}

response: {
    conversationId: UUID,
    reply: ChatMessageResponse,
    references: List<KnowledgeReferenceResponse>,
    provider: "gemini" | "openai",
    model: "gemini-1.0-pro"
}
```

**Các Endpoint Khác:**

- `GET /api/v1/chat/conversations` - Lấy danh sách cuộc hội thoại (Admin)
- `GET /api/v1/chat/conversations/{id}/messages` - Lấy tất cả tin nhắn trong cuộc hội thoại
- `POST /api/v1/chat/ingest` - Thêm kiến thức mới vào knowledge base (Admin)
- `POST /api/v1/chat/sync-domain` - Đồng bộ dữ liệu từ domain models (Admin)

---

### 2. **ChatServiceImpl** - Lõi Xử Lý Logic

#### Phương Thức Chính: `ask()`

```java
Flow:
1. Tìm hoặc tạo cuộc hội thoại (ChatConversation)
2. Lưu tin nhắn người dùng (ChatMessage)
3. Embedding: Chuyển đổi câu hỏi thành vector
4. Retrieve: Tìm kiếm kiến thức liên quan từ AiKnowledgeChunk
5. Rank: Sắp xếp kết quả theo cosine similarity (MIN_SCORE = 0.52)
6. Build context: Tạo prompt với kiến thức + lịch sử chat
7. Call AI: Gọi AiGatewayService để lấy response
8. Lưu response: Lưu tin nhắn từ AI vào ChatMessage
9. Return: Trả về response kèm references
```

**Các Tham Số Quan Trọng:**

```java
private static final int MAX_HISTORY = 8;      // Lưu 8 tin nhắn gần nhất
private static final int MAX_REFERENCES = 5;   // Trả về tối đa 5 tài liệu
private static final double MIN_SCORE = 0.52;  // Ngưỡng cosine similarity
```

#### Phương Thức Chính: `syncDomainData()`

```java
Flow:
1. Đồng bộ Company Info → Knowledge base
2. Đồng bộ Projects → Knowledge base (tất cả project active)
3. Đồng bộ Teams → Knowledge base (tất cả team active)
4. Đồng bộ Tasks → Knowledge base (50 task gần nhất)
5. Đồng bộ FAQs → Knowledge base + tính embedding nếu chưa có
6. Đồng bộ Technology Stacks → Knowledge base + tính embedding
```

**Cách Gọi:**

```
POST /api/v1/chat/sync-domain
Header: Accept-Language: vi
```

---

### 3. **AiGatewayService** - Kết Nối với AI Providers

#### Hỗ Trợ 2 AI Providers:

**A. Google Gemini (Mặc Định)**

```
- Model: gemini-1.0-pro
- Endpoint: https://generativelanguage.googleapis.com/v1/models/
- Embedding Model: text-embedding-004
- API Key: ai.gemini.api-key
```

**B. OpenAI**

```
- Model: gpt-4o-mini
- Endpoint: https://api.openai.com/v1/chat/completions
- Embedding Model: text-embedding-3-small
- API Key: ai.openai.api-key
```

#### Cấu Hình (application.properties):

```properties
# Chọn provider
ai.provider=gemini  # hoặc openai

# Gemini config
ai.gemini.api-key=your-gemini-key
ai.gemini.model=gemini-1.0-pro

# OpenAI config
ai.openai.api-key=your-openai-key
ai.openai.model=gpt-4o-mini

# Timeout
ai.timeout-ms=20000
```

#### Hai Phương Thức Chính:

**1. `embed(text, locale)` - Tạo Vector Embeddings**

```
Input: "Tôi muốn xây dựng website"
Processing:
  - Gửi text đến AI provider
  - Nhận vector có 1536 dimensions

Output: double[] {0.123, 0.456, ..., 0.789}
```

**2. `chat(systemPrompt, userMessage, contexts, history, locale)` - Gọi Chat API**

```
Input:
  - systemPrompt: "You are an AI consultant..."
  - userMessage: "Tôi cần tư vấn..."
  - contexts: ["Project: E-commerce...", "Tech: React..."]
  - history: ["user: ...", "assistant: ..."]
  - locale: "vi"

Output: "Dựa vào nhu cầu của bạn, tôi gợi ý..."
```

---

## Flow Hoạt Động của Chatbot

### 1. **Flow Khi Người Dùng Gửi Câu Hỏi**

```
┌──────────────────────────────────────┐
│ 1. Frontend gửi POST /api/v1/chat/ask │
│ {                                    │
│   conversationId: null/UUID,        │
│   message: "Tôi cần...",            │
│   locale: "vi"                      │
│ }                                   │
└──────────────────┬──────────────────┘
                   │
                   ↓
┌──────────────────────────────────────┐
│ 2. ChatController.ask()              │
│    - Xác thực request                │
│    - Gọi ChatServiceImpl.ask()        │
└──────────────────┬──────────────────┘
                   │
                   ↓
┌──────────────────────────────────────────┐
│ 3. ChatServiceImpl.ask()                  │
│ 3.1. Tìm hoặc tạo ChatConversation      │
│      - Nếu có conversationId: tìm       │
│      - Nếu không: tạo mới               │
│ 3.2. Lưu tin nhắn người dùng            │
│      INSERT INTO chat_messages          │
│      (role='user', content=...)         │
└──────────────────┬──────────────────────┘
                   │
                   ↓
┌──────────────────────────────────────────┐
│ 4. Embedding Query                       │
│    - Gọi aiGatewayService.embed()       │
│    - Chuyển câu hỏi thành vector        │
│    - "Tôi cần..." → [0.1, 0.2, ...]    │
└──────────────────┬──────────────────────┘
                   │
                   ↓
┌──────────────────────────────────────────┐
│ 5. Retrieve Knowledge                    │
│    - Lấy tất cả kiến thức từ            │
│      ai_knowledge_chunks table           │
│    - Filter theo language (vi/en)       │
└──────────────────┬──────────────────────┘
                   │
                   ↓
┌──────────────────────────────────────────┐
│ 6. Rank by Similarity (Cosine Distance) │
│    - Score = (query · chunk) / (||query|| × ||chunk||)
│    - Giữ lại những chunk có score >= 0.52
│    - Sắp xếp giảm dần, lấy top 5        │
└──────────────────┬──────────────────────┘
                   │
                   ↓
┌──────────────────────────────────────────┐
│ 7. Build Context & History               │
│    - Context: 5 tài liệu liên quan      │
│    - History: 8 tin nhắn gần nhất       │
│    - System Prompt: Role definition      │
└──────────────────┬──────────────────────┘
                   │
                   ↓
┌──────────────────────────────────────────┐
│ 8. Call AI Provider                      │
│    - Gọi aiGatewayService.chat()        │
│    - Gửi prompt tới Gemini/OpenAI       │
│    - Nhận response từ AI                 │
└──────────────────┬──────────────────────┘
                   │
                   ↓
┌──────────────────────────────────────────┐
│ 9. Lưu Response                          │
│    INSERT INTO chat_messages             │
│    (role='assistant', content=response)  │
│    UPDATE chat_conversations             │
│    SET last_message_at = now()          │
└──────────────────┬──────────────────────┘
                   │
                   ↓
┌──────────────────────────────────────────┐
│ 10. Return Response                      │
│ {                                        │
│   conversationId: UUID,                 │
│   reply: {id, role, content, time},    │
│   references: [{title, snippet, ...}],  │
│   provider: "gemini",                   │
│   model: "gemini-1.0-pro"               │
│ }                                        │
└──────────────────────────────────────────┘
                   │
                   ↓
┌──────────────────────────────────────────┐
│ Frontend nhận response và hiển thị       │
└──────────────────────────────────────────┘
```

---

### 2. **Flow Đồng Bộ Kiến Thức (syncDomainData)**

```
┌────────────────────────────────┐
│ 1. Admin gọi POST /sync-domain │
│    Header: Accept-Language: vi │
└────────────────┬───────────────┘
                 │
                 ↓
┌────────────────────────────────────────┐
│ 2. Đồng bộ Company Info                │
│    - Tạo chunk: "We provide..."        │
│    - Embedding: embed(content, "vi")   │
│    - INSERT INTO ai_knowledge_chunks   │
└────────────────┬───────────────────────┘
                 │
                 ↓
┌────────────────────────────────────────┐
│ 3. Đồng bộ Projects (from Projects)   │
│    FOR EACH project:                   │
│      - Build content: name, desc, ... │
│      - Embedding project content       │
│      - INSERT knowledge chunk          │
└────────────────┬───────────────────────┘
                 │
                 ↓
┌────────────────────────────────────────┐
│ 4. Đồng bộ Teams (from Teams)          │
│    FOR EACH team:                      │
│      - Build content: name, desc, ... │
│      - Embedding team content          │
│      - INSERT knowledge chunk          │
└────────────────┬───────────────────────┘
                 │
                 ↓
┌────────────────────────────────────────┐
│ 5. Đồng bộ Tasks (Limit 50 gần nhất)  │
│    FOR EACH task:                      │
│      - Build content: title, desc, ... │
│      - Embedding task content          │
│      - INSERT knowledge chunk          │
└────────────────┬───────────────────────┘
                 │
                 ↓
┌────────────────────────────────────────┐
│ 6. Đồng bộ FAQs (from FaqEntries)      │
│    FOR EACH faq:                       │
│      - Build content: Q + A            │
│      - IF NOT embedding: calculate     │
│      - UPDATE faq_entries.embedding    │
│      - INSERT knowledge chunk          │
└────────────────┬───────────────────────┘
                 │
                 ↓
┌────────────────────────────────────────┐
│ 7. Đồng bộ Tech Stacks                 │
│    FOR EACH tech:                      │
│      - Build content: name, desc, ... │
│      - IF NOT embedding: calculate     │
│      - UPDATE tech_stack.embedding     │
│      - INSERT knowledge chunk          │
└────────────────┬───────────────────────┘
                 │
                 ↓
┌────────────────────────────────────────┐
│ Return: List<KnowledgeReferenceResponse>
│ [{title, snippet, source, sourceId}]  │
└────────────────────────────────────────┘
```

---

## Cách Sử Dụng API

### 1. **Tạo Cuộc Hội Thoại Mới & Gửi Câu Hỏi**

**Endpoint:** `POST /api/v1/chat/ask`

**Request:**

```json
{
  "conversationId": null,
  "message": "Tôi muốn xây dựng một platform quản lý dự án. Budget khoảng 100 triệu đồng, timeline 6 tháng. Công nghệ nào bạn gợi ý?",
  "locale": "vi"
}
```

**Response:**

```json
{
  "conversationId": "123e4567-e89b-12d3-a456-426614174000",
  "reply": {
    "id": "223e4567-e89b-12d3-a456-426614174000",
    "role": "assistant",
    "content": "Dựa vào nhu cầu của bạn, tôi gợi ý:\n\n**Frontend**: React.js hoặc Vue.js\n- Dễ học, ecosystem phong phú\n- Phù hợp với timeline 6 tháng\n\n**Backend**: Node.js/Express hoặc Spring Boot\n- Node.js: nhanh, lightweight\n- Spring Boot: robust, enterprise-ready\n\n**Database**: PostgreSQL\n- Hỗ trợ complex queries\n- Giá rẻ\n\n**DevOps**: Docker, AWS/Heroku\n\nBudget ước tính:\n- Frontend: 20-30M\n- Backend: 30-40M\n- DevOps/Testing: 10-15M\n- Contingency: 20-30M",
    "createdAt": "2026-01-13T10:30:00Z"
  },
  "references": [
    {
      "title": "Tech: React",
      "snippet": "React.js là một JavaScript library...",
      "source": "DOCUMENT",
      "sourceId": "tech-001",
      "score": 0.87
    },
    {
      "title": "Tech: PostgreSQL",
      "snippet": "PostgreSQL là một database mã nguồn mở...",
      "source": "DOCUMENT",
      "sourceId": "tech-002",
      "score": 0.85
    },
    {
      "title": "Project: E-commerce Platform",
      "snippet": "Project Name: E-commerce Platform...",
      "source": "PROJECT",
      "sourceId": "proj-001",
      "score": 0.72
    }
  ],
  "provider": "gemini",
  "model": "gemini-1.0-pro"
}
```

---

### 2. **Tiếp Tục Cuộc Hội Thoại**

**Endpoint:** `POST /api/v1/chat/ask`

**Request:**

```json
{
  "conversationId": "123e4567-e89b-12d3-a456-426614174000",
  "message": "Có thể bắt đầu từ tháng 2 được không? Tôi cần backend có thể xử lý 10,000 requests/day",
  "locale": "vi"
}
```

**Response:**

```json
{
  "conversationId": "123e4567-e89b-12d3-a456-426614174000",
  "reply": {
    "id": "324e4567-e89b-12d3-a456-426614174000",
    "role": "assistant",
    "content": "Được, chúng ta có thể bắt đầu ngay tháng 2. Với 10,000 requests/day, đây là những lưu ý:\n\n1. **Backend Scaling**:\n   - Load Balancer (Nginx/HAProxy)\n   - Caching layer (Redis): cần thiết\n   - Database optimization: índexing, query optimization\n\n2. **Recommended Stack**:\n   - Node.js + Express (dễ scale horizontal)\n   - Hoặc Spring Boot với multi-threading\n   - Redis cache (tất cả)\n   - PostgreSQL + ReadReplicas\n\n3. **Infrastructure**:\n   - AWS EC2 (3-4 instances)\n   - CloudFront CDN\n   - Estimated cost: $2,000-3,000/month\n\n4. **Timeline**:\n   - Feb-Apr: Development\n   - May: Testing & Optimization\n   - June: Deployment & Monitoring",
    "createdAt": "2026-01-13T10:35:00Z"
  },
  "references": [
    {
      "title": "Tech: Redis",
      "snippet": "Redis là in-memory cache store...",
      "source": "DOCUMENT",
      "sourceId": "tech-005",
      "score": 0.89
    },
    {
      "title": "Tech: PostgreSQL",
      "snippet": "PostgreSQL: Cấu hình read replicas...",
      "source": "DOCUMENT",
      "sourceId": "tech-002",
      "score": 0.84
    }
  ],
  "provider": "gemini",
  "model": "gemini-1.0-pro"
}
```

---

### 3. **Lấy Lịch Sử Tin Nhắn**

**Endpoint:** `GET /api/v1/chat/conversations/{conversationId}/messages`

**Response:**

```json
[
  {
    "id": "223e4567-e89b-12d3-a456-426614174000",
    "role": "user",
    "content": "Tôi muốn xây dựng một platform...",
    "createdAt": "2026-01-13T10:30:00Z"
  },
  {
    "id": "324e4567-e89b-12d3-a456-426614174000",
    "role": "assistant",
    "content": "Dựa vào nhu cầu của bạn...",
    "createdAt": "2026-01-13T10:30:05Z"
  },
  {
    "id": "424e4567-e89b-12d3-a456-426614174000",
    "role": "user",
    "content": "Có thể bắt đầu từ tháng 2...",
    "createdAt": "2026-01-13T10:35:00Z"
  }
]
```

---

### 4. **Lấy Danh Sách Cuộc Hội Thoại (Admin)**

**Endpoint:** `GET /api/v1/chat/conversations?page=0&size=20`

**Yêu cầu:** `Authorization: Bearer <token>` với role ADMIN, PM, hoặc STAFF

**Response:**

```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "title": "Tư vấn dự án website thương mại điện tử",
    "locale": "vi",
    "lastMessageAt": "2026-01-13T10:35:00Z",
    "messageCount": 3
  },
  {
    "id": "223e4567-e89b-12d3-a456-426614174000",
    "title": "Hỏi về giá cả dịch vụ outsourcing",
    "locale": "vi",
    "lastMessageAt": "2026-01-13T09:50:00Z",
    "messageCount": 5
  }
]
```

---

### 5. **Thêm Kiến Thức Mới (Admin)**

**Endpoint:** `POST /api/v1/chat/ingest`

**Request:**

```json
{
  "title": "Hướng Dẫn CI/CD Pipeline",
  "content": "CI/CD là quá trình tự động hóa: 1. Code commit trigger Jenkins/GitHub Actions. 2. Run tests...",
  "language": "vi",
  "source": "DOCUMENT",
  "sourceId": "doc-ci-cd-001"
}
```

**Response:**

```json
{
  "title": "Hướng Dẫn CI/CD Pipeline",
  "snippet": "CI/CD là quá trình tự động hóa: 1. Code commit trigger...",
  "source": "DOCUMENT",
  "sourceId": "doc-ci-cd-001",
  "score": 1.0
}
```

---

### 6. **Đồng Bộ Dữ Liệu Domain (Admin)**

**Endpoint:** `POST /api/v1/chat/sync-domain`

**Header:** `Accept-Language: vi`

**Response:**

```json
[
  {
    "title": "Company Services",
    "snippet": "We provide software outsourcing, project management...",
    "source": "DOCUMENT",
    "sourceId": null,
    "score": 1.0
  },
  {
    "title": "Project: E-commerce Platform",
    "snippet": "Project Name: E-commerce Platform. Description: Complete e-commerce...",
    "source": "PROJECT",
    "sourceId": "proj-001",
    "score": 1.0
  },
  ...
]
```

---

## Dữ Liệu Được Lưu

### 1. **Trong Cuộc Hội Thoại**

```
ChatConversation:
├─ conversationId (UUID)
├─ userId (UUID, optional)
├─ locale (vi/en/ja)
├─ title (tự động tạo từ câu đầu, max 60 chars)
└─ metadata (flexible JSON)

ChatMessage:
├─ id (UUID)
├─ conversationId (FK)
├─ role (user|assistant)
├─ content (TEXT)
├─ tokenCount (integer)
└─ createdAt
```

### 2. **Kiến Thức (Knowledge Base)**

```
AiKnowledgeChunk:
├─ id (UUID)
├─ source (PROJECT|TEAM|TASK|FAQ|DOCUMENT)
├─ sourceId (reference ID)
├─ title
├─ content (TEXT)
├─ language (vi/en/ja)
├─ embedding (Double[] - 1536 dimensions)
├─ chunkIndex
└─ metadata (JSONB)
```

### 3. **Phân Tích & Feedback**

```
ChatIntent:
├─ conversationId
├─ detectedIntent
├─ confidenceScore
└─ extractedEntities (JSONB)

ChatFeedback:
├─ conversationId
├─ messageId
├─ userId
├─ rating (1-5)
├─ isHelpful (boolean)
├─ feedbackText
└─ issueType
```

---

## Những Cải Thiện Cần Thực Hiện

### 🔴 **Vấn Đề 1: Không Có Intent Detection Được Lưu**

**Hiện Tại:**

- `ChatIntent` table tạo nhưng **không được sử dụng** trong `ChatServiceImpl.ask()`
- Hệ thống không phát hiện ý định người dùng

**Cải Thiện:**

```java
// Thêm vào ChatServiceImpl.ask()
String detectedIntent = aiGatewayService.detectIntent(request.getMessage(), locale);
String extractedEntities = extractEntities(request.getMessage(), detectedIntent);

ChatIntent intent = new ChatIntent();
intent.setConversationId(conversation.getId());
intent.setDetectedIntent(detectedIntent);
intent.setConfidenceScore(0.85); // Calculate từ AI
intent.setExtractedEntities(extractedEntities);
chatIntentRepository.save(intent);

// Dùng detected intent để filter knowledge base
List<AiKnowledgeChunk> candidates = getKnowledgeByIntent(locale, detectedIntent);
```

---

### 🔴 **Vấn Đề 2: Không Collect Feedback từ Người Dùng**

**Hiện Tại:**

- `ChatFeedback` table tạo nhưng **không có endpoint** để người dùng feedback

**Cải Thiện:**

```java
// Thêm endpoint mới
@PostMapping("/feedback")
public ResponseEntity<Void> submitFeedback(
    @Valid @RequestBody ChatFeedbackRequest request) {
    chatService.saveFeedback(request);
    return ResponseEntity.ok().build();
}

// ChatFeedbackRequest
{
    conversationId: UUID,
    messageId: UUID,
    rating: 1-5,
    isHelpful: boolean,
    feedbackText: String,
    issueType: String
}
```

---

### 🟡 **Vấn Đề 3: Min Score Threshold Cần Tuning**

**Hiện Tại:**

```java
private static final double MIN_SCORE = 0.52;
```

**Vấn Đề:**

- 0.52 là quá thấp, có thể trả về kết quả không liên quan
- Không configurable, khó adjust theo feedback

**Cải Thiện:**

```properties
# application.properties
chatbot.similarity-threshold=0.65  # Tăng lên 0.65
chatbot.max-history=10             # Tăng lên 10
chatbot.max-references=7           # Tăng lên 7
```

```java
@Value("${chatbot.similarity-threshold:0.65}")
private double minScore;
```

---

### 🟡 **Vấn Đề 4: Không Có Rate Limiting**

**Hiện Tại:**

- User có thể spam tin nhắn không giới hạn
- Gây lãng phí API key (Gemini/OpenAI)

**Cải Thiện:**

```java
@PostMapping("/ask")
@RateLimiter(name = "chatAsk", fallbackMethod = "rateLimitFallback")
public ResponseEntity<ChatAskResponse> ask(@Valid @RequestBody ChatAskRequest request,
        @RequestHeader(name = "Accept-Language", required = false) Locale locale) {
    // Implementation
}

// application.properties
resilience4j.ratelimiter.instances.chatAsk.registerHealthIndicator=true
resilience4j.ratelimiter.instances.chatAsk.limitRefreshPeriod=1m
resilience4j.ratelimiter.instances.chatAsk.limitForPeriod=20  # 20 requests per minute
```

---

### 🟡 **Vấn Đề 5: Không Có Caching cho Knowledge Base**

**Hiện Tại:**

- Mỗi request phải query tất cả knowledge chunks từ database
- Gây chậm nếu có nhiều chunks

**Cải Thiện:**

```java
@Cacheable(value = "knowledgeCache", key = "#locale")
private List<AiKnowledgeChunk> getKnowledge(String locale) {
    List<AiKnowledgeChunk> localized = knowledgeRepository.findRecentByLanguage(locale);
    if (localized != null && !localized.isEmpty())
        return localized;
    return knowledgeRepository.findRecentByLanguage("en");
}

// Clear cache khi sync
@CacheEvict(value = "knowledgeCache", allEntries = true)
@Transactional
public List<KnowledgeReferenceResponse> syncDomainData(Locale locale) {
    // Implementation
}
```

---

### 🟡 **Vấn Đề 6: Embeddings Calculation Chậm**

**Hiện Tại:**

- `syncDomainData()` phải tính embedding cho tất cả chunks
- Nếu có 1000 chunks = 1000 API calls (rất chậm)

**Cải Thiện:**

```java
// Batch embedding API nếu provider hỗ trợ
@Transactional
public List<KnowledgeReferenceResponse> syncDomainData(Locale locale) {
    List<String> contentsToEmbed = new ArrayList<>();
    List<AiKnowledgeChunk> chunksToSave = new ArrayList<>();

    // Collect all contents
    // ...

    // Batch embedding call (nếu provider hỗ trợ)
    List<double[]> embeddings = aiGatewayService.embedBatch(contentsToEmbed, locale);

    // Save với embeddings
    for (int i = 0; i < chunksToSave.size(); i++) {
        chunksToSave.get(i).setEmbedding(toObjectArray(embeddings.get(i)));
    }
    knowledgeRepository.saveAll(chunksToSave);
}
```

---

### 🟡 **Vấn Đề 7: Không Có Logging Chi Tiết**

**Hiện Tại:**

- Khó debug nếu AI trả về kết quả sai
- Không track tỷ lệ thành công/thất bại

**Cải Thiện:**

```java
private static final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);

@Override
@Transactional
public ChatAskResponse ask(ChatAskRequest request, Locale locale, UUID userId) {
    logger.info("Chat request: conversationId={}, locale={}, message={}",
        request.getConversationId(), locale, request.getMessage());

    try {
        // ... implementation

        logger.info("Chat response: conversationId={}, referencesCount={}, provider={}",
            conversation.getId(), relevant.size(), aiGatewayService.getActiveProvider());

        return response;
    } catch (Exception e) {
        logger.error("Chat error: conversationId={}",
            request.getConversationId(), e);
        throw e;
    }
}
```

---

### 🟡 **Vấn Đề 8: Không Có Version Control cho Knowledge Base**

**Hiện Tại:**

- Nếu update kiến thức sai, không thể rollback
- Không biết ai update và khi nào

**Cải Thiện:**

```java
@Entity
@Table(name = "ai_knowledge_chunks_audit")
public class AiKnowledgeChunkAudit {
    private UUID id;
    private UUID chunkId; // FK to ai_knowledge_chunks
    private String oldContent;
    private String newContent;
    private UUID updatedBy;
    private OffsetDateTime updatedAt;
    private String action; // CREATE, UPDATE, DELETE
}

// Use @Audited from Envers
@Entity
@Audited
public class AiKnowledgeChunk extends BaseEntity {
    // ...
}
```

---

### 🟢 **Vấn Đề 9: Frontend Integration Issues**

**Hiện Tại:**

- Frontend có `apiChat.js` nhưng có thể không implement tất cả features

**Cần Kiểm Tra:**

```javascript
// FE/src/services/apiChat.js
- POST /api/v1/chat/ask (✓ có)
- GET /api/v1/chat/conversations (? kiểm tra)
- GET /api/v1/chat/conversations/{id}/messages (? kiểm tra)
- POST /api/v1/chat/ingest (? kiểm tra)
- POST /api/v1/chat/sync-domain (? kiểm tra)
- POST /api/v1/chat/feedback (✗ chưa có)
```

---

### 🟢 **Vấn Đề 10: Test Coverage**

**Hiện Tại:**

- Có thể chưa có test cases cho ChatService

**Cải Thiện:**

```java
@SpringBootTest
public class ChatServiceImplTest {

    @Test
    public void testAskWithNewConversation() {
        // Verify conversation created
        // Verify user message saved
        // Verify AI response saved
        // Verify references returned
    }

    @Test
    public void testAskWithExistingConversation() {
        // Verify history loaded correctly
        // Verify context limited to MAX_HISTORY
    }

    @Test
    public void testSyncDomainData() {
        // Verify all projects synced
        // Verify all teams synced
        // Verify all tasks synced
        // Verify all FAQs synced
        // Verify embeddings calculated
    }
}
```

---

## Tóm Tắt

### Kiến Trúc Tổng Quát:

```
┌─────────────────────────────┐
│  Frontend (Vue.js)          │
└──────────────┬──────────────┘
               │
      ┌────────▼────────┐
      │ ChatController  │
      └────────┬────────┘
               │
      ┌────────▼────────────────────┐
      │ ChatServiceImpl              │
      │ ├─ ask()                    │
      │ ├─ getMessages()            │
      │ ├─ ingest()                 │
      │ └─ syncDomainData()        │
      └────────┬────────────────────┘
               │
      ┌────────┴────────────┐
      │                     │
  ┌───▼────┐          ┌────▼──────────┐
  │Database │          │AiGatewayService
  │(4 tables)          │├─ Gemini API
  │├─ chat_conv        │├─ OpenAI API
  │├─ chat_msg         │├─ embed()
  │├─ chat_intent      │└─ chat()
  │└─ chat_feedback    │
  │                    │
  │AiKnowledgeChunk    │
  │├─ Project          │
  │├─ Team             │
  │├─ Task             │
  │├─ FAQ              │
  │└─ Document         │
  └────────────────────┘
```

### Dữ Liệu Lưu:

- ✓ Cuộc hội thoại (conversations)
- ✓ Tin nhắn (messages)
- ✓ Kiến thức (knowledge chunks + embeddings)
- ✓ Feedback (ratings, issues)
- ⚠ Intent detection (bảng tồn tại nhưng không dùng)

### Khuyến Nghị Ưu Tiên:

1. **Cao (Critical):**

   - Implement intent detection
   - Add feedback endpoint
   - Add rate limiting
   - Add logging

2. **Trung (Important):**

   - Tuning MIN_SCORE threshold
   - Add caching
   - Improve embedding performance
   - Add test cases

3. **Thấp (Nice-to-have):**
   - Audit trail for knowledge
   - Advanced analytics
   - Multi-language improvements
