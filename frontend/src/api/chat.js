import service from './request'
import { getToken } from '@/utils/auth'

const baseURL = import.meta.env.DEV ? 'http://localhost:8080' : '/api'

/**
 * 发送消息（普通模式）
 * @param {string} content - 消息内容
 * @param {object} ctx - 可选上下文 { orderId, merchantId }
 */
export function sendMessage(content, ctx = {}) {
  return service.post('/api/chat/send', {
    content,
    orderId: ctx.orderId || null,
    merchantId: ctx.merchantId || null,
    // 注意： userRole 已不再传递，服务端从 JWT 中解析用户角色
  })
}

/**
 * 获取会话历史
 */
export function getChatHistory() {
  return service.get('/api/chat/history')
}

/**
 * 清空会话
 */
export function clearChatSession() {
  return service.delete('/api/chat/session')
}

/**
 * 发送消息（SSE 流式模式）
 * 返回一个 { stream, abort } 对象
 * @param {string} content - 消息内容
 * @param {object} ctx - 可选上下文 { orderId, merchantId }
 * @param {function} onChunk - 每收到一个 token 的回调 (chunk: string) => void
 * @param {function} onDone  - 流结束的回调 () => void
 * @param {function} onError - 错误回调 (err) => void
 */
export function sendMessageStream(content, ctx = {}, onChunk, onDone, onError) {
  const controller = new AbortController()
  const token = getToken()

  fetch(`${baseURL}/api/chat/stream`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify({
      content,
      orderId: ctx.orderId || null,
      merchantId: ctx.merchantId || null,
      // 注意： userRole 已不再传递，服务端从 JWT 中解析用户角色
    }),
    signal: controller.signal,
  })
    .then(async (response) => {
      if (!response.ok) {
        const text = await response.text()
        onError?.(new Error(text || `HTTP ${response.status}`))
        return
      }
      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let lineBuffer = '' // 跨 chunk 行缓冲区

      const processLine = (line) => {
        if (line.startsWith('data:')) {
          const chunk = line.slice(5)
          // 不要 trim，保留有意义的空格；只跳过 [DONE] 标记
          if (chunk.trim() !== '[DONE]' && chunk !== '') {
            onChunk?.(chunk)
          }
        }
      }

      const read = async () => {
        try {
          while (true) {
            const { done, value } = await reader.read()
            if (done) {
              // 处理缓冲区中残留的最后一行
              if (lineBuffer.trim()) {
                processLine(lineBuffer)
              }
              onDone?.()
              break
            }
            const text = decoder.decode(value, { stream: true })
            // 将新数据追加到缓冲区
            lineBuffer += text
            // 按换行分割，最后一段可能不完整，留在缓冲区
            const parts = lineBuffer.split('\n')
            // 最后一个元素可能是不完整的行，保留
            lineBuffer = parts.pop() || ''
            // 处理完整的行
            for (const part of parts) {
              const trimmed = part.trim()
              if (trimmed) {
                processLine(trimmed)
              }
            }
          }
        } catch (err) {
          if (err.name !== 'AbortError') {
            onError?.(err)
          }
        }
      }
      read()
    })
    .catch((err) => {
      if (err.name !== 'AbortError') {
        onError?.(err)
      }
    })

  return { abort: () => controller.abort() }
}
