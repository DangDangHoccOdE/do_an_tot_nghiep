# Hướng Dẫn Chi Tiết: Hệ Thống Chatbot AI - Luồng Hoạt Động & Kiến Trúc

**Tác giả**: Development Team  
**Ngày viết**: January 14, 2026  
**Mục tiêu**: Cung cấp hướng dẫn chi tiết cho người mới hiểu rõ logic hoạt động của chatbot để có thể áp dụng cho các dự án khác.

---

## Mục Lục

1. [Kiến Trúc Tổng Quan](#kiến-trúc-tổng-quan)
2. [Các Entity & Bảng Dữ Liệu](#các-entity--bảng-dữ-liệu)
3. [Request/Response Models](#requestresponse-models)
4. [Luồng Hoạt Động Chi Tiết](#luồng-hoạt-động-chi-tiết)
5. [Các Service Chính](#các-service-chính)
6. [Cách Thêm Dữ Liệu Mới Cho Chatbot](#cách-thêm-dữ-liệu-mới-cho-chatbot)
7. [Xử Lý AI & Embedding](#xử-lý-ai--embedding)
8. [Áp Dụng Cho Dự Án Khác](#áp-dụng-cho-dự-án-khác)

---

## Kiến Trúc Tổng Quan

```
┌─────────────────────────────────────────────────────────────────┐
│                      FRONTEND (Vue.js)                          │
│              ChatWidget Component (Chat Interface)              │
└──────────────────────────┬──────────────────────────────────────┘
                           │ HTTP Request/Response
                           ↓
┌──────────────────────────────────────────────────────────────────┐
│                    API LAYER (REST Controller)                   │
│               POST /api/v1/chat/ask                              │
│               GET /api/v1/chat/conversations/{id}                │
│               POST /api/v1/chat/feedback                         │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ↓
┌──────────────────────────────────────────────────────────────────┐
│                 SERVICE LAYER (Business Logic)                   │
│                                                                  │
│  ┌────────────────────────────────────────────────────────┐     │
│  │ ChatService (Main Orchestrator)                        │     │
│  │ - ask()                (Call AI model)                 │     │
│  │ - ingest()             (Add knowledge)                 │     │
│  │ - syncDomainData()     (Sync knowledge from DB)        │     │
│  │ - saveFeedback()       (Save user feedback)            │     │
│  └────────────────────────────────────────────────────────┘     │
│                           ↓                                      │
│  ┌────────────────────────────────────────────────────────┐     │
│  │ AiGatewayService (AI Provider Integration)            │     │
│  │ - embed(text)          (Create embeddings)            │     │
│  │ - chat(system, user)   (Call Gemini/OpenAI)          │     │
│  │ Support: Gemini, OpenAI                               │     │
│  └────────────────────────────────────────────────────────┘     │
│                           ↓                                      │
│  ┌────────────────────────────────────────────────────────┐     │
│  │ IIntentDetectionService                               │     │
│  │ - detectIntent()       (Detect user intent)           │     │
│  │ - extractEntities()    (Extract key info)             │     │
│  └────────────────────────────────────────────────────────┘     │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ↓
┌──────────────────────────────────────────────────────────────────┐
│              DATA ACCESS LAYER (Repositories)                    │
│                                                                  │
│  ChatConversationRepository  - Lưu trữ conversation/phiên chat   │
│  ChatMessageRepository       - Lưu trữ tin nhắn user/bot        │
│  ChatIntentRepository        - Lưu trữ intent detected          │
│  ChatFeedbackRepository      - Lưu trữ feedback người dùng      │
│  AiKnowledgeChunkRepository  - Lưu trữ knowledge base           │
│  ProjectRepository           - Dữ liệu dự án                    │
│  TeamRepository              - Dữ liệu team                     │
│  DailyTaskRepository         - Dữ liệu task hàng ngày           │
│  FaqEntryRepository          - Câu hỏi thường gặp              │
│  TechnologyStackRepository   - Công nghệ & stack khác           │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ↓
┌──────────────────────────────────────────────────────────────────┐
│                    DATABASE (PostgreSQL)                         │
│                                                                  │
│  Tables:                                                         │
│  - chat_conversations        (Lưu phiên chat)                   │
│  - chat_messages             (Lưu tin nhắn)                     │
│  - chat_intents              (Lưu intent)                       │
│  - chat_feedback             (Lưu feedback)                     │
│  - ai_knowledge_chunks       (Vector database)                  │
│  - projects, teams, tasks... (Domain data)                      │
└──────────────────────────────────────────────────────────────────┘
                           │
                           ↓
┌──────────────────────────────────────────────────────────────────┐
│            EXTERNAL AI SERVICES (Cloud APIs)                     │
│                                                                  │
│  Google Gemini API      (Mặc định)                              │
│  OpenAI API             (Thay thế)                              │
│  - Chat: gpt-4o-mini (OpenAI), gemini-2.5-flash (Gemini)      │
│  - Embedding: Được cung cấp bởi AI provider                    │
└──────────────────────────────────────────────────────────────────┘
```

---

## Các Entity & Bảng Dữ Liệu

### 1. ChatConversation (Phiên Chat)

**Bảng**: `chat_conversations`

```java
@Entity
@Table(name = "chat_conversations")
public class ChatConversation extends BaseEntity {
    private UUID userId;           // User ID (who started conversation)
    private String locale = "en";  // Language: en, vi, ja...
    private String title;          // Conversation title
    private OffsetDateTime lastMessageAt;  // Lần chat cuối
    private String metadata;       // Additional data (JSONB)
}
```

**Mục đích**: Lưu trữ thông tin về mỗi phiên chat (conversation)  
**Quan hệ**: 1 conversation → N messages

---

### 2. ChatMessage (Tin Nhắn)

**Bảng**: `chat_messages`

```java
@Entity
@Table(name = "chat_messages")
public class ChatMessage extends BaseEntity {
    private UUID conversationId;   // FK → ChatConversation
    private ChatMessageRole role;  // USER hoặc ASSISTANT
    private String content;        // Nội dung tin nhắn
    private Integer tokenCount;    // Số token dùng (cho OpenAI)
    private String metadata;       // JSONB
}
```

**Enum**: `ChatMessageRole`

- `USER` - Tin nhắn từ người dùng
- `ASSISTANT` - Tin nhắn từ bot/AI

---

### 3. ChatIntent (Ý Định Người Dùng)

**Bảng**: `chat_intents`

```java
@Entity
@Table(name = "chat_intents")
public class ChatIntent {
    private UUID conversationId;   // FK → ChatConversation
    private String detectedIntent; // PRICING_INQUIRY, TECH_RECOMMENDATION, PROJECT_TIMELINE...
    private Double confidenceScore;  // 0.0-1.0 (độ tin cậy)
    private String extractedEntities; // JSONB {"budget": "50M", "timeline": "3 months"}
    private OffsetDateTime createdAt;
}
```

**Các Intent Type** (được phát hiện tự động):

- `PRICING_INQUIRY` - Hỏi về giá/chi phí
- `TECH_RECOMMENDATION` - Hỏi về công nghệ
- `PROJECT_TIMELINE` - Hỏi về thời gian
- `FEATURE_REQUEST` - Yêu cầu tính năng
- `GENERAL_INFO` - Thông tin chung

---

### 4. ChatFeedback (Phản Hồi Người Dùng)

**Bảng**: `chat_feedback`

```java
@Entity
@Table(name = "chat_feedback")
public class ChatFeedback {
    private UUID conversationId;   // FK
    private UUID messageId;        // Feedback cho message nào
    private UUID userId;           // Ai feedback
    private Integer rating;        // 1-5 sao
    private Boolean isHelpful;     // true/false/null
    private String feedbackText;   // Nội dung feedback
    private IssueType issueType;   // INACCURATE, IRRELEVANT, INCOMPLETE, RUDE, OTHER
    private OffsetDateTime createdAt;
}
```

**Enum**: `IssueType`

- `INACCURATE` - Không chính xác
- `IRRELEVANT` - Không liên quan
- `INCOMPLETE` - Không đầy đủ
- `RUDE` - Thô lỗ
- `OTHER` - Khác

---

### 5. AiKnowledgeChunk (Cơ Sở Kiến Thức)

**Bảng**: `ai_knowledge_chunks` (Vector Database)

```java
@Entity
@Table(name = "ai_knowledge_chunks")
public class AiKnowledgeChunk extends BaseEntity {
    private String source;       // PROJECT, FAQ, TEAM, TASK, DOCUMENT, TECH
    private String sourceId;     // ID của source (project ID, task ID...)
    private String title;        // Tiêu đề/tên
    private String content;      // Nội dung (dùng để tạo embedding)
    private String language;     // en, vi, ja...
    private Double[] embedding;  // Vector (1536 chiều cho OpenAI, 768 cho Gemini)
    private Integer chunkIndex;  // Chỉ số chunk trong document
    private String metadata;     // JSONB
}
```

**Mục đích**: Lưu trữ vector embeddings của tất cả kiến thức  
**Cách sử dụng**: Khi user hỏi, tính embedding của câu hỏi → compare với DB → tìm top K chunks tương tự

---

## Request/Response Models

### ChatAskRequest (Input khi user hỏi)

```java
public class ChatAskRequest {
    private UUID conversationId;  // null nếu conversation mới
    @NotBlank
    private String message;       // Câu hỏi của user
    private String locale;        // "vi", "en", "ja"
}
```

### ChatAskResponse (Output khi bot trả lời)

```java
public class ChatAskResponse {
    private UUID conversationId;  // ID của conversation
    private ChatMessageResponse reply;  // Trả lời từ bot
    private List<KnowledgeReferenceResponse> references;  // Tài liệu tham khảo
    private String provider;      // "gemini" hoặc "openai"
    private String model;         // "gemini-2.5-flash" hoặc "gpt-4o-mini"
}
```

### KnowledgeReferenceResponse (Tài liệu tham khảo)

```java
public class KnowledgeReferenceResponse {
    private String title;         // Tiêu đề tài liệu
    private String snippet;       // Đoạn trích (220 ký tự)
    private String source;        // PROJECT, FAQ, TEAM, TASK...
    private String sourceId;      // ID của source
    private Double score;         // Độ tương tự (0.0-1.0)
}
```

### ChatIntentResponse (Intent của user)

```java
public class ChatIntentResponse {
    private UUID id;
    private UUID conversationId;
    private String detectedIntent;     // PRICING_INQUIRY...
    private Double confidenceScore;    // 0.0-1.0
    private Map<String, Object> extractedEntities;  // {budget, timeline, type...}
    private OffsetDateTime createdAt;
}
```

---

## Luồng Hoạt Động Chi Tiết

### 🔄 Luồng Chính: User Hỏi ChatBot

```
┌─────────────────────────────────────────────────────────────────┐
│ STEP 1: User gửi câu hỏi từ Frontend                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Frontend (ChatWidget.vue)                                      │
│  ├─ User input: "Giá dịch vụ app mobile là bao nhiêu?"        │
│  ├─ Gửi POST /api/v1/chat/ask                                 │
│  └─ Payload:                                                    │
│     {                                                           │
│       "conversationId": "existing-uuid" (or null),             │
│       "message": "Giá dịch vụ app mobile là bao nhiêu?",      │
│       "locale": "vi"                                           │
│     }                                                           │
└─────────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────────┐
│ STEP 2: Controller nhận request & gọi Service                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ChatController.ask(request, locale)                            │
│  ├─ Validate input: @Valid ChatAskRequest                      │
│  ├─ Extract user from Authentication (nếu có)                 │
│  └─ Gọi: chatService.ask(request, locale, userId)            │
└─────────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────────┐
│ STEP 3: Service - Tìm/Tạo Conversation                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ChatServiceImpl.ask()                                           │
│  ├─ Resolve locale: "vi" (từ request) hoặc "en" (mặc định)    │
│  ├─ Gọi findOrCreateConversation():                            │
│  │  ├─ Nếu conversationId có: Tìm từ DB                       │
│  │  └─ Nếu null: Tạo mới ChatConversation                     │
│  └─ Lấy được: conversation UUID                                │
└─────────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────────┐
│ STEP 4: Lưu tin nhắn từ User                                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Tạo ChatMessage entity:                                        │
│  ├─ role = ChatMessageRole.USER                               │
│  ├─ content = "Giá dịch vụ app mobile là bao nhiêu?"         │
│  ├─ conversationId = (conversation UUID)                       │
│  └─ Lưu vào DB                                                 │
└─────────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────────┐
│ STEP 5: Phát Hiện Intent & Entities                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  IIntentDetectionService:                                       │
│  ├─ detectIntent("Giá dịch vụ app mobile là bao nhiêu?")     │
│  │  → "PRICING_INQUIRY"                                       │
│  ├─ extractEntities(message, intent)                           │
│  │  → {"projectType": "mobile app", "type": "app"}           │
│  └─ calculateConfidence(message, intent)                       │
│     → 0.85 (độ tin cậy 85%)                                    │
│                                                                 │
│  Tạo ChatIntent entity & lưu vào DB                            │
└─────────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────────┐
│ STEP 6: Tạo Embedding & Tìm Kiến Thức                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  AiGatewayService.embed(message, locale):                      │
│  ├─ Input: "Giá dịch vụ app mobile là bao nhiêu?"            │
│  ├─ Call AI provider (Gemini/OpenAI)                          │
│  └─ Output: double[] embedding (768 hoặc 1536 chiều)         │
│                                                                 │
│  rankBySimilarity(queryEmbedding, candidates):                │
│  ├─ Lấy tất cả AiKnowledgeChunk từ DB                        │
│  ├─ So sánh embedding bằng cosine similarity:                │
│  │  similarity = (A · B) / (||A|| * ||B||)                    │
│  ├─ Sắp xếp theo điểm từ cao → thấp                          │
│  └─ Lọc: chỉ lấy chunks có score >= MIN_SCORE (0.52)        │
│                                                                 │
│  Kết quả: List<ScoredChunk> relevant (top 5)                  │
│  ├─ Ví dụ:                                                     │
│  │  1. "FAQ: Giá app web" (score: 0.87)                       │
│  │  2. "Project: CRM mobile" (score: 0.82)                    │
│  │  3. "Pricing guide" (score: 0.75)                          │
│  │  ...                                                        │
└─────────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────────┐
│ STEP 7: Lấy Chat History & Build Context                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  latestMessages(conversationId, MAX_HISTORY):                  │
│  ├─ MAX_HISTORY = 8 (lấy 8 tin nhắn gần nhất)                 │
│  └─ Lấy từ DB: [user1, bot1, user2, bot2, ...]              │
│                                                                 │
│  Build contexts String:                                        │
│  ├─ Từ knowledge chunks:                                       │
│  │  "Context:\n- FAQ: Giá app web\n- Project: CRM mobile..." │
│  └─ Từ history:                                                │
│  │  "Chat history:\nuser: ...previous message...\nassistant..." │
└─────────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────────┐
│ STEP 8: Gọi AI Model để Tạo Response                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  buildSystemPrompt(locale):                                    │
│  ├─ Tạo system prompt theo ngôn ngữ (vi/en/ja)               │
│  └─ Ví dụ: "Bạn là trợ lý AI cho công ty Luvina..."         │
│                                                                 │
│  AiGatewayService.chat(systemPrompt, userMessage, contexts, history) │
│  ├─ Nếu provider = "gemini":                                  │
│  │  └─ Gọi Google Gemini API (gemini-2.5-flash)             │
│  └─ Nếu provider = "openai":                                  │
│     └─ Gọi OpenAI API (gpt-4o-mini)                           │
│                                                                 │
│  Prompt gửi đi:                                                │
│  ┌──────────────────────────────────────────────┐             │
│  │ System: "Bạn là trợ lý AI..."               │             │
│  │ Context:                                      │             │
│  │ - FAQ: Giá app web: 30-50M                   │             │
│  │ - Project: CRM: giá 50-100M                  │             │
│  │                                               │             │
│  │ Chat history: (last 8 messages)              │             │
│  │ User (vi): Giá dịch vụ app mobile?           │             │
│  └──────────────────────────────────────────────┘             │
│                                                                 │
│  Response từ AI:                                               │
│  "Giá dịch vụ app mobile thường từ 30-80 triệu..."           │
└─────────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────────┐
│ STEP 9: Lưu Response từ Bot                                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Tạo ChatMessage entity:                                        │
│  ├─ role = ChatMessageRole.ASSISTANT                          │
│  ├─ content = "Giá dịch vụ app mobile thường..."             │
│  ├─ conversationId = (conversation UUID)                       │
│  └─ Lưu vào DB                                                 │
│                                                                 │
│  Cập nhật Conversation:                                        │
│  ├─ lastMessageAt = now()                                      │
│  └─ Lưu vào DB                                                 │
└─────────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────────┐
│ STEP 10: Build & Return Response                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ChatAskResponse response:                                      │
│  ├─ conversationId = conversation.getId()                      │
│  ├─ reply = ChatMessageResponse {                              │
│  │    id, role="assistant", content, createdAt              │
│  │  }                                                          │
│  ├─ references = List<KnowledgeReferenceResponse> {           │
│  │    [                                                       │
│  │      {title: "FAQ: ...", snippet: "...", source: "FAQ", ...},  │
│  │      {title: "Project: ...", snippet: "...", source: "PROJECT", ...},  │
│  │      ...                                                    │
│  │    ]                                                       │
│  │  }                                                          │
│  ├─ provider = "gemini"                                        │
│  └─ model = "gemini-2.5-flash"                                │
│                                                                 │
│  Return ResponseEntity<ChatAskResponse>.ok(response)           │
└─────────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────────┐
│ STEP 11: Frontend nhận response & hiển thị                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ChatWidget.vue:                                               │
│  ├─ Nhận response từ BE                                        │
│  ├─ Hiển thị message bot                                      │
│  ├─ Hiển thị references (tài liệu tham khảo)                 │
│  └─ Hiển thị ChatFeedback component (để user feedback)       │
│                                                                 │
│  User có thể:                                                  │
│  ├─ Đánh giá sao (1-5)                                        │
│  ├─ Chọn helpful/not helpful                                  │
│  ├─ Chọn issue type (inaccurate, incomplete...)              │
│  └─ Viết feedback thêm                                        │
└─────────────────────────────────────────────────────────────────┘
```

---

## Các Service Chính

### 1. ChatServiceImpl (Dịch Vụ Chính)

**Vị trí**: `service/impl/ChatServiceImpl.java`

**Các phương thức chính**:

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements IChatService {

    // ========== Core Chat Methods ==========

    @Override
    @Transactional
    public ChatAskResponse ask(ChatAskRequest request, Locale locale, UUID userId) {
        // Luồng chính: xử lý câu hỏi từ user
        // 1. Tìm/tạo conversation
        // 2. Lưu message từ user
        // 3. Phát hiện intent
        // 4. Tạo embedding
        // 5. Tìm kiến thức tương tự
        // 6. Gọi AI model
        // 7. Lưu response
        // 8. Return ChatAskResponse
    }

    @Override
    public ChatConversationResponse getConversation(UUID id) {
        // Lấy thông tin chi tiết 1 conversation
        // Trả về: id, title, locale, lastMessageAt, messageCount
    }

    @Override
    public List<ChatMessageResponse> getMessages(UUID conversationId) {
        // Lấy tất cả messages trong 1 conversation
        // Trả về: danh sách messages với role, content, createdAt
    }

    @Override
    public List<ChatConversationResponse> getConversations(int page, int size) {
        // Lấy danh sách conversations (phân trang)
        // Trả về: các conversations gần đây
    }

    // ========== Knowledge Management Methods ==========

    @Override
    @Transactional
    public KnowledgeReferenceResponse ingest(ChatIngestRequest request) {
        // Thêm 1 knowledge chunk mới
        // 1. Tạo embedding từ content
        // 2. Lưu vào ai_knowledge_chunks table
        // 3. Return KnowledgeReferenceResponse
    }

    @Override
    @Transactional
    public List<KnowledgeReferenceResponse> syncDomainData(Locale locale) {
        // Đồng bộ tất cả dữ liệu từ các table khác:
        // - Projects (Dự án)
        // - Teams (Đội)
        // - DailyTasks (Công việc)
        // - FaqEntries (FAQ)
        // - TechnologyStacks (Công nghệ)
        // Tạo embeddings & lưu vào ai_knowledge_chunks
        // Xóa cache để load lại
    }

    // ========== Feedback Methods ==========

    @Override
    @Transactional
    public ChatFeedbackResponse saveFeedback(ChatFeedbackRequest request, UUID userId) {
        // Lưu feedback của user
        // Chuyển issueType từ String → Enum
    }

    @Override
    public ChatFeedbackStatisticsResponse getFeedbackStatistics() {
        // Thống kê feedback:
        // - Average rating
        // - Total feedbacks
        // - Helpful count
        // - Issue types distribution
    }

    @Override
    public List<ChatFeedbackResponse> getLowRatedFeedbacks() {
        // Lấy feedback có rating <= 2
        // Dùng để improve AI
    }

    // ========== Intent Methods ==========

    @Override
    public List<ChatIntentResponse> getConversationIntents(UUID conversationId) {
        // Lấy tất cả intent trong 1 conversation
        // Trả về: list ChatIntentResponse với extracted entities
    }

    // ========== Helper Methods ==========

    private ChatConversation findOrCreateConversation(...) {
        // Tìm conversation theo ID
        // Nếu không có: tạo mới
    }

    private List<ScoredChunk> rankBySimilarity(double[] query,
                                                List<AiKnowledgeChunk> candidates) {
        // So sánh embedding bằng cosine similarity
        // Sắp xếp từ cao → thấp
        // Trả về: List<ScoredChunk> (chunk + similarity score)
    }

    private double cosine(double[] a, double[] b) {
        // Tính cosine similarity giữa 2 vectors
        // Formula: (A · B) / (||A|| * ||B||)
    }

    private List<AiKnowledgeChunk> getKnowledge(String locale) {
        // Lấy tất cả knowledge chunks theo language
        // @Cacheable - lưu cache 30 phút
    }

    private List<ChatMessage> latestMessages(UUID conversationId, int limit) {
        // Lấy N messages gần nhất từ conversation
    }

    private String buildSystemPrompt(String locale) {
        // Tạo system prompt theo ngôn ngữ
        // Ví dụ: "Bạn là trợ lý AI cho công ty Luvina..."
    }
}
```

**Các Constants**:

```java
private static final int MAX_HISTORY = 8;       // Lưu tối đa 8 tin nhắn từ trước
private static final int MAX_REFERENCES = 5;   // Trả lại tối đa 5 tài liệu tham khảo
private static final double MIN_SCORE = 0.52;  // Ngưỡng tối thiểu similarity (52%)
```

---

### 2. AiGatewayService (Gateway tới AI Provider)

**Vị trí**: `service/impl/AiGatewayService.java`

**Mục đích**: Tích hợp với các AI provider (Gemini, OpenAI)

```java
@Service
public class AiGatewayService {

    // ========== Configuration ==========

    @Value("${ai.provider:gemini}")  // Mặc định: gemini
    private String provider;

    @Value("${ai.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${ai.gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    @Value("${ai.openai.api-key:}")
    private String openAiApiKey;

    @Value("${ai.openai.model:gpt-4o-mini}")
    private String openAiModel;

    @Value("${ai.timeout-ms:20000}")  // Timeout 20 seconds
    private long timeoutMs;

    // ========== Core Methods ==========

    public double[] embed(String text, String locale) {
        // Tạo embedding (vector) từ text
        // Input: text, locale
        // Output: double[] (768 chiều cho Gemini, 1536 cho OpenAI)
        // Logic:
        // if (provider == "openai") → return embedOpenAi(text)
        // else → return embedGemini(text, locale)
    }

    public String chat(String systemPrompt, String userMessage,
                       List<String> contexts, List<String> history,
                       String locale) {
        // Gọi AI model để tạo response
        // Input:
        //   - systemPrompt: "Bạn là trợ lý AI..."
        //   - userMessage: Câu hỏi của user
        //   - contexts: Danh sách context từ knowledge base
        //   - history: Chat history
        //   - locale: Ngôn ngữ
        // Output: String (response từ AI)
        // Logic:
        // if (provider == "openai") → return chatOpenAi(...)
        // else → return chatGemini(...)
    }

    // ========== Gemini Integration ==========

    private double[] embedGemini(String text, String locale) {
        // Gọi Gemini Embedding API
        // URL: https://generativelanguage.googleapis.com/v1/models/{model}:embedContent
        // Trả về: vector embedding
    }

    private String chatGemini(String systemPrompt, String userMessage,
                             List<String> contexts, List<String> history,
                             String locale) {
        // Gọi Gemini Chat API
        // URL: https://generativelanguage.googleapis.com/v1/models/{model}:generateContent
        // Build prompt: systemPrompt + contexts + history + userMessage
        // Config: temperature 0.4, topK 32, topP 0.9, maxOutputTokens 900
    }

    // ========== OpenAI Integration ==========

    private double[] embedOpenAi(String text) {
        // Gọi OpenAI Embedding API
        // Endpoint: POST https://api.openai.com/v1/embeddings
        // Model: text-embedding-3-small hoặc text-embedding-3-large
    }

    private String chatOpenAi(String systemPrompt, String userMessage,
                             List<String> contexts, List<String> history,
                             String locale) {
        // Gọi OpenAI Chat API
        // Endpoint: POST https://api.openai.com/v1/chat/completions
        // Messages:
        //   [{role: "system", content: systemPrompt},
        //    {role: "user", content: ...}]
        // Config: model gpt-4o-mini, temperature 0.4
    }

    // ========== Helper Methods ==========

    public void ensureConfigured() {
        // Kiểm tra API keys được cấu hình chưa
        // Throw ConfigurationException nếu chưa
    }

    public String getActiveProvider() {
        // Trả về provider đang active (gemini hoặc openai)
    }

    public String getModelName() {
        // Trả về tên model đang sử dụng
    }
}
```

**Cấu hình trong `application.properties`**:

```properties
# AI Provider Configuration
ai.provider=gemini                           # hoặc "openai"
ai.gemini.api-key=AIzaSy...                 # Gemini API Key
ai.gemini.model=gemini-2.5-flash           # Gemini model
ai.openai.api-key=${OPENAI_API_KEY:}       # OpenAI API Key
ai.openai.model=gpt-4o-mini                # OpenAI model
ai.timeout-ms=20000                        # Timeout 20 seconds
```

---

### 3. IIntentDetectionService (Phát Hiện Ý Định)

**Vị trí**: `service/inter/IIntentDetectionService.java`

```java
public interface IIntentDetectionService {

    String detectIntent(String message, String locale);
    // Input: user message, locale
    // Output: Intent (PRICING_INQUIRY, TECH_RECOMMENDATION, ...)

    Map<String, Object> extractEntities(String message, String detectedIntent);
    // Input: message, intent type
    // Output: Extracted entities {"budget": "50M", "timeline": "3 months", ...}

    Double calculateConfidence(String message, String detectedIntent);
    // Input: message, intent
    // Output: Confidence score (0.0-1.0)
}
```

**Mục đích**: Hiểu ý định thực sự của user, không chỉ tìm từ khóa.

---

## Cách Thêm Dữ Liệu Mới Cho Chatbot

### Phương pháp 1: Sử dụng API `/chat/ingest`

```javascript
// Frontend: Thêm 1 knowledge chunk
POST /api/v1/chat/ingest
{
  "title": "Pricing Guide 2024",
  "content": "App development cost ranges from 50M to 500M VND...",
  "language": "en",
  "source": "DOCUMENT",
  "sourceId": "pricing-doc-001"
}

// Response:
{
  "title": "Pricing Guide 2024",
  "snippet": "App development cost ranges from 50M to 500M VND...",
  "source": "DOCUMENT",
  "sourceId": "pricing-doc-001",
  "score": 1.0
}
```

### Phương pháp 2: Sử dụng API `/chat/sync-domain`

Tự động đồng bộ dữ liệu từ các bảng chính:

```javascript
POST /api/v1/chat/sync-domain
Header: Accept-Language: vi

// Response: Danh sách tất cả chunks được tạo
[
  {
    "title": "Project: Luvina",
    "snippet": "Công ty Luvina...",
    "source": "PROJECT",
    "sourceId": "project-uuid"
  },
  ...
]
```

**Dữ liệu được đồng bộ**:

1. **Projects** (Dự án)

   - Title: "Project: {projectName}"
   - Content: Mô tả dự án + công nghệ + nhân sự
   - Source: "PROJECT"

2. **Teams** (Đội)

   - Title: "Team: {teamName}"
   - Content: Mô tả team + thành viên
   - Source: "TEAM"

3. **Daily Tasks** (Công việc hàng ngày)

   - Title: "Task: {taskName}"
   - Content: Mô tả task + status + priority
   - Source: "TASK"

4. **FAQ Entries**

   - Title: "FAQ: {category}"
   - Content: "Q: {question} A: {answer}"
   - Source: "FAQ"
   - Cập nhật embedding nếu chưa có

5. **Technology Stacks**
   - Title: "Tech: {techName}"
   - Content: Mô tả công nghệ + pros/cons + use cases
   - Source: "DOCUMENT"
   - Cập nhật embedding nếu chưa có

---

## Xử Lý AI & Embedding

### 1. Embedding (Vector Hóa)

**Khái niệm**: Chuyển text thành vector (danh sách số) để so sánh giống nhau

```
Text: "App development cost"
      ↓ (AI encoding)
Vector: [0.25, 0.18, 0.42, ..., 0.93]  (1536 hoặc 768 chiều)
```

**Cách sử dụng**:

```java
// Bước 1: Khi thêm knowledge
double[] embedding = aiGatewayService.embed("App development content", "en");
chunk.setEmbedding(embedding);  // Lưu vector vào DB

// Bước 2: Khi user hỏi
double[] queryEmbedding = aiGatewayService.embed("App cost?", "en");

// Bước 3: So sánh với tất cả knowledge
double similarity = cosine(queryEmbedding, knowledgeEmbedding);
// similarity ~ 1.0 = rất giống
// similarity ~ 0.0 = rất khác
```

### 2. Cosine Similarity

Công thức toán học:

$$\text{similarity} = \frac{A \cdot B}{||A|| \times ||B||}$$

**Giải thích**:

- $A \cdot B$ = tích vô hướng (dot product)
- $||A||$ = độ dài vector A
- Kết quả: 0.0 đến 1.0 (0 = không giống, 1 = giống hệt)

**Code**:

```java
private double cosine(double[] a, double[] b) {
    double dotProduct = 0;
    double normA = 0;
    double normB = 0;

    for (int i = 0; i < a.length; i++) {
        dotProduct += a[i] * b[i];
        normA += a[i] * a[i];
        normB += b[i] * b[i];
    }

    return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
}
```

### 3. System Prompt & Context Building

**System Prompt** (hướng dẫn cho AI):

```
Bạn là trợ lý AI cho công ty Luvina Software.
Bạn giúp khách hàng trả lời câu hỏi về:
- Dịch vụ phát triển phần mềm
- Giá cả & chi phí
- Công nghệ & stack
- Quy trình làm việc

Luôn trả lời bằng tiếng Việt (nếu user hỏi Việt).
Trả lời chính xác, ngắn gọn, thân thiện.
Nếu không chắc, hãy nói "Tôi không biết" thay vì bịa chuyện.
```

**Context** (ngữ cảnh từ knowledge base):

```
Context:
- FAQ: Giá app mobile từ 30-80M tùy yêu cầu
- Project: CRM system giá 100M, dùng React + Java
- Tech: React tốt cho performance, có large community
```

**Chat History** (lịch sử trò chuyện):

```
Short chat history:
user: Công nghệ nào tốt nhất?
assistant: React hoặc Vue tùy vào yêu cầu...
user: Giá bao nhiêu?
```

**Final Prompt** (kết hợp):

```
System: "Bạn là trợ lý AI..."

Context:
- FAQ: Giá app mobile...
- Project: CRM...

Chat history:
user: Công nghệ nào?
assistant: React...

User (vi): Giá dịch vụ app mobile là bao nhiêu?
```

---

## Áp Dụng Cho Dự Án Khác

### Step 1: Sao Chép & Tùy Chỉnh Entities

```java
// Sao chép từ dự án Luvina:
// - ChatConversation.java
// - ChatMessage.java
// - ChatIntent.java
// - ChatFeedback.java
// - AiKnowledgeChunk.java

// Tùy chỉnh cho dự án mới:
// Ví dụ: Thêm field dây đủ cho dự án khác
// @Column(name = "department")
// private String department;  // Bộ phận liên quan
```

### Step 2: Tạo Migration Database

```sql
-- Sao chép migration V16, V17, V18 từ dự án Luvina
-- Sửa tên table/column nếu cần
-- Chạy migration: ./mvnw flyway:migrate
```

### Step 3: Tạo Repositories

```java
// Tạo interface extends JpaRepository
public interface ChatConversationRepository
    extends JpaRepository<ChatConversation, UUID> {
    List<ChatConversation> findByUserId(UUID userId);
    Page<ChatConversation> findRecent(Pageable pageable);
}

// Tương tự cho các entity khác
```

### Step 4: Tạo DTOs (Request/Response)

```java
// Sao chép DTOs từ dự án Luvina
// - ChatAskRequest.java
// - ChatAskResponse.java
// - ChatMessageResponse.java
// - etc.

// Tùy chỉnh theo nhu cầu dự án
```

### Step 5: Tạo Services

```java
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements IChatService {

    // Sao chép logic từ dự án Luvina
    // Tùy chỉnh:
    // - buildSystemPrompt() - đổi prompt phù hợp
    // - syncDomainData() - thêm/bỏ data sources
    // - Constants (MAX_HISTORY, MAX_REFERENCES, MIN_SCORE)
}
```

### Step 6: Tạo Controller

```java
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final IChatService chatService;

    @PostMapping("/ask")
    public ResponseEntity<ChatAskResponse> ask(
        @Valid @RequestBody ChatAskRequest request,
        @RequestHeader(name = "Accept-Language") Locale locale) {
        return ResponseEntity.ok(chatService.ask(request, locale, null));
    }

    // Copy endpoints từ dự án Luvina
}
```

### Step 7: Cấu Hình AI Provider

```properties
# application.properties
ai.provider=gemini
ai.gemini.api-key=YOUR_GEMINI_KEY
ai.gemini.model=gemini-2.5-flash

ai.openai.api-key=YOUR_OPENAI_KEY
ai.openai.model=gpt-4o-mini

ai.timeout-ms=20000

# Chatbot config
chatbot.similarity-threshold=0.65
chatbot.max-history=10
chatbot.max-references=7
```

### Step 8: Tạo Frontend Component

```vue
<!-- Sao chép từ dự án Luvina -->
<!-- FE/src/components/common/ChatWidget.vue -->
<!-- FE/src/components/common/ChatFeedback.vue -->
<!-- FE/src/services/apiChat.js -->

<!-- Tùy chỉnh màu sắc, logo, prompt -->
```

---

## Tóm Tắt Luồng Hoạt Động

```
┌─────────────────────────────────────────────┐
│ User Hỏi: "Giá dịch vụ là bao nhiêu?"     │
└────────────────┬────────────────────────────┘
                 │
                 ↓
         ┌───────────────┐
         │ 1. Tạo/Tìm    │
         │ Conversation  │
         └───────┬───────┘
                 │
                 ↓
         ┌───────────────┐
         │ 2. Lưu User   │
         │    Message    │
         └───────┬───────┘
                 │
                 ↓
         ┌────────────────────────┐
         │ 3. Phát Hiện Intent &  │
         │    Lưu Entities        │
         └────────┬───────────────┘
                  │
                  ↓
         ┌────────────────────────┐
         │ 4. Tạo Embedding      │
         │    (Vector hóa)        │
         └────────┬───────────────┘
                  │
                  ↓
         ┌────────────────────────┐
         │ 5. Tìm Knowledge      │
         │    Tương Tự (Top 5)   │
         └────────┬───────────────┘
                  │
                  ↓
         ┌────────────────────────┐
         │ 6. Lấy Chat History   │
         │    (Last 8 messages)  │
         └────────┬───────────────┘
                  │
                  ↓
         ┌────────────────────────┐
         │ 7. Build System        │
         │    Prompt + Contexts   │
         └────────┬───────────────┘
                  │
                  ↓
         ┌────────────────────────┐
         │ 8. Gọi AI Model       │
         │    (Gemini/OpenAI)    │
         └────────┬───────────────┘
                  │
                  ↓
         ┌───────────────┐
         │ 9. Lưu Bot   │
         │    Response  │
         └───────┬───────┘
                 │
                 ↓
         ┌───────────────────────┐
         │ 10. Return Response  │
         │ + References + Metadata
         └───────┬───────────────┘
                 │
                 ↓
    ┌────────────────────────────┐
    │ Frontend Hiển Thị          │
    │ - Message                  │
    │ - References (FAQ, Project)│
    │ - Feedback Component       │
    └────────────────────────────┘
```

---

## Các Config Quan Trọng

### application.properties

```properties
# AI Configuration
ai.provider=gemini                          # Sử dụng Gemini (Gemini hoặc OpenAI)
ai.gemini.api-key=AIzaSyBrtOC1p8f43...    # API key Gemini
ai.gemini.model=gemini-2.5-flash          # Model Gemini
ai.openai.api-key=${OPENAI_API_KEY:}      # API key OpenAI (nếu dùng)
ai.openai.model=gpt-4o-mini               # Model OpenAI
ai.timeout-ms=20000                        # Timeout 20 giây

# Chatbot Tuning
chatbot.similarity-threshold=0.65           # Ngưỡng similarity (0-1, cao = strict)
chatbot.max-history=10                      # Số tin nhắn lịch sử để dùng
chatbot.max-references=7                    # Số tài liệu tham khảo trả về

# Cache
spring.cache.type=caffeine
spring.cache.caffeine.spec=maximumSize=1000,expireAfterWrite=30m
```

### Constants trong ChatServiceImpl

```java
private static final int MAX_HISTORY = 8;       // Dùng 8 tin nhắn từ trước
private static final int MAX_REFERENCES = 5;   // Trả 5 tài liệu
private static final double MIN_SCORE = 0.52;  // Similarity >= 52%
```

---

## Troubleshooting & Best Practices

### 1. Embedding không chính xác?

- ✅ Kiểm tra dữ liệu trong ai_knowledge_chunks
- ✅ Tăng `similarity-threshold` nếu quá nhiều kết quả không liên quan
- ✅ Giảm `MIN_SCORE` nếu quá ít kết quả

### 2. AI response không tốt?

- ✅ Sửa `buildSystemPrompt()` - system prompt quá vague
- ✅ Thêm dữ liệu vào knowledge base (thêm FAQs, docs)
- ✅ Điều chỉnh `temperature` (0.0-1.0, thấp = deterministic, cao = creative)
- ✅ Tăng `max-history` để AI có ngữ cảnh tốt hơn

### 3. Performance chậm?

- ✅ Kiểm tra indexes trong database (idx*chat_feedback*...)
- ✅ Enable cache cho `getKnowledge()` (đã enable)
- ✅ Tăng `ai.timeout-ms` nếu API chậm
- ✅ Reduce `MAX_REFERENCES` để process ít chunks

### 4. API quota hết?

- ✅ Kiểm tra API limits từ Gemini/OpenAI
- ✅ Implement rate limiting
- ✅ Cache responses để tránh gọi lại
- ✅ Monitor API usage logs

---

## Tài Liệu Tham Khảo

- [Google Gemini API Docs](https://ai.google.dev)
- [OpenAI API Docs](https://platform.openai.com/docs)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [PostgreSQL Array/JSON Types](https://www.postgresql.org/docs)

---

**End of Guide**  
Nếu có câu hỏi thêm, vui lòng tham khảo source code trong project.
