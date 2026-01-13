# Chat Feedback UI - Visual Guide

## Before vs After

### BEFORE:

```
┌─────────────────────────────────┐
│ Chat Feedback Panel             │
├─────────────────────────────────┤
│ Phản hồi có hữu ích không?     │  ← Label always visible
│ [Có]  [Không]                   │
│                                 │
│ Đánh giá phản hồi này          │  ← Separate row
│ ★ ★ ★ ★ ☆ (show-text)          │  ← Shows text like "4 sao"
│                                 │
│ → Form expands below (always)   │
│ Loại vấn đề:                    │
│ [Select issue type with labels] │  ← Mixed languages
│ Inaccurate (Không chính xác)   │
│ Irrelevant (Không liên quan)   │
│                                 │
│ [Submit] [Cancel]               │
└─────────────────────────────────┘
```

### AFTER:

```
┌─────────────────────────────────┐
│ Chat Feedback Panel             │
├─────────────────────────────────┤
│ Đánh giá:  ★ ★ ★ ★ ☆ ← Compact │
│            (no text label)       │
│                                 │
│ ↓ Form expands on rating        │
│ Phản hồi có hữu ích không? ×    │
│ [Có]  [Không]                   │
│                                 │
│ Loại vấn đề:                    │
│ [-- Chọn loại vấn đề --]        │ ← Pure Vietnamese
│   Không chính xác               │    (or English/Japanese)
│   Không liên quan               │
│   Không đủ thông tin            │
│   Thô lỗ                        │
│   Khác                          │
│                                 │
│ Phản hồi thêm:                  │
│ [Textarea - 2 rows]             │
│                                 │
│ [Gửi phản hồi] [Hủy]            │
└─────────────────────────────────┘
```

---

## Layout Flow

### Step 1: Initial State

```
Card Background: #f5f7fa
┌───────────────────────────┐
│ Đánh giá:  ★ ★ ★ ★ ☆     │  ← Interactive, compact
│           (3.5 stars)      │
└───────────────────────────┘
```

### Step 2: User Clicks/Selects Rating

```
Form Auto-Expands with Smooth Animation
┌───────────────────────────────┐
│ Đánh giá:  ★ ★ ★ ★ ☆         │
│                              │
│ ↓ collapse-transition        │
│                              │
│ Phản hồi có hữu ích không?  │
│ [✓ Có]  [Không]             │  ← Highlighted on selection
│                              │
│ Loại vấn đề:                │
│ [-- Chọn loại vấn đề --] ✕  │  ← Clearable select
│                              │
│ Phản hồi thêm:              │
│ [Text area - 2 rows]        │
│ Show word limit             │
│                              │
│ [Gửi phản hồi] [Hủy]        │
└───────────────────────────────┘
```

### Step 3: After Submission

```
Success Alert Appears
┌───────────────────────────────┐
│ ✓ Cảm ơn bạn đã phản hồi!    │  ← Green alert
└───────────────────────────────┘

(Auto-reset after 3 seconds)
```

---

## Component Structure

```vue
<template>
  <div class="chat-feedback">
    <!-- Row 1: Rating Stars (Always Visible) -->
    <div class="rating-section">
      <span class="rating-label">Đánh giá:</span>
      <el-rate
        v-model="feedback.rating"
        size="small"
        allow-half
        @change="onRatingChange"
        ←
        Triggers
        expansion
      />
    </div>

    <!-- Row 2: Expandable Form -->
    <el-collapse-transition>
      <div v-if="isExpanded" class="feedback-form">
        <!-- Helpful/Not Helpful -->
        <div class="feedback-section">
          <span>Phản hồi có hữu ích không?</span>
          <div class="button-group">
            <el-button :type="feedback.isHelpful === true ? 'success' : ''"
              >Có</el-button
            >
            <el-button :type="feedback.isHelpful === false ? 'danger' : ''"
              >Không</el-button
            >
          </div>
        </div>

        <!-- Issue Type (with i18n) -->
        <div class="form-group">
          <label>Loại vấn đề</label>
          <el-select>
            <el-option
              :label="t('chatFeedback.inaccurate')"
              ←
              Dynamic
              i18n
              value="INACCURATE"
            />
            <!-- ... more options ... -->
          </el-select>
        </div>

        <!-- Feedback Text -->
        <div class="form-group">
          <label>Phản hồi thêm</label>
          <el-input type="textarea" :rows="2" />
        </div>

        <!-- Actions -->
        <div class="form-actions">
          <el-button type="primary">Gửi phản hồi</el-button>
          <el-button>Hủy</el-button>
        </div>
      </div>
    </el-collapse-transition>

    <!-- Success Message -->
    <el-alert v-if="showSuccess" type="success" />
  </div>
</template>

<script setup>
// isExpanded computed property
const isExpanded = computed(() => {
  return feedback.value.isHelpful !== null || feedback.value.rating !== null;
  // Form shows if user selects rating OR helpful/not helpful
});

// Auto-expand on rating change
const onRatingChange = () => {
  // isExpanded will automatically update
  // due to computed property reactivity
};
</script>
```

---

## Styling Improvements

### Colors

- Background: `#f5f7fa` (light blue-gray)
- Rating label: `#606266` (dark gray)
- Success: Default green (Element Plus)

### Spacing

- Padding: 12px (compact for chat widget)
- Gap between sections: 10px
- Form border-top: 1px solid `#dcdfe6`

### Responsive

- Mobile: Adapts within chat widget (380px)
- Touch-friendly button sizes
- Textarea adjusts to content

---

## Interaction States

### Rating (No Label Display)

```
☆ ☆ ☆ ☆ ☆  ← Empty state
★ ★ ★ ☆ ☆  ← 3 stars (no "3 sao" text)
★ ★ ★ ★ ★  ← 5 stars (no "5 sao" text)
★ ★ ★ ½ ☆  ← 3.5 stars (no text)
```

### Helpful Buttons

```
[Có]  [Không]         ← Initial state (plain)
[✓ Có]  [Không]       ← "Có" selected (green highlight)
[Có]  [✓ Không]       ← "Không" selected (red highlight)
[Có]  [Không]         ← Deselected (back to plain)
```

### Issue Type Select

```
[-- Chọn loại vấn đề --]
 ├─ Không chính xác
 ├─ Không liên quan
 ├─ Không đủ thông tin
 ├─ Thô lỗ
 └─ Khác
```

---

## Data Structure

```javascript
feedback = {
  conversationId: "uuid",
  messageId: "uuid",
  isHelpful: null, // null | true | false
  rating: null, // null | 1-5 (with half)
  issueType: null, // null | "INACCURATE" | "IRRELEVANT" | etc
  feedbackText: "", // user text, max 500 chars
};
```

---

## Key Changes Summary

✅ **Removed**:

- Combined button rows
- Rating text display ("4 sao")
- Hardcoded issue type labels with mixed languages

✅ **Added**:

- Compact rating-only initial state
- Auto-expand form on user interaction
- Proper i18n for all issue types
- Better visual hierarchy and spacing
- Improved form layout with sections

✅ **Improved**:

- UX flow (clear steps)
- Visual design (cleaner, more intuitive)
- Accessibility (better button states)
- i18n (dynamic translation)
- Backend type safety (enum)
- Database integrity (constraints)
