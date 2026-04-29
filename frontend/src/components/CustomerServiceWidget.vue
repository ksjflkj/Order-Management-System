<template>
  <!-- 悬浮触发按钮 -->
  <div
    v-if="!isOpen"
    class="cs-trigger"
    @click="openChat"
    title="智能客服"
  >
    <span class="cs-trigger-icon">{{ assistantIcon }}</span>
    <span class="cs-trigger-label">{{ currentRole === 'ADMIN' ? '助手' : currentRole === 'MERCHANT' ? '助手' : '客服' }}</span>
    <span v-if="unreadCount > 0" class="cs-badge">{{ unreadCount }}</span>
  </div>

  <!-- 聊天窗口 -->
  <div v-else class="cs-window">
    <!-- 头部 -->
    <div class="cs-header">
      <div class="cs-header-info">
        <div class="cs-avatar">{{ assistantIcon }}</div>
        <div>
          <div class="cs-name">{{ assistantName }}</div>
          <div class="cs-status">
            <span class="cs-status-dot"></span>
            在线
          </div>
        </div>
      </div>
      <div class="cs-header-actions">
        <button class="cs-btn-icon" title="清空会话" @click="handleClearSession">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 6h18M8 6V4h8v2M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6"/>
          </svg>
        </button>
        <button class="cs-btn-icon" title="最小化" @click="isOpen = false">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M5 12h14"/>
          </svg>
        </button>
      </div>
    </div>

    <!-- 消息列表 -->
    <div class="cs-messages" ref="messagesRef">
      <!-- 欢迎消息 -->
      <div v-if="messages.length === 0" class="cs-welcome">
        <div class="cs-welcome-icon">👋</div>
        <div class="cs-welcome-text" v-if="currentRole === 'ADMIN'">
          你好管理员！我是平台管理助手，可以查看运营数据、分析订单趋势、提供策略建议 💼
        </div>
        <div class="cs-welcome-text" v-else-if="currentRole === 'MERCHANT'">
          你好老板！我是你的运营助手，可以查看订单、分析数据、处理差评等，试试下方快捷入口 👇
        </div>
        <div class="cs-welcome-text" v-else-if="currentOrderId">
          我检测到你正在查看订单详情，可以为你解答关于这笔订单的任何问题哦～
        </div>
        <div class="cs-welcome-text" v-else>
          你好！我是小饿，可以帮你查询订单、查看优惠券、处理售后等，问我吧！
        </div>
        <div class="cs-quick-actions">
          <button
            v-for="q in quickQuestions"
            :key="q"
            class="cs-quick-btn"
            @click="sendQuickMessage(q)"
          >{{ q }}</button>
        </div>
      </div>

      <!-- 消息气泡 -->
      <div
        v-for="(msg, index) in messages"
        :key="index"
        class="cs-message"
        :class="msg.role"
      >
        <div v-if="msg.role === 'assistant'" class="cs-msg-avatar">{{ assistantIcon }}</div>
        <div class="cs-bubble-wrapper">
          <div class="cs-bubble" :class="msg.role">
            <template v-if="msg.role === 'assistant'">
              <div class="cs-md-content" v-html="renderMarkdown(msg.content)"></div>
              <span v-if="msg.streaming" class="cs-cursor">|</span>
            </template>
            <span v-else>{{ msg.content }}</span>
          </div>
          <div class="cs-msg-time">{{ formatTime(msg.createdAt) }}</div>
        </div>
      </div>

      <!-- 加载动画 -->
      <div v-if="isLoading" class="cs-message assistant">
        <div class="cs-msg-avatar">{{ assistantIcon }}</div>
        <div class="cs-bubble assistant cs-typing">
          <span></span><span></span><span></span>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="cs-input-area">
      <textarea
        ref="inputRef"
        v-model="inputText"
        class="cs-input"
        placeholder="输入您的问题..."
        :disabled="isLoading"
        @keydown.enter.exact.prevent="handleSend"
        @input="autoResize"
        rows="1"
      ></textarea>
      <button
        class="cs-send-btn"
        :disabled="!inputText.trim() || isLoading"
        @click="handleSend"
      >
        <svg v-if="!isLoading" viewBox="0 0 24 24" fill="currentColor">
          <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/>
        </svg>
        <div v-else class="cs-send-loading"></div>
      </button>
    </div>
    <div class="cs-input-hint">Enter 发送 · Shift+Enter 换行</div>
  </div>
</template>

<script setup>
import { ref, nextTick, computed } from 'vue'
import { useRoute } from 'vue-router'
import { marked } from 'marked'
import { sendMessageStream, getChatHistory, clearChatSession } from '@/api/chat'
import { useUserStore } from '@/stores/user'
import { ElMessageBox, ElMessage } from 'element-plus'

// 配置 marked：不使用段落换行（更紧凑）
marked.setOptions({
  breaks: true,   // 换行符转 <br>
  gfm: true,      // 支持 GitHub 风格 Markdown
})

// 将 Markdown 文本渲染为 HTML（实时调用）
function renderMarkdown(text) {
  if (!text) return ''
  // 预处理：确保列表项前有换行，让 marked 在流式中也能正确识别列表
  let processed = text
    // 有序列表：非换行符后紧跟的 "数字." 前面插入换行
    .replace(/([^\n])\s*(\d+\.\s)/g, '$1\n$2')
    // 无序列表：非换行符后紧跟的 "- " 或 "* " 前面插入换行
    .replace(/([^\n])\s*([-*]\s)/g, '$1\n$2')
  return marked.parse(processed)
}

const userStore = useUserStore()
const route = useRoute()

// 自动感知当前角色（根据路由前缀）
const currentRole = computed(() => {
  if (route.path.startsWith('/merchant')) return 'MERCHANT'
  if (route.path.startsWith('/admin')) return 'ADMIN'
  return 'USER'
})

// 自动从路由中感知是否在订单详情页
const currentOrderId = computed(() => {
  if (route.name === 'order-detail' && route.params.id) {
    return Number(route.params.id)
  }
  return null
})

// 客服名称和图标（三端不同身份）
const assistantName = computed(() => {
  if (currentRole.value === 'ADMIN') return '平台管理助手'
  if (currentRole.value === 'MERCHANT') return '商家运营助手'
  return '小饿智能客服'
})
const assistantIcon = computed(() => {
  if (currentRole.value === 'ADMIN') return '🛡️'
  if (currentRole.value === 'MERCHANT') return '📊'
  return '🍜'
})

const isOpen = ref(false)
const messages = ref([])
const inputText = ref('')
const isLoading = ref(false)
const unreadCount = ref(0)
const messagesRef = ref(null)
const inputRef = ref(null)

// 快捷问题（根据角色 + 页面动态显示）
const quickQuestions = computed(() => {
  if (currentRole.value === 'ADMIN') {
    return [
      '平台今日运营数据概览',
      '最近有哪些新订单？',
      '给我一些运营建议',
      '平台商家和用户统计',
    ]
  }
  if (currentRole.value === 'MERCHANT') {
    return [
      '今天有多少新订单？',
      '查看我的热销菜品',
      '有差评需要处理吗？',
      '怎么参加平台活动？',
    ]
  }
  if (currentOrderId.value) {
    return [
      '这个订单什么时候能送达？',
      '我想申请退款',
      '查看我所有的订单',
      '有可用的优惠券吗？',
    ]
  }
  return [
    '查询我最近的订单',
    '我有哪些可用优惠券？',
    '如何申请退款？',
    '你可以帮我做什么？',
  ]
})

const isLoggedIn = computed(() => !!userStore.userInfo && !userStore.isGuest)

// 打开聊天窗口
async function openChat() {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再使用智能客服')
    return
  }
  isOpen.value = true
  unreadCount.value = 0
  // 加载历史消息
  if (messages.value.length === 0) {
    await loadHistory()
  }
  await nextTick()
  scrollToBottom()
  inputRef.value?.focus()
}

// 加载历史消息
async function loadHistory() {
  try {
    const res = await getChatHistory()
    if (res.code === 200 && res.data?.length > 0) {
      messages.value = res.data.map(m => ({
        ...m,
        createdAt: m.createdAt ? new Date(m.createdAt) : new Date(),
      }))
    }
  } catch (e) {
    // 历史加载失败不影响正常使用
  }
}

// 发送消息
async function handleSend() {
  const text = inputText.value.trim()
  if (!text || isLoading.value) return

  // 二次检查：防止 token 过期后仍然发送
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再使用智能客服')
    isOpen.value = false
    return
  }

  // 推入用户消息
  messages.value.push({
    role: 'user',
    content: text,
    createdAt: new Date(),
  })
  inputText.value = ''
  await nextTick()
  scrollToBottom()
  resetInputHeight()

  // 推入空的 assistant 消息（流式填充）
  const aiMsgIndex = messages.value.length
  messages.value.push({
    role: 'assistant',
    content: '',
    createdAt: new Date(),
    streaming: true,
  })

  isLoading.value = true

  // 自动感知当前页面的上下文（仅传递辅助查询字段，角色由服务端从 JWT 中解析）
  const ctx = {
    orderId: currentOrderId.value,
    // userRole 已移除，后端不再接受前端传值
  }

  // 使用流式模式
  const { abort } = sendMessageStream(
    text,
    ctx,
    // onChunk — 直接追加原文，模板通过 renderMarkdown 实时渲染
    async (chunk) => {
      messages.value[aiMsgIndex].content += chunk
      await nextTick()
      scrollToBottom()
    },
    // onDone
    () => {
      messages.value[aiMsgIndex].streaming = false
      isLoading.value = false
      if (!isOpen.value) unreadCount.value++
    },
    // onError
    (err) => {
      console.error('客服请求失败:', err)
      messages.value[aiMsgIndex].content = '抱歉，网络出了点小问题，请稍后重试～'
      messages.value[aiMsgIndex].streaming = false
      isLoading.value = false
    },
  )
}


// 快捷问题点击
function sendQuickMessage(text) {
  inputText.value = text
  handleSend()
}

// 清空会话
async function handleClearSession() {
  try {
    await ElMessageBox.confirm('确定要清空当前对话吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await clearChatSession()
    messages.value = []
    ElMessage.success('会话已清空')
  } catch {
    // 用户取消
  }
}

// 自动滚到底部
function scrollToBottom() {
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

// textarea 自适应高度
function autoResize(e) {
  const el = e.target
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 120) + 'px'
}

function resetInputHeight() {
  if (inputRef.value) {
    inputRef.value.style.height = 'auto'
  }
}

// 格式化时间
function formatTime(date) {
  if (!date) return ''
  const d = new Date(date)
  const now = new Date()
  const diffMs = now - d
  if (diffMs < 60000) return '刚刚'
  if (diffMs < 3600000) return Math.floor(diffMs / 60000) + '分钟前'
  return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}
</script>

<style scoped>
/* ====== 悬浮触发按钮 ====== */
.cs-trigger {
  position: fixed;
  right: 24px;
  bottom: 80px;
  z-index: 9998;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  width: 56px;
  height: 64px;
  background: linear-gradient(135deg, #FFC300, #FF9500);
  border-radius: 16px;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(255, 195, 0, 0.45);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  justify-content: center;
  user-select: none;
}
.cs-trigger:hover {
  transform: translateY(-4px) scale(1.05);
  box-shadow: 0 8px 30px rgba(255, 195, 0, 0.6);
}
.cs-trigger-icon {
  font-size: 24px;
  line-height: 1;
}
.cs-trigger-label {
  font-size: 11px;
  font-weight: 600;
  color: #222;
  letter-spacing: 0.5px;
}
.cs-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  background: #ff4d4f;
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  border-radius: 8px;
  padding: 1px 5px;
  min-width: 16px;
  text-align: center;
}

/* ====== 聊天窗口 ====== */
.cs-window {
  position: fixed;
  right: 24px;
  bottom: 80px;
  z-index: 9999;
  width: 380px;
  height: 560px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 12px 60px rgba(0, 0, 0, 0.18), 0 0 0 1px rgba(0,0,0,0.06);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: cs-slide-up 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes cs-slide-up {
  from { opacity: 0; transform: translateY(24px) scale(0.95); }
  to   { opacity: 1; transform: translateY(0) scale(1); }
}

/* 头部 */
.cs-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background: linear-gradient(135deg, #FFC300, #FF9500);
  flex-shrink: 0;
}
.cs-header-info {
  display: flex;
  align-items: center;
  gap: 10px;
}
.cs-avatar {
  width: 38px;
  height: 38px;
  background: rgba(255,255,255,0.3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}
.cs-name {
  font-weight: 700;
  font-size: 14px;
  color: #222;
}
.cs-status {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #444;
  margin-top: 1px;
}
.cs-status-dot {
  width: 6px;
  height: 6px;
  background: #22c55e;
  border-radius: 50%;
  animation: cs-pulse 2s infinite;
}
@keyframes cs-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}
.cs-header-actions {
  display: flex;
  gap: 6px;
}
.cs-btn-icon {
  width: 30px;
  height: 30px;
  background: rgba(255,255,255,0.25);
  border: none;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
  padding: 6px;
  color: #333;
}
.cs-btn-icon:hover {
  background: rgba(255,255,255,0.5);
}
.cs-btn-icon svg {
  width: 16px;
  height: 16px;
}

/* 消息区 */
.cs-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px 14px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: #f8f8f8;
  scroll-behavior: smooth;
}
.cs-messages::-webkit-scrollbar { width: 4px; }
.cs-messages::-webkit-scrollbar-track { background: transparent; }
.cs-messages::-webkit-scrollbar-thumb { background: #ddd; border-radius: 4px; }

/* 欢迎区 */
.cs-welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px 10px;
  text-align: center;
}
.cs-welcome-icon { font-size: 40px; }
.cs-welcome-text {
  font-size: 14px;
  color: #555;
  line-height: 1.5;
}
.cs-quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  justify-content: center;
  margin-top: 4px;
}
.cs-quick-btn {
  padding: 5px 12px;
  background: #fff;
  border: 1.5px solid #FFC300;
  border-radius: 20px;
  font-size: 12px;
  color: #b88a00;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 500;
}
.cs-quick-btn:hover {
  background: #FFC300;
  color: #222;
}

/* 消息气泡 */
.cs-message {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  animation: cs-msg-in 0.2s ease-out;
}
@keyframes cs-msg-in {
  from { opacity: 0; transform: translateY(6px); }
  to   { opacity: 1; transform: translateY(0); }
}
.cs-message.user {
  flex-direction: row-reverse;
}
.cs-msg-avatar {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #FFC300, #FF9500);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}
.cs-bubble-wrapper {
  display: flex;
  flex-direction: column;
  gap: 3px;
  max-width: 72%;
}
.cs-message.user .cs-bubble-wrapper { align-items: flex-end; }
.cs-message.assistant .cs-bubble-wrapper { align-items: flex-start; }

.cs-bubble {
  padding: 10px 14px;
  border-radius: 16px;
  font-size: 13.5px;
  line-height: 1.6;
  word-break: break-word;
}

/* Markdown 渲染内容样式 */
.cs-md-content {
  word-break: break-word;
}
.cs-md-content :deep(p) {
  margin: 0 0 6px 0;
}
.cs-md-content :deep(p:last-child) {
  margin-bottom: 0;
}
.cs-md-content :deep(ol),
.cs-md-content :deep(ul) {
  margin: 4px 0;
  padding-left: 20px;
}
.cs-md-content :deep(li) {
  margin-bottom: 3px;
}
.cs-md-content :deep(strong) {
  font-weight: 600;
  color: #222;
}
.cs-md-content :deep(em) {
  font-style: italic;
}
.cs-md-content :deep(code) {
  background: #f5f5f5;
  padding: 1px 4px;
  border-radius: 3px;
  font-size: 12px;
  color: #e65100;
}
.cs-md-content :deep(h1),
.cs-md-content :deep(h2),
.cs-md-content :deep(h3),
.cs-md-content :deep(h4) {
  margin: 6px 0 4px;
  font-weight: 700;
  font-size: 14px;
  color: #222;
}
.cs-md-content :deep(blockquote) {
  margin: 4px 0;
  padding: 4px 10px;
  border-left: 3px solid #FFC300;
  background: #fffdf0;
  color: #666;
}
.cs-bubble.user {
  background: linear-gradient(135deg, #FFC300, #FF9500);
  color: #222;
  border-bottom-right-radius: 4px;
}
.cs-bubble.assistant {
  background: #fff;
  color: #333;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}
.cs-msg-time {
  font-size: 10px;
  color: #aaa;
}

/* 流式光标 */
.cs-cursor {
  display: inline-block;
  animation: cs-blink 0.8s infinite;
  color: #FFC300;
  font-weight: 300;
}
@keyframes cs-blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}


/* 打字动画（loading） */
.cs-typing {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 12px 16px !important;
  min-width: 54px;
}
.cs-typing span {
  width: 7px;
  height: 7px;
  background: #FFC300;
  border-radius: 50%;
  animation: cs-bounce 1.2s infinite;
}
.cs-typing span:nth-child(2) { animation-delay: 0.2s; }
.cs-typing span:nth-child(3) { animation-delay: 0.4s; }
@keyframes cs-bounce {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-8px); }
}

/* 输入区域 */
.cs-input-area {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  padding: 10px 14px 8px;
  background: #fff;
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
}
.cs-input {
  flex: 1;
  border: 1.5px solid #e8e8e8;
  border-radius: 12px;
  padding: 8px 12px;
  font-size: 13px;
  resize: none;
  outline: none;
  line-height: 1.5;
  font-family: inherit;
  color: #333;
  background: #fafafa;
  transition: border-color 0.2s;
  max-height: 120px;
  overflow-y: auto;
}
.cs-input:focus {
  border-color: #FFC300;
  background: #fff;
}
.cs-input:disabled { opacity: 0.6; cursor: not-allowed; }
.cs-send-btn {
  width: 38px;
  height: 38px;
  background: linear-gradient(135deg, #FFC300, #FF9500);
  border: none;
  border-radius: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  flex-shrink: 0;
  padding: 0;
}
.cs-send-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
.cs-send-btn:not(:disabled):hover {
  transform: scale(1.08);
  box-shadow: 0 4px 12px rgba(255, 195, 0, 0.5);
}
.cs-send-btn svg {
  width: 18px;
  height: 18px;
  fill: #222;
}
.cs-send-loading {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(34,34,34,0.2);
  border-top-color: #222;
  border-radius: 50%;
  animation: cs-spin 0.7s linear infinite;
}
@keyframes cs-spin {
  to { transform: rotate(360deg); }
}
.cs-input-hint {
  text-align: center;
  font-size: 10px;
  color: #bbb;
  padding-bottom: 8px;
  background: #fff;
  flex-shrink: 0;
}

/* 移动端响应式 */
@media (max-width: 480px) {
  .cs-window {
    right: 0;
    bottom: 0;
    width: 100%;
    height: 70vh;
    border-radius: 20px 20px 0 0;
  }
  .cs-trigger {
    right: 16px;
    bottom: 70px;
  }
}
</style>
