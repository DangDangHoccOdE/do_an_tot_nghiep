# Trạng Thái Các Bảng Chat AI - Phân Tích Chi Tiết

## 📊 Tổng Quan Các Bảng

### ✅ **Bảng ĐANG ĐƯỢC SỬ DỤNG**

| Bảng                  | Entity             | Repository                   | Service           | Controller       | Mục Đích                   |
| --------------------- | ------------------ | ---------------------------- | ----------------- | ---------------- | -------------------------- |
| `chat_conversations`  | ChatConversation ✓ | ChatConversationRepository ✓ | ChatServiceImpl ✓ | ChatController ✓ | Lưu cuộc hội thoại         |
| `chat_messages`       | ChatMessage ✓      | ChatMessageRepository ✓      | ChatServiceImpl ✓ | ChatController ✓ | Lưu tin nhắn               |
| `ai_knowledge_chunks` | AiKnowledgeChunk ✓ | AiKnowledgeChunkRepository ✓ | ChatServiceImpl ✓ | ChatController ✓ | Lưu kiến thức + embeddings |
| `faq_entries`         | FaqEntry ✓         | FaqEntryRepository ✓         | ChatServiceImpl ✓ | -                | FAQ cho chatbot            |
| `technology_stacks`   | TechnologyStack ✓  | TechnologyStackRepository ✓  | ChatServiceImpl ✓ | -                | Gợi ý công nghệ            |

---

### ⚠️ **Bảng TỒN TẠI NHƯNG CHƯA DÙNG (Cần Triển Khai)**

#### 1. **`chat_intents`** - Phát Hiện Ý Định 🔴 CRITICAL

**Entity:** `ChatIntent.java` ✓  
**Repository:** `ChatIntentRepository.java` ✓ (có sẵn queries)  
**Service:** ❌ CHƯA CÓ  
**Controller:** ❌ CHƯA CÓ

**Trạng Thái:**

```
✓ Entity đã tạo
✓ Repository đã tạo với các methods:
  - findByConversationId()
  - getIntentStatistics()
  - findByConversationIdOrderByCreatedAtDesc()
✗ Không có service nào gọi repository này
✗ Không có endpoint nào expose
```

**Cần Làm:**

- Tạo `IntentDetectionService` để detect intent từ user message
- Tích hợp vào `ChatServiceImpl.ask()` để lưu intent mỗi khi chat
- Tạo endpoint `GET /api/v1/chat/intents/statistics` (Admin)
- Sử dụng intent để filter knowledge base (improve accuracy)

**Impact:** 🔴 **HIGH** - Giúp chatbot hiểu rõ hơn ý định người dùng

---

#### 2. **`chat_feedback`** - Feedback Người Dùng 🔴 CRITICAL

**Entity:** `ChatFeedback.java` ✓  
**Repository:** `ChatFeedbackRepository.java` ✓ (có sẵn queries)  
**Service:** ❌ CHƯA CÓ  
**Controller:** ❌ CHƯA CÓ

**Trạng Thái:**

```
✓ Entity đã tạo
✓ Repository đã tạo với các methods:
  - findByConversationId()
  - getAverageRating()
  - findLowRatedFeedback()
✗ Không có service nào gọi repository này
✗ Không có endpoint nào expose
✗ User KHÔNG THỂ feedback về chất lượng bot
```

**Cần Làm:**

- Thêm method `saveFeedback()` vào `IChatService` và `ChatServiceImpl`
- Tạo endpoint `POST /api/v1/chat/feedback`
- Tạo endpoint `GET /api/v1/chat/feedback/statistics` (Admin)
- Frontend thêm thumbs up/down button và rating stars

**Impact:** 🔴 **HIGH** - Critical để cải thiện chatbot qua feedback

---

#### 3. **`ai_consulting_logs`** - Logs Tư Vấn AI ⚠️ CÂN NHẮC

**Entity:** `AIConsultingLog.java` ✓  
**Repository:** `AIConsultingLogRepository.java` ✓  
**Service:** ❌ CHƯA CÓ  
**Controller:** ❌ CHƯA CÓ

**Trạng Thái:**

```
✓ Entity đã tạo (rất đơn giản: user_id, question, aiResponse)
✓ Repository đã tạo (chỉ extend BaseRepository)
✗ Không có service nào gọi
✗ Không có endpoint nào expose
```

**Phân Tích:**

- **Dữ liệu trùng lặp** với `chat_messages` table
- `chat_messages` đã lưu đầy đủ: user message + assistant response
- `AIConsultingLog` không có thêm thông tin gì mới

**Khuyến Nghị:**

```
🟡 XÓA HOẶC MERGE

Option 1: XÓA bảng này (recommended)
  - chat_messages đã đủ để lưu Q&A
  - Tránh duplicate data

Option 2: MỞ RỘNG để khác biệt
  - Thêm fields: consultingType, estimatedCost, recommendedTech
  - Chỉ lưu những tư vấn chính thức (không lưu mọi chat)
  - Dùng cho báo cáo thống kê consulting
```

**Impact:** 🟡 **MEDIUM** - Có thể xóa hoặc mở rộng

---

## 📋 Kế Hoạch Triển Khai

### Phase 1: Critical Features (Ưu tiên cao) 🔴

#### Task 1.1: Implement ChatFeedback

```java
// 1. Create DTOs
- ChatFeedbackRequest.java
- ChatFeedbackResponse.java
- ChatFeedbackStatisticsResponse.java

// 2. Update IChatService
+ saveFeedback(ChatFeedbackRequest request)
+ getFeedbackStatistics()
+ getLowRatedFeedbacks()

// 3. Update ChatController
+ POST /api/v1/chat/feedback
+ GET /api/v1/chat/feedback/statistics (Admin)

// 4. Frontend Update
- Add feedback buttons to chat UI
- Show rating stars
```

#### Task 1.2: Implement Intent Detection

```java
// 1. Create IntentDetectionService
- detectIntent(String message, String locale)
- extractEntities(String message, String intent)
- calculateConfidence(String message, String intent)

// 2. Update ChatServiceImpl.ask()
- Call intentDetectionService after user message
- Save ChatIntent to database
- Use intent to filter knowledge base

// 3. Add Admin Endpoints
+ GET /api/v1/chat/intents/statistics
+ GET /api/v1/chat/intents/{conversationId}
```

---

### Phase 2: Performance & Quality Improvements (Ưu tiên trung bình) 🟡

#### Task 2.1: Rate Limiting

```java
// Add to pom.xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-ratelimiter</artifactId>
</dependency>

// Update ChatController.ask()
@RateLimiter(name = "chatAsk", fallbackMethod = "rateLimitFallback")
public ResponseEntity<ChatAskResponse> ask(...)

// application.properties
resilience4j.ratelimiter.instances.chatAsk.limitForPeriod=20
resilience4j.ratelimiter.instances.chatAsk.limitRefreshPeriod=1m
```

#### Task 2.2: Externalize Configuration

```properties
# application.properties
chatbot.similarity-threshold=0.65
chatbot.max-history=10
chatbot.max-references=7
chatbot.timeout-ms=20000
```

#### Task 2.3: Add Caching

```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        return new CaffeineCacheManager("knowledgeCache");
    }
}

// ChatServiceImpl
@Cacheable(value = "knowledgeCache", key = "#locale")
private List<AiKnowledgeChunk> getKnowledge(String locale)

@CacheEvict(value = "knowledgeCache", allEntries = true)
public List<KnowledgeReferenceResponse> syncDomainData(Locale locale)
```

#### Task 2.4: Add Structured Logging

```java
// ChatServiceImpl
private static final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);

public ChatAskResponse ask(...) {
    logger.info("Chat request: conversationId={}, locale={}, messageLength={}", ...);
    try {
        // ...
        logger.info("Chat response success: provider={}, referencesCount={}", ...);
    } catch (Exception e) {
        logger.error("Chat error: conversationId={}, error={}", ..., e);
        throw e;
    }
}
```

---

### Phase 3: Decision on AIConsultingLog (Quyết định) 🟡

**Lựa chọn 1: XÓA (Recommended)**

```sql
-- Drop table
DROP TABLE ai_consulting_logs;

-- Remove files
rm AIConsultingLog.java
rm AIConsultingLogRepository.java
```

**Lựa chọn 2: MỞ RỘNG (Nếu muốn giữ)**

```java
// Extend AIConsultingLog.java
@Entity
@Table(name = "ai_consulting_logs")
public class AIConsultingLog extends BaseEntity {
    private UUID conversationId; // Link to chat_conversations

    @Enumerated(EnumType.STRING)
    private ConsultingType type; // PRICING, TECH_STACK, PROJECT_TIMELINE

    private String detectedIntent;

    @Column(columnDefinition = "TEXT")
    private String question;

    @Column(columnDefinition = "TEXT")
    private String aiResponse;

    // Consulting-specific fields
    private Long estimatedBudget;
    private Integer estimatedDays;
    private String[] recommendedTechnologies;

    private Boolean followedUp; // Did we follow up with client?
    private OffsetDateTime followUpDate;
}
```

---

## 🎯 Tóm Tắt & Khuyến Nghị

### Bảng Đang Dùng Tốt ✅

1. `chat_conversations` - ✓ Hoạt động tốt
2. `chat_messages` - ✓ Hoạt động tốt
3. `ai_knowledge_chunks` - ✓ Hoạt động tốt
4. `faq_entries` - ✓ Đang dùng trong syncDomainData()
5. `technology_stacks` - ✓ Đang dùng trong syncDomainData()

### Bảng Cần Triển Khai Ngay 🔴

1. **`chat_intents`** - Để detect ý định người dùng
2. **`chat_feedback`** - Để collect feedback và cải thiện

### Bảng Cần Quyết Định 🟡

1. **`ai_consulting_logs`**
   - **Option A:** Xóa (vì trùng với chat_messages)
   - **Option B:** Mở rộng (thêm consulting-specific fields)
   - **Khuyến nghị:** Xóa để tránh duplicate data

### Priority Order

```
1. 🔴 HIGH: Implement ChatFeedback (quan trọng nhất)
2. 🔴 HIGH: Implement Intent Detection
3. 🟡 MEDIUM: Add Rate Limiting
4. 🟡 MEDIUM: Add Caching
5. 🟡 MEDIUM: Externalize Config
6. 🟡 MEDIUM: Add Logging
7. 🟢 LOW: Quyết định về AIConsultingLog
```

---

## 📝 Chi Tiết Implementation

### File Structure Cần Tạo

```
dto/request/
  ├─ ChatFeedbackRequest.java (NEW)

dto/response/
  ├─ ChatFeedbackResponse.java (NEW)
  └─ ChatFeedbackStatisticsResponse.java (NEW)

service/inter/
  └─ IIntentDetectionService.java (NEW)

service/impl/
  ├─ IntentDetectionServiceImpl.java (NEW)
  └─ ChatServiceImpl.java (UPDATE)

controller/
  └─ ChatController.java (UPDATE - add feedback endpoint)

config/
  ├─ CacheConfig.java (NEW)
  └─ RateLimitConfig.java (NEW)
```

---

## Kết Luận

**Các bảng cần triển khai:**

- ✅ chat_intents (có entity + repository, cần service + controller)
- ✅ chat_feedback (có entity + repository, cần service + controller)

**Bảng cần xem xét xóa:**

- ⚠️ ai_consulting_logs (duplicate với chat_messages, không có giá trị thêm)

**Tất cả đã có infrastructure (entity + repository), chỉ cần viết business logic!**
