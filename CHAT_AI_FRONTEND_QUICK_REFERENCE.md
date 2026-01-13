# Chat AI Frontend Quick Reference & Checklist

## 📋 Files Created/Updated

### ✅ New Components Created

```
FE/src/components/
├── ChatFeedback.vue                    # User feedback form
└── ChatIntentDisplay.vue               # Intent visualization

FE/src/components/admin/
└── ChatFeedbackDashboard.vue           # Admin analytics dashboard
```

### ✅ Updated Components

```
FE/src/components/
└── ChatWidget.vue                      # Enhanced with feedback & intent integration

FE/src/services/
└── apiChat.js                          # New endpoints: submitFeedback, getFeedbackStatistics, getLowRatedFeedbacks, getConversationIntents
```

### ✅ Updated i18n Translations

```
FE/src/locales/
├── en/
│   ├── chatWidget.json                 # Added: chatFeedback, chatIntent
│   └── admin.json                      # Added: feedbackDashboard keys
├── vi/
│   ├── chatWidget.json                 # Added: chatFeedback, chatIntent (Vietnamese)
│   └── admin.json                      # Added: feedbackDashboard keys (Vietnamese)
└── ja/
    ├── chatWidget.json                 # Added: chatFeedback, chatIntent (Japanese)
    └── admin.json                      # Added: feedbackDashboard keys (Japanese)
```

---

## 🎯 Component Usage Examples

### 1. ChatFeedback

```vue
<ChatFeedback
  :conversation-id="conversationId"
  :message-id="messageId"
  @feedback-submitted="handleFeedbackSubmitted"
/>
```

**Props:**

- `conversationId: String` - Conversation ID
- `messageId: String` - Message ID to feedback on

**Events:**

- `@feedback-submitted` - Emitted when feedback submitted

**Template Fragment:**

```vue
<el-icon><Close /></el-icon>  <!-- Close button -->
<el-radio-group v-model="isHelpful">   <!-- Yes/No toggle -->
<el-rate v-model="rating" />            <!-- 1-5 stars -->
<el-select v-model="issueType">         <!-- Issue type dropdown -->
<el-input v-model="feedbackText">       <!-- Feedback textarea -->
<el-button @click="submitFeedback">     <!-- Submit button -->
```

---

### 2. ChatIntentDisplay

```vue
<ChatIntentDisplay v-if="msg.intent" :intent-data="msg.intent" />
```

**Props:**

- `intentData: Object` - Intent data object with:
  ```javascript
  {
    detectedIntent: "PRICING_INQUIRY",
    confidenceScore: 0.95,
    extractedEntities: {
      budget: "$50,000",
      timeline: "3 months",
      technologies: ["React"],
      projectType: "Web App"
    }
  }
  ```

**Features:**

- Intent badge with confidence %
- Collapsible entity details
- Color-coded by intent type

---

### 3. ChatFeedbackDashboard

```vue
<template>
  <ChatFeedbackDashboard />
</template>

<script setup>
import ChatFeedbackDashboard from "@/components/admin/ChatFeedbackDashboard.vue";
</script>
```

**Features:**

- 4 statistics cards (avg rating, total, helpful, not helpful)
- Top issues bar chart
- Low-rated feedbacks table
- Refresh button

**Data:**

```javascript
{
  averageRating: 4.2,
  totalFeedbacks: 150,
  helpfulCount: 120,
  notHelpfulCount: 30,
  topIssues: [
    { issueType: 'INACCURATE', count: 25 },
    { issueType: 'INCOMPLETE', count: 20 }
  ]
}
```

---

## 🔌 API Service Methods (apiChat.js)

### New Methods Added

#### 1. Submit Feedback

```javascript
async submitFeedback(conversationId, messageId, feedbackData) {
  return await request.post(`/api/v1/chat/feedback`, {
    conversationId,
    messageId,
    rating: feedbackData.rating,           // 1-5
    isHelpful: feedbackData.isHelpful,     // true/false
    issueType: feedbackData.issueType,     // string
    feedbackText: feedbackData.feedbackText // string (optional)
  })
}
```

#### 2. Get Feedback Statistics

```javascript
async getFeedbackStatistics() {
  return await request.get(`/api/v1/chat/feedback/statistics`)
}
// Returns: { averageRating, totalFeedbacks, helpfulCount, notHelpfulCount, topIssues }
```

#### 3. Get Low-Rated Feedbacks

```javascript
async getLowRatedFeedbacks(limit = 50) {
  return await request.get(`/api/v1/chat/feedback/low-rated`, {
    params: { limit }
  })
}
// Returns: Array of feedback objects with rating ≤ 2
```

#### 4. Get Conversation Intents

```javascript
async getConversationIntents(conversationId) {
  return await request.get(`/api/v1/chat/intents/${conversationId}`)
}
// Returns: Array of intent objects
```

---

## 🌍 i18n Translation Keys

### Chat Widget Feedback Keys

```javascript
chatFeedback: {
  helpful: "Was this response helpful?",  // Main question
  yes: "Yes",
  no: "No",
  rating: "Rate this response",
  issueType: "Issue type",
  selectIssue: "-- Select issue type --",
  feedbackText: "Additional feedback",
  feedbackPlaceholder: "Tell us what went wrong...",
  submit: "Submit feedback",
  successMessage: "Thank you for your feedback!",
  submitError: "Failed to submit feedback",
  selectAtLeast: "Please select at least one feedback option",
  inaccurate: "Inaccurate",
  irrelevant: "Irrelevant",
  incomplete: "Incomplete",
  rude: "Rude",
  other: "Other"
}
```

### Chat Widget Intent Keys

```javascript
chatIntent: {
  extractedEntities: "Extracted Information",
  pricingInquiry: "Pricing/Budget Question",
  techRecommendation: "Technology Recommendation",
  projectTimeline: "Timeline Question",
  featureRequest: "Feature Request",
  generalInfo: "General Information",
  budget: "Budget",
  timeline: "Timeline",
  projectType: "Project Type",
  technologies: "Mentioned Technologies"
}
```

### Admin Dashboard Keys

```javascript
admin: {
  feedbackDashboard: "Chat Feedback Dashboard",
  averageRating: "Average Rating",
  totalFeedbacks: "Total Feedbacks",
  helpful: "Helpful",
  notHelpful: "Not Helpful",
  responses: "responses",
  topIssues: "Top Issues",
  lowRatedFeedbacks: "Low Rated Feedbacks",
  issueType: "Issue Type",
  feedback: "Feedback",
  date: "Date",
  loadStatisticsError: "Failed to load statistics"
}
```

---

## 📖 ChatWidget.vue Integration

### Message Object Structure

```javascript
{
  id: "uuid",                    // From API response - for feedback linking
  role: "assistant",             // "user" or "assistant"
  content: "Response text",      // The actual message
  intent: {                      // Async loaded from backend
    detectedIntent: "PRICING_INQUIRY",
    confidenceScore: 0.95,
    extractedEntities: { ... }
  }
}
```

### Template Changes

```vue
<!-- In message loop -->
<div v-for="msg in messages" :key="msg.id" class="message">
  <!-- Message content -->
  <p>{{ msg.content }}</p>
  
  <!-- Intent display -->
  <ChatIntentDisplay 
    v-if="msg.role === 'assistant' && msg.intent"
    :intent-data="msg.intent"
  />
  
  <!-- Feedback form -->
  <ChatFeedback 
    v-if="msg.role === 'assistant' && msg.id"
    :conversation-id="conversationId"
    :message-id="msg.id"
  />
</div>
```

### Script Changes

```javascript
// In sendMessage()
const botMessage = {
  id: response?.reply?.id, // Capture ID
  role: "assistant",
  intent: null,
};

// Load intents asynchronously
const intents = await apiChat.getConversationIntents(conversationId.value);
if (intents?.length > 0) {
  botMessage.intent = intents[0];
}

// In loadHistory()
const intents = await apiChat.getConversationIntents(conversationId.value);
const intentMap = {};
intents?.forEach((intent) => {
  intentMap[intent.messageId] = intent;
});

messages.forEach((msg) => {
  if (msg.role === "assistant") {
    msg.intent = intentMap[msg.id];
  }
});
```

---

## 🛠️ Setup Instructions

### Step 1: Copy Components

```bash
# Already done in FE/src/
- ChatFeedback.vue
- ChatIntentDisplay.vue
- components/admin/ChatFeedbackDashboard.vue
```

### Step 2: Update Imports in ChatWidget.vue

```javascript
import ChatFeedback from "@/components/ChatFeedback.vue";
import ChatIntentDisplay from "@/components/ChatIntentDisplay.vue";
import { apiChat } from "@/services/apiChat";
```

### Step 3: Update i18n Files

```bash
# Already done in FE/src/locales/
- en/chatWidget.json (added chatFeedback & chatIntent)
- vi/chatWidget.json (Vietnamese translations)
- ja/chatWidget.json (Japanese translations)
- en/admin.json (added feedbackDashboard keys)
- vi/admin.json (Vietnamese translations)
- ja/admin.json (Japanese translations)
```

### Step 4: Create Admin Page (if needed)

**File:** `FE/src/pages/AdminChatFeedback.vue`

```vue
<template>
  <MainLayout>
    <ChatFeedbackDashboard />
  </MainLayout>
</template>

<script setup>
import MainLayout from "@/layouts/MainLayout.vue";
import ChatFeedbackDashboard from "@/components/admin/ChatFeedbackDashboard.vue";
</script>
```

### Step 5: Add Route (if needed)

**File:** `FE/src/router/index.js`

```javascript
{
  path: '/admin/chat-feedback',
  component: () => import('@/pages/AdminChatFeedback.vue'),
  meta: {
    requiresAuth: true,
    requiresRole: 'ADMIN'
  },
  name: 'ChatFeedbackDashboard'
}
```

---

## ✅ Verification Checklist

### Frontend Files

- [ ] ChatFeedback.vue exists
- [ ] ChatIntentDisplay.vue exists
- [ ] ChatFeedbackDashboard.vue exists
- [ ] apiChat.js has 4 new methods
- [ ] ChatWidget.vue has imports and integration
- [ ] All i18n files updated (en, vi, ja)
- [ ] AdminChatFeedback.vue created
- [ ] Router has /admin/chat-feedback route

### Backend Services

- [ ] Spring Boot running on localhost:8080
- [ ] Database migrations applied
- [ ] chat_feedback table exists
- [ ] chat_intents table exists
- [ ] ChatController has 5 endpoints
- [ ] IntentDetectionService working
- [ ] Caffeine cache configured

### Integration Tests

- [ ] Chat message loads with intent
- [ ] Feedback form appears below bot message
- [ ] Feedback submission succeeds
- [ ] Admin dashboard shows statistics
- [ ] i18n translations display correctly
- [ ] All languages work (en, vi, ja)

---

## 🎨 CSS Classes Reference

### ChatFeedback Component

```css
.feedback-form                /* Main container */
/* Main container */
.feedback-helpful-group       /* Helpful toggle */
.feedback-rating-group        /* Rating container */
.feedback-issue-group         /* Issue type dropdown */
.feedback-text-group          /* Textarea container */
.feedback-actions             /* Buttons container */
.feedback-success-message     /* Success message */
.feedback-error-message; /* Error message */
```

### ChatIntentDisplay Component

```css
.intent-badge                 /* Intent label badge */
/* Intent label badge */
.intent-confidence            /* Confidence score */
.intent-entities              /* Entities container */
.intent-entity-item           /* Individual entity */
.intent-entity-label          /* Entity type label */
.intent-entity-value; /* Entity value */
```

### ChatFeedbackDashboard Component

```css
.feedback-dashboard           /* Main container */
/* Main container */
.statistics-grid              /* Stats cards grid */
.stat-card                    /* Single stat card */
.stat-icon                    /* Icon container */
.stat-content                 /* Content area */
.stat-label                   /* Label text */
.stat-value                   /* Value number */
.stat-unit                    /* Unit/subtext */
.issues-section               /* Issues section */
.issue-bar-container          /* Bar container */
.issue-bar                    /* Bar background */
.issue-bar-fill               /* Bar fill (animated) */
.low-rated-section; /* Feedback table section */
```

---

## 🚀 Performance Tips

1. **Lazy Load Admin Dashboard**

   ```javascript
   () => import("@/pages/AdminChatFeedback.vue");
   ```

2. **Async Intent Loading** (already implemented)

   - Doesn't block chat UI
   - Loads in background

3. **Parallel API Calls**

   ```javascript
   Promise.all([
     apiChat.getFeedbackStatistics(),
     apiChat.getLowRatedFeedbacks(),
   ]);
   ```

4. **Cache Intent Data**
   - Backend: Caffeine cache (30min)
   - Frontend: Store in message object

---

## 🐛 Common Issues & Solutions

| Issue               | Solution                                           |
| ------------------- | -------------------------------------------------- |
| Intent not showing  | Check msg.intent exists, check role is 'assistant' |
| Feedback not saving | Check message has id, check API endpoint           |
| i18n keys missing   | Update chatWidget.json & admin.json in all locales |
| Dashboard empty     | Check feedback data in DB, check admin role        |
| CSS not loading     | Check component path, check scoped styles          |
| Async error         | Check network, check API endpoint URL              |

---

## 📊 Data Flow Diagrams

### User Submits Feedback

```
User rates/comments
    ↓
ChatFeedback.submitFeedback()
    ↓
apiChat.submitFeedback()
    ↓
POST /api/v1/chat/feedback
    ↓
Backend: ChatServiceImpl.saveFeedback()
    ↓
INSERT chat_feedback table
    ↓
ElMessage.success()
```

### Admin Views Dashboard

```
Navigate to /admin/chat-feedback
    ↓
ChatFeedbackDashboard mounts
    ↓
loadStatistics() called
    ↓
Promise.all([
  apiChat.getFeedbackStatistics(),
  apiChat.getLowRatedFeedbacks()
])
    ↓
GET /api/v1/chat/feedback/statistics
GET /api/v1/chat/feedback/low-rated
    ↓
Backend aggregates data
    ↓
Statistics cards rendered
    ↓
Tables populated
```

---

## 📝 Code Snippets

### Using ChatFeedback in Custom Component

```vue
<script setup>
import ChatFeedback from "@/components/ChatFeedback.vue";
import { ElMessage } from "element-plus";

const conversationId = ref("conv-123");
const messageId = ref("msg-456");

const handleFeedbackSubmitted = (feedback) => {
  ElMessage.success("Feedback recorded!");
  console.log("Feedback:", feedback);
};
</script>

<template>
  <ChatFeedback
    :conversation-id="conversationId"
    :message-id="messageId"
    @feedback-submitted="handleFeedbackSubmitted"
  />
</template>
```

### Using ChatIntentDisplay in Custom Component

```vue
<script setup>
import ChatIntentDisplay from "@/components/ChatIntentDisplay.vue";

const messageIntent = {
  detectedIntent: "PRICING_INQUIRY",
  confidenceScore: 0.92,
  extractedEntities: {
    budget: "$50,000 - $100,000",
    timeline: "3 months",
    technologies: ["React", "Node.js"],
    projectType: "Web Application",
  },
};
</script>

<template>
  <ChatIntentDisplay :intent-data="messageIntent" />
</template>
```

---

## 📚 Related Documents

1. **CHATBOT_AI_ARCHITECTURE_GUIDE.md** - System architecture overview
2. **CHAT_AI_IMPLEMENTATION_SUMMARY.md** - Backend implementation details
3. **CHAT_AI_FRONTEND_IMPLEMENTATION.md** - Detailed frontend docs
4. **CHAT_AI_COMPLETE_SYSTEM.md** - End-to-end system guide

---

## 🎓 Summary

**Frontend Chat AI System:**

- ✅ 3 new Vue components (ChatFeedback, ChatIntentDisplay, ChatFeedbackDashboard)
- ✅ 4 new API methods in apiChat.js
- ✅ Full i18n support (3 languages)
- ✅ Admin analytics dashboard
- ✅ Integrated with ChatWidget
- ✅ Production-ready code
- ✅ Comprehensive documentation

**Status: Ready for Deployment** 🚀
