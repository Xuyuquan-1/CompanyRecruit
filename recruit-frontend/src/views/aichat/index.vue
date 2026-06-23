<template>
  <div class="aichat-page">
    <el-card shadow="never" class="chat-container">
      <div class="chat-header">
        <span>🤖 AI 智能助手</span>
        <el-button text type="danger" size="small" @click="handleClear">清空对话</el-button>
      </div>
      <div class="chat-messages" ref="msgContainer">
        <div v-if="messages.length === 0" class="welcome">
          <p>你好！我是 HR 招聘管理系统智能助手 👋</p>
          <p>可以问我：岗位怎么发布？如何上传简历？面试流程是怎样的？</p>
        </div>
        <div v-for="(msg, idx) in messages" :key="idx" :class="['msg-row', msg.role]">
          <div class="msg-bubble">
            <div class="msg-role">{{ msg.role === 'user' ? '你' : 'AI助手' }}</div>
            <div class="msg-text" v-html="formatMsg(msg.content)"></div>
          </div>
        </div>
      </div>
      <div class="chat-input">
        <el-input v-model="question" placeholder="输入你的问题..." @keyup.enter="sendMsg" :disabled="sending">
          <template #append>
            <el-button type="primary" @click="sendMsg" :loading="sending">发送</el-button>
          </template>
        </el-input>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { chat, getChatHistory, clearChatHistory } from '../../api/aichat'

const question = ref(''), sending = ref(false), messages = ref([]), msgContainer = ref(null)

onMounted(async () => {
  try {
    const res = await getChatHistory()
    const list = res.data || []
    list.forEach(h => {
      messages.value.push({ role: 'user', content: h.question })
      messages.value.push({ role: 'ai', content: h.answer })
    })
  } catch {}
  scrollBottom()
})

async function sendMsg() {
  const q = question.value.trim()
  if (!q || sending.value) return
  messages.value.push({ role: 'user', content: q })
  question.value = ''
  sending.value = true
  try {
    const res = await chat(q)
    messages.value.push({ role: 'ai', content: res.data.answer })
  } catch {
    messages.value.push({ role: 'ai', content: '抱歉，AI服务暂不可用，请稍后重试。' })
  }
  sending.value = false
  nextTick(scrollBottom)
}

async function handleClear() {
  try { await clearChatHistory(); messages.value = []; ElMessage.success('已清除') } catch {}
}

function scrollBottom() {
  nextTick(() => {
    if (msgContainer.value) msgContainer.value.scrollTop = msgContainer.value.scrollHeight
  })
}

function formatMsg(text) {
  return (text || '').replace(/\n/g, '<br>')
}
</script>

<style scoped>
.chat-container { height:calc(100vh - 140px); display:flex; flex-direction:column }
.chat-header { display:flex; justify-content:space-between; align-items:center; padding-bottom:12px; border-bottom:1px solid #ebeef5; font-size:16px; font-weight:bold }
.chat-messages { flex:1; overflow-y:auto; padding:16px 0 }
.welcome { text-align:center; color:#909399; margin-top:100px; line-height:2 }
.msg-row { margin-bottom:16px; display:flex }
.msg-row.user { justify-content:flex-end }
.msg-row.ai { justify-content:flex-start }
.msg-bubble { max-width:75%; padding:12px 16px; border-radius:12px }
.msg-row.user .msg-bubble { background:#409EFF; color:#fff; border-bottom-right-radius:4px }
.msg-row.ai .msg-bubble { background:#f4f4f5; color:#303133; border-bottom-left-radius:4px }
.msg-role { font-size:12px; margin-bottom:4px; opacity:0.7 }
.msg-text { line-height:1.6; white-space:pre-wrap }
.chat-input { padding-top:12px; border-top:1px solid #ebeef5 }
</style>