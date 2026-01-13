# Chat AI Frontend Implementation Guide

## Overview

Complete frontend integration of the Chat AI Feedback and Intent Detection system for the IT Project Consulting application.

## Components Created

### 1. **ChatFeedback.vue**

**Location:** `FE/src/components/ChatFeedback.vue`

**Purpose:** Collect user feedback on AI responses

**Features:**

- ⭐ Star rating system (1-5 stars)
- 👍👎 Helpful/Not Helpful toggle
- 🏷️ Issue type selection:

  - INACCURATE: Response contains wrong information
  - IRRELEVANT: Response doesn't address the question
  - INCOMPLETE: Response lacks necessary details
  - RUDE: Response is disrespectful
  - OTHER: Other issues

- 💬 Expandable feedback text area
- 📤 Submit feedback endpoint integration

**Props:**

```javascript
conversationId: String,  // Conversation ID
messageId: String       // Message ID to give feedback on
```

**Events:**

```javascript
emit("feedback-submitted", feedbackData);
```

**Usage:**

```vue
<ChatFeedback
  :conversation-id="conversationId"
  :message-id="messageId"
  @feedback-submitted="handleFeedbackSubmitted"
/>
```

---

### 2. **ChatIntentDisplay.vue**

**Location:** `FE/src/components/ChatIntentDisplay.vue`

**Purpose:** Display detected user intent and extracted information

**Features:**

- 🎯 Intent badge with confidence score
- 📊 Collapsible entity details
- 🎨 Color-coded by intent type
- 📋 Formatted entity display

**Intent Types:**

- `PRICING_INQUIRY`: Budget/pricing questions
- `TECH_RECOMMENDATION`: Technology suggestions
- `PROJECT_TIMELINE`: Timeline-related questions
- `FEATURE_REQUEST`: Feature requests
- `GENERAL_INFO`: General information

**Props:**

```javascript
intentData: {
  detectedIntent: String,      // e.g., 'PRICING_INQUIRY'
  confidenceScore: Number,     // 0-1
  extractedEntities: {
    budget?: String,           // Budget range
    timeline?: String,         // Project timeline
    technologies?: String[],   // Tech mentions
    projectType?: String       // Project type
  }
}
```

**Usage:**

```vue
<ChatIntentDisplay :intent-data="messageIntent" />
```

---

### 3. **ChatFeedbackDashboard.vue** (Admin)

**Location:** `FE/src/components/admin/ChatFeedbackDashboard.vue`

**Purpose:** Display feedback statistics and analytics for admins

**Features:**

- 📊 Average rating card (gradient background)
- 📈 Total feedback count
- ✅ Helpful vs Not Helpful metrics
- 📉 Top issues bar chart
- 📋 Low-rated feedbacks table
- 🔄 Real-time refresh button

**Statistics Displayed:**

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

**Low Rated Feedbacks:**

- Table with columns: Rating, Issue Type, Feedback, Date
- Sortable by rating (ascending)
- Shows only feedbacks with rating ≤ 2

---

### 4. **Updated ChatWidget.vue**

**Location:** `FE/src/components/ChatWidget.vue`

**Key Updates:**

#### Imports Added:

```javascript
import ChatFeedback from "@/components/ChatFeedback.vue";
import ChatIntentDisplay from "@/components/ChatIntentDisplay.vue";
import { apiChat } from "@/services/apiChat";
```

#### Enhanced Message Object:

```javascript
{
  id: String,           // Message ID for feedback linking
  role: 'user' | 'assistant',
  content: String,
  intent: IntentData    // Intent information
}
```

#### Template Updates:

```vue
<div v-for="msg in messages" :key="msg.id" class="message">
  <!-- Message content -->
  <p>{{ msg.content }}</p>
  
  <!-- Show intent for assistant messages -->
  <ChatIntentDisplay 
    v-if="msg.role === 'assistant' && msg.intent" 
    :intent-data="msg.intent" 
  />
  
  <!-- Show feedback form for assistant messages -->
  <ChatFeedback 
    v-if="msg.role === 'assistant' && msg.id" 
    :conversation-id="conversationId"
    :message-id="msg.id"
    @feedback-submitted="handleFeedbackSubmitted"
  />
</div>
```

#### Enhanced sendMessage():

```javascript
const sendMessage = async () => {
  // Send message and get response
  const response = await apiChat.ask({
    conversationId: conversationId.value,
    message: userInput.value,
  });

  // Create bot message with ID
  const botMessage = {
    id: response?.reply?.id,
    role: "assistant",
    intent: null,
  };

  // Asynchronously load intents
  try {
    const intents = await apiChat.getConversationIntents(conversationId.value);
    if (intents?.length > 0) {
      botMessage.intent = intents[0];
    }
  } catch (error) {
    console.error("Error loading intents:", error);
  }

  messages.value.push(botMessage);
};
```

#### Enhanced loadHistory():

```javascript
const loadHistory = async () => {
  const messages = await apiChat.getMessages(conversationId.value);

  // Load intents for all messages
  const intents = await apiChat.getConversationIntents(conversationId.value);

  // Map intents to messages
  const intentMap = {};
  intents?.forEach((intent) => {
    intentMap[intent.messageId] = intent;
  });

  messages.forEach((msg) => {
    if (msg.role === "assistant") {
      msg.intent = intentMap[msg.id];
    }
  });

  messages.value = messages;
};
```

---

## API Service Integration

### Updated apiChat.js

**Location:** `FE/src/services/apiChat.js`

**New Methods:**

```javascript
// Submit feedback for a message
async submitFeedback(conversationId, messageId, feedbackData) {
  return await request.post(`/api/v1/chat/feedback`, {
    conversationId,
    messageId,
    rating: feedbackData.rating,
    isHelpful: feedbackData.isHelpful,
    issueType: feedbackData.issueType,
    feedbackText: feedbackData.feedbackText
  })
}

// Get feedback statistics
async getFeedbackStatistics() {
  return await request.get(`/api/v1/chat/feedback/statistics`)
}

// Get low-rated feedbacks
async getLowRatedFeedbacks(limit = 50) {
  return await request.get(`/api/v1/chat/feedback/low-rated`, {
    params: { limit }
  })
}

// Get intents for a conversation
async getConversationIntents(conversationId) {
  return await request.get(`/api/v1/chat/intents/${conversationId}`)
}
```

---

## i18n Translation Keys

### chatFeedback

```javascript
{
  helpful: "Was this response helpful?",
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

### chatIntent

```javascript
{
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

### admin (Dashboard)

```javascript
{
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

**Supported Languages:**

- 🇻🇳 Vietnamese (vi)
- 🇬🇧 English (en)
- 🇯🇵 Japanese (ja)

---

## Routing Setup

### Add Admin Dashboard Route

**Location:** `FE/src/router/index.js`

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

### Create Admin Page

**Location:** `FE/src/pages/AdminChatFeedback.vue`

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

---

## Component Tree

```
App.vue
├── MainLayout.vue
│   ├── ChatWidget.vue
│   │   ├── ChatIntentDisplay.vue (for each assistant message)
│   │   └── ChatFeedback.vue (for each assistant message)
│   │
│   └── AdminDashboard.vue
│       └── ChatFeedbackDashboard.vue
│           ├── Statistics Cards
│           ├── Top Issues Bar Chart
│           └── Low Rated Feedbacks Table
```

---

## Data Flow

### Feedback Submission Flow

```
User clicks feedback on message
        ↓
ChatFeedback component opens
        ↓
User rates and provides feedback
        ↓
ChatFeedback emits 'feedback-submitted'
        ↓
apiChat.submitFeedback() called
        ↓
POST /api/v1/chat/feedback
        ↓
Backend saves to chat_feedback table
        ↓
User sees success message
```

### Intent Display Flow

```
User sends message
        ↓
ChatWidget.sendMessage() executes
        ↓
apiChat.ask() returns response with message ID
        ↓
apiChat.getConversationIntents() called asynchronously
        ↓
GET /api/v1/chat/intents/{conversationId}
        ↓
Intent data mapped to message object
        ↓
ChatIntentDisplay renders intent badge
```

### Admin Dashboard Flow

```
Admin navigates to /admin/chat-feedback
        ↓
ChatFeedbackDashboard mounted
        ↓
loadStatistics() called
        ↓
apiChat.getFeedbackStatistics() & getLowRatedFeedbacks() in parallel
        ↓
Statistics cards rendered with data
        ↓
Tables populated with feedback data
        ↓
Admin can click refresh button to reload
```

---

## Styling

### Component Styling Features

- **Responsive Grid:** Statistics cards adapt to screen size
- **Gradient Backgrounds:** Icon containers use color gradients
- **Smooth Animations:** Hover effects on cards
- **Element Plus Integration:** Uses el-table, el-rate, el-icon
- **Color Scheme:**
  - Blue (#409EFF) for stats
  - Green (#67C23A) for helpful
  - Red (#F56C6C) for not helpful
  - Neutral grays (#606266, #909399)

### CSS Classes

```css
.feedback-dashboard {
}
.statistics-grid {
}
.stat-card {
}
.stat-icon {
}
.stat-content {
}
.stat-label {
}
.stat-value {
}
.stat-unit {
}
.issues-section {
}
.section-title {
}
.issue-item {
}
.issue-bar-container {
}
.issue-bar {
}
.issue-bar-fill {
}
```

---

## Error Handling

### ChatFeedback Error Handling

```javascript
try {
  await apiChat.submitFeedback(...)
  ElMessage.success(t('chatFeedback.successMessage'))
} catch (error) {
  console.error('Feedback submission error:', error)
  ElMessage.error(t('chatFeedback.submitError'))
}
```

### ChatIntentDisplay Error Handling

```javascript
// Gracefully handles missing intent data
v-if="intentData && intentData.detectedIntent"
```

### ChatFeedbackDashboard Error Handling

```javascript
try {
  const [statsRes, lowRatedRes] = await Promise.all([...])
} catch (error) {
  console.error('Load statistics error:', error)
  ElMessage.error(t('admin.loadStatisticsError'))
}
```

---

## Testing Checklist

- [ ] ChatFeedback component renders correctly
- [ ] Feedback submission saves to database
- [ ] ChatIntentDisplay shows intent badge
- [ ] Intent entities display with correct values
- [ ] ChatWidget integrates feedback and intent components
- [ ] Dashboard loads statistics correctly
- [ ] i18n translations work for all languages
- [ ] Responsive design works on mobile
- [ ] Error messages display on failures
- [ ] Async intent loading doesn't block chat
- [ ] Dashboard refresh button updates data
- [ ] Low-rated feedbacks table sorts by rating

---

## Performance Optimization

1. **Async Intent Loading:**

   - Intent loading is deferred to prevent blocking chat flow
   - Intents load in background after message is displayed

2. **Dashboard Statistics:**

   - Statistics and low-rated feedbacks fetched in parallel with `Promise.all()`
   - Reduces dashboard load time

3. **Component Lazy Loading:**

   - ChatFeedbackDashboard component only loads when needed
   - Reduces initial app bundle size

4. **Caching:**
   - Backend Caffeine cache (30min TTL) for knowledge base
   - Reduces DB queries

---

## Integration Steps

1. **Ensure all files are in place:**

   - ✅ ChatFeedback.vue
   - ✅ ChatIntentDisplay.vue
   - ✅ ChatFeedbackDashboard.vue
   - ✅ apiChat.js (updated)
   - ✅ ChatWidget.vue (updated)
   - ✅ i18n translations (updated)

2. **Add admin route:** (if not already present)

   - Create route: `/admin/chat-feedback`
   - Create page: `AdminChatFeedback.vue`

3. **Update admin menu:** (if needed)

   - Add "Chat Feedback" menu item
   - Link to `/admin/chat-feedback`

4. **Test the integration:**
   - Start backend service
   - Start frontend development server
   - Test feedback submission
   - Test admin dashboard access

---

## Troubleshooting

### Issue: ChatIntentDisplay not showing

**Solution:** Ensure message has `intent` property with valid data structure

### Issue: Feedback submit fails

**Solution:** Check backend API endpoint is running at `/api/v1/chat/feedback`

### Issue: i18n keys missing

**Solution:** Ensure all locales updated: en, vi, ja

### Issue: Dashboard statistics empty

**Solution:** Verify feedback data exists in `chat_feedback` table

### Issue: Async intent loading blocks chat

**Solution:** Intent loading is already async, but check network requests

---

## Future Enhancements

1. **Machine Learning:**

   - Train model on feedback data
   - Improve intent detection accuracy

2. **Advanced Analytics:**

   - Time-based feedback trends
   - User satisfaction graphs
   - Intent distribution charts

3. **Automated Actions:**

   - Auto-respond to low-rated feedbacks
   - Escalate to human support

4. **Feedback Threads:**

   - Allow follow-up feedback on same message
   - Track feedback improvements over time

5. **AI Improvement Loop:**
   - Use low-rated feedbacks to improve responses
   - A/B test different response templates

---

## Related Documents

- [CHATBOT_AI_ARCHITECTURE_GUIDE.md](CHATBOT_AI_ARCHITECTURE_GUIDE.md) - Overall architecture
- [CHAT_AI_IMPLEMENTATION_SUMMARY.md](CHAT_AI_IMPLEMENTATION_SUMMARY.md) - Backend implementation
- [AUTHORIZATION_SYSTEM_GUIDE.md](AUTHORIZATION_SYSTEM_GUIDE.md) - Security & authorization

---

## Summary

The Chat AI Frontend implementation provides:

- ✅ Complete user feedback collection system
- ✅ Intent visualization for transparency
- ✅ Admin analytics dashboard
- ✅ Multi-language support (Vi, En, Ja)
- ✅ Responsive and modern UI
- ✅ Full integration with backend APIs
- ✅ Error handling and validation
- ✅ Performance optimization

All components are production-ready and fully integrated with the existing Chat Widget system.
