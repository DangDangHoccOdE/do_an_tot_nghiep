import { get, post } from '@/utils/http'

export const apiChat = {
  // Chat endpoints
  ask: (payload) => post('/chat/ask', payload),
  getMessages: (conversationId) => get(`/chat/conversations/${conversationId}/messages`),
  getConversations: (params) => get('/chat/conversations', params),
  
  // Knowledge management
  ingest: (payload) => post('/chat/ingest', payload),
  syncDomain: () => post('/chat/sync-domain'),
  
  // Feedback endpoints
  submitFeedback: (payload) => post('/chat/feedback', payload),
  getFeedbackStatistics: () => get('/chat/feedback/statistics'),
  getLowRatedFeedbacks: () => get('/chat/feedback/low-rated'),
  
  // Intent endpoints
  getConversationIntents: (conversationId) => get(`/chat/conversations/${conversationId}/intents`)
}
