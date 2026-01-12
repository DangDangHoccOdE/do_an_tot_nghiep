import { get, post } from '@/utils/http'

export const apiChat = {
  ask: (payload) => post('/chat/ask', payload),
  getMessages: (conversationId) => get(`/chat/conversations/${conversationId}/messages`),
  getConversations: (params) => get('/chat/conversations', params),
  ingest: (payload) => post('/chat/ingest', payload),
  syncDomain: () => post('/chat/sync-domain')
}
