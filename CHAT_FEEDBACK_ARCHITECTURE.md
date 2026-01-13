# Chat Feedback System - Architecture & Data Flow

## System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                         CHAT FEEDBACK SYSTEM                         │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                          FRONTEND (Vue.js)                           │
│                    src/components/common/ChatFeedback.vue            │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ┌──────────────────────────────────────────────────────┐           │
│  │ Rating Section (Always Visible)                      │           │
│  │ ─────────────────────────────────────────────────   │           │
│  │ Label: t('chatFeedback.rating')                     │           │
│  │ Component: el-rate (1-5 stars, half-support)        │           │
│  │ Behavior: @change → trigger expansion               │           │
│  └──────────────────────────────────────────────────────┘           │
│                           ↓ (on select)                             │
│  ┌──────────────────────────────────────────────────────┐           │
│  │ Feedback Form (Auto-Expandable)                      │           │
│  │ ─────────────────────────────────────────────────   │           │
│  │ [1] Helpful/Not Helpful                              │           │
│  │     ├─ t('chatFeedback.helpful')                    │           │
│  │     ├─ el-button (yes/no with types)                │           │
│  │     └─ feedback.isHelpful = true|false|null         │           │
│  │                                                      │           │
│  │ [2] Issue Type Select (i18n)                        │           │
│  │     ├─ t('chatFeedback.issueType')                 │           │
│  │     ├─ el-select (clearable)                        │           │
│  │     ├─ :label="t('chatFeedback.inaccurate')"       │           │
│  │     ├─ :label="t('chatFeedback.irrelevant')"       │           │
│  │     ├─ :label="t('chatFeedback.incomplete')"       │           │
│  │     ├─ :label="t('chatFeedback.rude')"             │           │
│  │     ├─ :label="t('chatFeedback.other')"            │           │
│  │     └─ feedback.issueType = "INACCURATE"|null      │           │
│  │                                                      │           │
│  │ [3] Feedback Text                                    │           │
│  │     ├─ t('chatFeedback.feedbackText')              │           │
│  │     ├─ el-input textarea (max 500 chars)            │           │
│  │     └─ feedback.feedbackText = ""                   │           │
│  │                                                      │           │
│  │ [4] Actions                                          │           │
│  │     ├─ Submit: submitFeedback()                      │           │
│  │     └─ Cancel: resetFeedback()                       │           │
│  └──────────────────────────────────────────────────────┘           │
│                           ↓ (on submit)                             │
│  ┌──────────────────────────────────────────────────────┐           │
│  │ Success Alert (3 seconds)                            │           │
│  │ ─────────────────────────────────────────────────   │           │
│  │ ✓ t('chatFeedback.successMessage')                  │           │
│  │   Auto-closes + form resets                          │           │
│  └──────────────────────────────────────────────────────┘           │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
                                ↓ HTTP
                    POST /api/v1/chat/feedback
                    {rating, isHelpful, issueType, feedbackText}
                                ↓

┌─────────────────────────────────────────────────────────────────────┐
│                       BACKEND (Spring Boot)                          │
│                    ChatController → ChatService                      │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ┌──────────────────────────────────────────────────────┐           │
│  │ Request Processing                                   │           │
│  │ ─────────────────────────────────────────────────   │           │
│  │ 1. Receive request with String issueType            │           │
│  │ 2. Parse: @Valid ChatFeedbackRequest               │           │
│  │ 3. Extract userId from Authentication               │           │
│  └──────────────────────────────────────────────────────┘           │
│                           ↓                                         │
│  ┌──────────────────────────────────────────────────────┐           │
│  │ ChatService.saveFeedback()                           │           │
│  │ ─────────────────────────────────────────────────   │           │
│  │ 1. Create ChatFeedback entity                        │           │
│  │ 2. String → Enum Conversion:                        │           │
│  │    "INACCURATE" → IssueType.INACCURATE             │           │
│  │ 3. Handle invalid values: log warning, skip         │           │
│  │ 4. Set all fields:                                  │           │
│  │    ├─ conversationId, messageId, userId             │           │
│  │    ├─ rating (1-5)                                  │           │
│  │    ├─ isHelpful (true|false|null)                  │           │
│  │    ├─ issueType (enum)                              │           │
│  │    ├─ feedbackText                                  │           │
│  │    └─ createdAt (now)                               │           │
│  │ 5. Save to repository                               │           │
│  │ 6. Enum → String for response:                      │           │
│  │    IssueType.INACCURATE → "INACCURATE"             │           │
│  └──────────────────────────────────────────────────────┘           │
│                           ↓                                         │
│  ┌──────────────────────────────────────────────────────┐           │
│  │ Response Mapping                                     │           │
│  │ ─────────────────────────────────────────────────   │           │
│  │ ChatFeedbackResponse {                               │           │
│  │   id: UUID,                                          │           │
│  │   conversationId: UUID,                              │           │
│  │   messageId: UUID,                                   │           │
│  │   rating: Integer,                                   │           │
│  │   isHelpful: Boolean,                                │           │
│  │   feedbackText: String,                              │           │
│  │   issueType: String,  ← "INACCURATE"               │           │
│  │   createdAt: OffsetDateTime                          │           │
│  │ }                                                    │           │
│  └──────────────────────────────────────────────────────┘           │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
                                ↓ HTTP
                    200 OK + ChatFeedbackResponse
                                ↓

┌─────────────────────────────────────────────────────────────────────┐
│                      DATABASE (PostgreSQL)                           │
│                  chat_feedback Table (v18 migration)                 │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  Table Structure:                                                   │
│  ┌────────────────────────────────────────────────────┐             │
│  │ Column              │ Type        │ Constraints   │             │
│  ├─────────────────────┼─────────────┼──────────────┤             │
│  │ id                  │ UUID        │ PRIMARY KEY  │             │
│  │ conversation_id     │ UUID        │ NOT NULL, FK │             │
│  │ message_id          │ UUID        │ NOT NULL     │             │
│  │ user_id             │ UUID        │ NULL         │             │
│  │ rating              │ INTEGER     │ 1-5 or NULL  │             │
│  │ is_helpful          │ BOOLEAN     │ NULL         │             │
│  │ feedback_text       │ TEXT        │ NULL         │             │
│  │ issue_type          │ issue_type  │ ENUM (v18)  │             │
│  │ created_at          │ TIMESTAMPTZ │ DEFAULT NOW  │             │
│  └────────────────────────────────────────────────────┘             │
│                                                                      │
│  Enum Type: issue_type_enum                                         │
│  ├─ INACCURATE                                                     │
│  ├─ IRRELEVANT                                                     │
│  ├─ INCOMPLETE                                                     │
│  ├─ RUDE                                                           │
│  └─ OTHER                                                          │
│                                                                      │
│  Check Constraints (v18):                                           │
│  ├─ rating >= 1 AND rating <= 5 (or NULL)                         │
│  └─ At least one of: isHelpful, rating, feedbackText, issueType   │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Data Type Conversion Flow

```
┌──────────────────────────────────────────────────────────────┐
│              STRING ↔ ENUM CONVERSION STRATEGY               │
└──────────────────────────────────────────────────────────────┘

FRONTEND (String)
    "INACCURATE"
         ↓

POST Request
    issueType: "INACCURATE"
         ↓

ChatFeedbackRequest (String)
    request.getIssueType()  // "INACCURATE"
         ↓

ChatService.saveFeedback()
    String → Enum Conversion:
    ChatFeedback.IssueType.valueOf("INACCURATE")
         ↓

ChatFeedback Entity (Enum)
    private IssueType issueType;  // IssueType.INACCURATE
         ↓

Database Storage (Enum as TEXT)
    issue_type_enum type
    Value stored: "INACCURATE" (as string)
         ↓

Database Retrieval
    Enum type converts back:
    "INACCURATE" → IssueType.INACCURATE
         ↓

ChatService (Enum → String)
    .issueType()
    IssueType.INACCURATE → "INACCURATE"
         ↓

ChatFeedbackResponse (String)
    issueType: "INACCURATE"
         ↓

GET Response (JSON)
    "issueType": "INACCURATE"
         ↓

Frontend (String)
    Receives: "INACCURATE"
```

---

## Localization Flow

```
┌──────────────────────────────────────────────────────────────┐
│           INTERNATIONALIZATION (i18n) STRATEGY               │
└──────────────────────────────────────────────────────────────┘

Frontend Component (ChatFeedback.vue)
    ├─ Imported: useI18n() from vue-i18n
    └─ Current locale: computed from router/store
         ↓

Locale Detection
    ├─ User language preference
    ├─ Browser language
    └─ Fall back to Vietnamese (default)
         ↓

Translation Keys (Used in Template)
    <el-option :label="t('chatFeedback.inaccurate')" value="INACCURATE" />
    <el-option :label="t('chatFeedback.irrelevant')" value="IRRELEVANT" />
    <el-option :label="t('chatFeedback.incomplete')" value="INCOMPLETE" />
    <el-option :label="t('chatFeedback.rude')" value="RUDE" />
    <el-option :label="t('chatFeedback.other')" value="OTHER" />
         ↓

Locale Files
    ├─ src/locales/vi/chatWidget.json
    │  ├─ chatFeedback.inaccurate: "Không chính xác"
    │  ├─ chatFeedback.irrelevant: "Không liên quan"
    │  ├─ chatFeedback.incomplete: "Không đủ thông tin"
    │  ├─ chatFeedback.rude: "Thô lỗ"
    │  └─ chatFeedback.other: "Khác"
    │
    ├─ src/locales/en/chatWidget.json
    │  ├─ chatFeedback.inaccurate: "Inaccurate"
    │  ├─ chatFeedback.irrelevant: "Irrelevant"
    │  ├─ chatFeedback.incomplete: "Incomplete"
    │  ├─ chatFeedback.rude: "Rude"
    │  └─ chatFeedback.other: "Other"
    │
    └─ src/locales/ja/chatWidget.json
       ├─ chatFeedback.inaccurate: "不正確"
       ├─ chatFeedback.irrelevant: "無関係"
       ├─ chatFeedback.incomplete: "不完全"
       ├─ chatFeedback.rude: "失礼"
       └─ chatFeedback.other: "その他"
         ↓

Runtime Translation
    t('chatFeedback.inaccurate')
    ↓
    Looks up current locale + key
    ↓
    Returns translated string
    ├─ Vietnamese: "Không chính xác"
    ├─ English: "Inaccurate"
    └─ Japanese: "不正確"
         ↓

Option Label Display
    [Không chính xác]  (or Inaccurate / 不正確)
         ↓

Value Stored
    value="INACCURATE"  (always same)
```

---

## Enum Values & Mapping

```
┌────────────────────────────────────────────────────────────────┐
│              ISSUE TYPE ENUM - COMPLETE REFERENCE               │
└────────────────────────────────────────────────────────────────┘

Database Enum Type: issue_type_enum

┌─────────────┬──────────────────────┬──────────────────┬────────────┐
│ Enum Value  │ Vietnamese           │ English          │ Japanese   │
├─────────────┼──────────────────────┼──────────────────┼────────────┤
│ INACCURATE  │ Không chính xác      │ Inaccurate       │ 不正確     │
│ IRRELEVANT  │ Không liên quan      │ Irrelevant       │ 無関係     │
│ INCOMPLETE  │ Không đủ thông tin   │ Incomplete       │ 不完全     │
│ RUDE        │ Thô lỗ               │ Rude             │ 失礼       │
│ OTHER       │ Khác                 │ Other            │ その他     │
└─────────────┴──────────────────────┴──────────────────┴────────────┘

Java Enum Definition:
public enum IssueType {
    INACCURATE,    // Không chính xác
    IRRELEVANT,    // Không liên quan
    INCOMPLETE,    // Không đủ thông tin
    RUDE,          // Thô lỗ
    OTHER          // Khác
}

JSON Serialization: String "INACCURATE"
Database Storage: TEXT as "INACCURATE"
API Response: String "INACCURATE"
```

---

## Complete Request/Response Cycle

```
1. USER INTERACTION (Frontend)
   ┌──────────────────────────────────────┐
   │ User sees bot response               │
   │ Clicks on rating star (3.5 stars)   │
   │ Form auto-expands                    │
   │                                      │
   │ Selects:                             │
   │ - Helpful: "Có" (yes)               │
   │ - Issue: "Không chính xác"          │
   │ - Text: "Response too brief"        │
   │ Clicks: "Gửi phản hồi"             │
   └──────────────────────────────────────┘
                ↓

2. PAYLOAD (JavaScript Object)
   ┌──────────────────────────────────────┐
   │ feedback = {                         │
   │   conversationId: UUID,              │
   │   messageId: UUID,                   │
   │   isHelpful: true,                  │
   │   rating: 3.5,                      │
   │   issueType: "INACCURATE",          │
   │   feedbackText: "Response too brief" │
   │ }                                    │
   └──────────────────────────────────────┘
                ↓

3. HTTP REQUEST
   ┌──────────────────────────────────────┐
   │ POST /api/v1/chat/feedback           │
   │ Content-Type: application/json       │
   │                                      │
   │ {                                    │
   │   "conversationId": "...",           │
   │   "messageId": "...",                │
   │   "isHelpful": true,                │
   │   "rating": 3.5,                    │
   │   "issueType": "INACCURATE",        │
   │   "feedbackText": "Response too..."  │
   │ }                                    │
   └──────────────────────────────────────┘
                ↓

4. BACKEND PROCESSING
   ┌──────────────────────────────────────┐
   │ ChatController.submitFeedback()      │
   │ ├─ Validate with @Valid             │
   │ ├─ Extract userId from auth         │
   │ └─ Call chatService.saveFeedback()  │
   │                                      │
   │ ChatService.saveFeedback()           │
   │ ├─ Create ChatFeedback entity        │
   │ ├─ Convert: "INACCURATE"            │
   │ │          → IssueType.INACCURATE   │
   │ ├─ chatFeedbackRepository.save()    │
   │ └─ Convert: IssueType.INACCURATE    │
   │            → "INACCURATE"           │
   │                                      │
   │ Return ChatFeedbackResponse {        │
   │   id: UUID,                         │
   │   conversationId: UUID,             │
   │   messageId: UUID,                  │
   │   isHelpful: true,                 │
   │   rating: 3.5,                     │
   │   issueType: "INACCURATE",         │
   │   feedbackText: "...",              │
   │   createdAt: Timestamp              │
   │ }                                    │
   └──────────────────────────────────────┘
                ↓

5. DATABASE STORAGE
   ┌──────────────────────────────────────┐
   │ INSERT INTO chat_feedback            │
   │ VALUES (                             │
   │   id,                  // UUID       │
   │   conversation_id,     // UUID       │
   │   message_id,          // UUID       │
   │   user_id,             // UUID       │
   │   3.5,                 // rating     │
   │   true,                // is_helpful │
   │   "Response too...",   // text       │
   │   'INACCURATE',        // enum       │
   │   now()                // created_at │
   │ )                                    │
   │                                      │
   │ Constraints checked:                 │
   │ ✓ Rating in 1-5 range               │
   │ ✓ Issue type in enum values         │
   │ ✓ At least one field set            │
   │ ✓ Foreign key valid                 │
   └──────────────────────────────────────┘
                ↓

6. HTTP RESPONSE (200 OK)
   ┌──────────────────────────────────────┐
   │ {                                    │
   │   "id": "new-uuid",                 │
   │   "conversationId": "...",          │
   │   "messageId": "...",               │
   │   "isHelpful": true,                │
   │   "rating": 3.5,                    │
   │   "issueType": "INACCURATE",        │
   │   "feedbackText": "Response too...", │
   │   "createdAt": "2026-01-14T..."     │
   │ }                                    │
   └──────────────────────────────────────┘
                ↓

7. FRONTEND SUCCESS
   ┌──────────────────────────────────────┐
   │ Show success alert:                  │
   │ "✓ Cảm ơn bạn đã phản hồi!"        │
   │                                      │
   │ After 3 seconds:                     │
   │ - Alert closes                       │
   │ - Form resets                        │
   │ - Rating stars back to empty         │
   │ - Ready for next feedback            │
   └──────────────────────────────────────┘
```

---

## Architecture Benefits

### Type Safety

✅ **Compiler Level**: Enum prevents invalid values
✅ **Database Level**: PostgreSQL ENUM constraint
✅ **Runtime Level**: Graceful conversion with logging

### Data Integrity

✅ **Constraints**: Rating (1-5), at least one field
✅ **Consistency**: No duplicate/typo issue types
✅ **Audit Trail**: createdAt, userId tracking

### Code Quality

✅ **Type-Safe**: No string comparisons
✅ **Maintainable**: Central enum definition
✅ **Testable**: Enum-based unit tests

### User Experience

✅ **Intuitive**: Clear form flow
✅ **Responsive**: Auto-expand animation
✅ **Multilingual**: Full i18n support
✅ **Accessible**: Proper button states

---

## Summary

This chat feedback system provides:

1. **Clean UI**: Compact rating → auto-expanding form
2. **Type Safety**: Enum-based issue types
3. **Database Integrity**: Constraints and validation
4. **Internationalization**: Support for Vi, En, Ja
5. **Seamless Conversion**: String ↔ Enum handling
6. **Complete Documentation**: Implementation guides
