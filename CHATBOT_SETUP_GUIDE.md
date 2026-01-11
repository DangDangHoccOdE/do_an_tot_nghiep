# 🤖 Hướng Dẫn Tích Hợp Chat Bot AI

## ✨ Tính Năng Đã Triển Khai

- ✅ **Chat Widget** ở góc phải dưới với animation đẹp mắt
- ✅ **Chỉ hiển thị** khi người dùng đã đăng nhập
- ✅ **AI Bot tư vấn** về dự án IT (quy trình, thời gian, chi phí, công nghệ)
- ✅ **Câu hỏi nhanh** (Quick Questions) để bắt đầu trò chuyện
- ✅ **Typing indicator** khi bot đang trả lời
- ✅ **Hỗ trợ 3 ngôn ngữ**: Tiếng Việt, English, 日本語
- ✅ **Responsive** trên mọi thiết bị

---

## 🚀 Cách Sử Dụng

### Bước 1: Chọn AI Provider

Mở file `FE/src/components/common/ChatWidget.vue` và chọn AI provider:

```javascript
// Dòng 47-48: Thay đổi AI_PROVIDER
const AI_PROVIDER = "gemini"; // Chọn 'gemini' (FREE) hoặc 'openai' (PAID)
```

---

## 🔑 Bước 2: Lấy API Key

### Option 1: Google Gemini (KHUYÊN DÙNG - MIỄN PHÍ)

**Ưu điểm:**

- ✅ **Miễn phí** với 15 requests/phút
- ✅ **Nhanh** - phản hồi ~1-2 giây
- ✅ **Chất lượng cao** - Gemini Pro model
- ✅ **Dễ lấy API key**

**Cách lấy:**

1. Truy cập: https://makersuite.google.com/app/apikey
2. Đăng nhập Google Account
3. Click **"Create API key"**
4. Copy API key

**Cập nhật code:**

```javascript
// Dòng 53 trong ChatWidget.vue
const GEMINI_API_KEY = "AIzaSyXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"; // Thay bằng key của bạn
```

---

### Option 2: OpenAI GPT (TRẢ PHÍ - CHẤT LƯỢNG CAO NHẤT)

**Ưu điểm:**

- ✅ **Chất lượng tốt nhất** - GPT-3.5-turbo
- ✅ **Ổn định** - infra mạnh mẽ
- ⚠️ **Có phí**: ~$0.002 / 1000 tokens (~0.5₫/tin nhắn)

**Cách lấy:**

1. Truy cập: https://platform.openai.com/api-keys
2. Đăng ký account (cần thẻ visa/credit card)
3. Click **"Create new secret key"**
4. Copy API key

**Cập nhật code:**

```javascript
// Dòng 50 trong ChatWidget.vue
const AI_PROVIDER = "openai"; // Đổi sang 'openai'

// Dòng 54 trong ChatWidget.vue
const OPENAI_API_KEY = "sk-proj-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"; // Thay bằng key của bạn
```

---

## 📊 So Sánh Chi Tiết

| Tiêu chí        | Google Gemini        | OpenAI GPT        |
| --------------- | -------------------- | ----------------- |
| **Giá**         | MIỄN PHÍ             | $0.002/1K tokens  |
| **Tốc độ**      | 1-2 giây             | 1-3 giây          |
| **Chất lượng**  | Rất tốt (9/10)       | Xuất sắc (10/10)  |
| **Giới hạn**    | 15 req/phút          | Không giới hạn\*  |
| **Setup**       | Dễ (chỉ cần Google)  | Khó (cần thẻ)     |
| **Khuyên dùng** | ✅ **Dự án nhỏ/vừa** | ✅ Production lớn |

\* _Giới hạn tùy plan_

---

## 🎨 Tùy Chỉnh Bot

### Thay đổi System Prompt (Cách bot trả lời)

Mở `ChatWidget.vue` dòng 60-80:

```javascript
const systemPrompt = `Bạn là trợ lý AI tư vấn dự án IT...

Khi khách hàng hỏi về giá:
- Dự án nhỏ (1-3 tháng): 50-150 triệu VNĐ
- Dự án trung bình (3-6 tháng): 150-500 triệu VNĐ
- Dự án lớn (6-12 tháng): 500 triệu - 2 tỷ VNĐ

// ⬇️ CHỈNH SỬA TẠI ĐÂY để bot trả lời theo cách bạn muốn
`;
```

### Thay đổi Câu Hỏi Nhanh

Sửa trong `FE/src/locales/vi/chatWidget.json`:

```json
{
  "chatWidget": {
    "quickQ1": "Câu hỏi mẫu 1 của bạn?",
    "quickQ2": "Câu hỏi mẫu 2 của bạn?",
    "quickQ3": "Câu hỏi mẫu 3 của bạn?",
    "quickQ4": "Câu hỏi mẫu 4 của bạn?"
  }
}
```

### Thay đổi Màu Sắc

Sửa trong `ChatWidget.vue` phần `<style>`:

```css
/* Dòng 377 - Màu gradient chính */
background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

/* Thay bằng màu khác, ví dụ: */
background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); /* Hồng */
background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); /* Xanh dương */
background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); /* Xanh lá */
```

---

## 🧪 Test Chat Bot

1. **Chạy frontend:**

   ```bash
   cd FE
   npm run dev
   ```

2. **Đăng nhập** vào hệ thống

3. **Kiểm tra góc phải dưới** - sẽ thấy nút chat tròn màu tím

4. **Click vào** để mở chat window

5. **Thử hỏi:**
   - "Tôi muốn làm một website bán hàng, mất bao lâu?"
   - "Chi phí cho app mobile quản lý nhân sự?"
   - "Công nghệ nào phù hợp cho CRM?"

---

## ⚠️ Troubleshooting

### 1. "Vui lòng thay thế API Key"

- ✅ Check xem đã thay `YOUR_GEMINI_API_KEY_HERE` chưa
- ✅ API key phải là chuỗi dài ~40 ký tự

### 2. "API Error" khi gửi tin nhắn

- ✅ Check kết nối internet
- ✅ Mở Console (F12) xem lỗi chi tiết
- ✅ Gemini: check quota tại https://makersuite.google.com
- ✅ OpenAI: check billing tại https://platform.openai.com/account/billing

### 3. Chat không hiện

- ✅ Đảm bảo đã đăng nhập (check `authStore.isAuthenticated`)
- ✅ Check Console xem có lỗi import không

### 4. Phản hồi quá chậm

- ✅ Đổi sang Gemini (nhanh hơn OpenAI)
- ✅ Giảm `maxOutputTokens` trong code (dòng 168)

---

## 🎯 Tips Tối Ưu

### 1. Tiết kiệm API Calls

Thêm cache cho câu hỏi phổ biến:

```javascript
// Thêm vào ChatWidget.vue
const cachedAnswers = {
  "chi phí": "Dự án thường từ 50 triệu - 2 tỷ tùy quy mô...",
  "thời gian": "Thường từ 1-12 tháng tùy độ phức tạp...",
};
```

### 2. Giới Hạn Độ Dài Phản Hồi

```javascript
// Dòng 168 - giảm maxOutputTokens
maxOutputTokens: 512; // Từ 1024 → 512 (nhanh hơn, rẻ hơn)
```

### 3. Thêm Phân Tích

```javascript
// Track số lượng tin nhắn
console.log(`User asked: ${userInput}`);
// Gửi lên backend để phân tích xu hướng câu hỏi
```

---

## 📱 Alternative Solutions

Nếu không muốn tự host AI:

1. **Tidio** - https://www.tidio.com (Free tier tốt)
2. **Tawk.to** - https://www.tawk.to (Miễn phí hoàn toàn)
3. **Crisp** - https://crisp.chat (Free + có AI addon)
4. **Dialogflow** - https://dialogflow.cloud.google.com (Google's chatbot platform)

---

## 📞 Liên Hệ Nếu Cần Hỗ Trợ

- Email: support@yourcompany.com
- Hotline: 1900 xxxx

---

**Chúc bạn triển khai thành công! 🎉**
