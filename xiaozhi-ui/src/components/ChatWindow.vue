<template>
  <div class="app-layout">
    <div class="sidebar">
      <div class="logo-section">
        <img src="@/assets/logo.png" alt="硅谷小智" width="160" height="160" />
        <span class="logo-text">硅谷小智（医疗版）</span>
      </div>
      <el-button class="new-chat-button" @click="newChat">
        <i class="fa-solid fa-plus"></i>
        &nbsp;新会话
      </el-button>
      
      <!-- 会话列表 -->
      <div class="session-list">
        <div 
          v-for="session in chatSessions" 
          :key="session.id" 
          class="session-item"
          :class="{ active: session.id == uuid }"
          @click="switchChat(session.id)"
        >
          <span class="session-title">{{ session.title }}</span>
          <i class="fa-solid fa-trash delete-icon" @click.stop="deleteChat(session.id)"></i>
        </div>
      </div>
    </div>
    <div class="main-content">
      <div class="chat-container">
        <div class="message-list" ref="messaggListRef">
          <div
            v-for="(message, index) in messages"
            :key="index"
            :class="
              message.isUser ? 'message user-message' : 'message bot-message'
            "
          >
            <!-- 会话图标 -->
            <i
              :class="
                message.isUser
                  ? 'fa-solid fa-user message-icon'
                  : 'fa-solid fa-robot message-icon'
              "
            ></i>
            <!-- 会话内容 -->
            <div class="message-content">
              <div v-html="convertStreamOutput(message.content)"></div>
              <!-- loading -->
              <span
                class="loading-dots"
                v-if="message.isThinking || message.isTyping"
              >
                <span class="dot"></span>
                <span class="dot"></span>
              </span>
            </div>
          </div>
        </div>
        <div class="input-container">
          <el-input
            v-model="inputMessage"
            placeholder="请输入消息"
            @keyup.enter="sendMessage"
          ></el-input>
          <el-button @click="sendMessage" :disabled="isSending" type="primary"
            >发送</el-button
          >
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import axios from 'axios'
import { v4 as uuidv4 } from 'uuid'
import { ElMessage, ElMessageBox } from 'element-plus'

const messaggListRef = ref()
const isSending = ref(false)
const uuid = ref()
const inputMessage = ref('')
const messages = ref([])
const chatSessions = ref([]) // 会话列表

onMounted(() => {
  initChatSystem()
})

const scrollToBottom = () => {
  if (messaggListRef.value) {
    messaggListRef.value.scrollTop = messaggListRef.value.scrollHeight
  }
}

// 初始化聊天系统
const initChatSystem = () => {
  const savedSessions = localStorage.getItem('chat_sessions')
  if (savedSessions) {
    chatSessions.value = JSON.parse(savedSessions)
  }
  
  let storedUUID = localStorage.getItem('user_uuid')
  
  // 如果没有存储的 UUID 或者存储的 UUID 不在现有列表中
  if (!storedUUID || (chatSessions.value.length > 0 && !chatSessions.value.find(s => s.id == storedUUID))) {
    // 如果有会话，默认选第一个
    if (chatSessions.value.length > 0) {
      switchChat(chatSessions.value[0].id)
    } else {
      // 如果没有会话，创建新的
      createNewSession()
    }
  } else {
    // 加载当前 UUID 的会话
    uuid.value = storedUUID
    const currentSession = chatSessions.value.find(s => s.id == storedUUID)
    if (currentSession) {
      messages.value = currentSession.messages
    } else {
      // 异常情况处理
      createNewSession() 
    }
  }

  // 监听 messages 变化自动保存
  watch(messages, (newVal) => {
    const currentSession = chatSessions.value.find(s => s.id == uuid.value)
    if (currentSession) {
      currentSession.messages = newVal
      // 更新标题（可选：取第一条消息的前10个字作为标题）
      if (newVal.length > 0 && currentSession.title.startsWith('新对话')) {
          const firstUserMsg = newVal.find(m => m.isUser)
          if (firstUserMsg) {
              currentSession.title = firstUserMsg.content.substring(0, 8) + (firstUserMsg.content.length > 8 ? '...' : '')
          }
      }
      saveSessions()
    }
    scrollToBottom()
  }, { deep: true })
}

const saveSessions = () => {
  localStorage.setItem('chat_sessions', JSON.stringify(chatSessions.value))
}

const createNewSession = () => {
  const newId = uuidToNumber(uuidv4())
  // 计算新的序号：找到当前所有“新对话 X”中的最大序号
  let maxNum = 0
  chatSessions.value.forEach(s => {
    if (s.title.startsWith('新对话 ')) {
      const num = parseInt(s.title.replace('新对话 ', ''))
      if (!isNaN(num) && num > maxNum) {
        maxNum = num
      }
    }
  })
  
  const newSession = {
    id: newId,
    title: '新对话 ' + (maxNum + 1),
    messages: []
  }
  chatSessions.value.unshift(newSession) // 新会话加到最前面
  switchChat(newId)
  
  // 只有在新创建且不需要立即交互时才发送 hello，或者根据需求决定是否发送
  // 这里保留原逻辑：新会话发送 hello
  hello() 
}

const hello = () => {
  // 检查是否已经有消息，避免重复发送欢迎语
  if (messages.value.length === 0) {
    sendRequest('你好')
  }
}

const switchChat = (id) => {
  uuid.value = id
  localStorage.setItem('user_uuid', id)
  const session = chatSessions.value.find(s => s.id == id)
  if (session) {
    messages.value = session.messages
    scrollToBottom()
  }
}

const deleteChat = (id) => {
  console.log('尝试删除会话:', id)
  if (!confirm('确定要删除这个会话吗？')) {
    return
  }
  
  const index = chatSessions.value.findIndex(s => s.id == id)
  if (index > -1) {
    chatSessions.value.splice(index, 1)
    saveSessions()
    
    // 如果删除的是当前会话
    if (id == uuid.value) {
      if (chatSessions.value.length > 0) {
        switchChat(chatSessions.value[0].id)
      } else {
        createNewSession()
      }
    }
    ElMessage.success('删除成功')
  } else {
    console.error('未找到会话:', id)
  }
}


const sendMessage = () => {
  if (inputMessage.value.trim()) {
    sendRequest(inputMessage.value.trim())
    inputMessage.value = ''
  }
}

const sendRequest = (message) => {
  isSending.value = true
  const userMsg = {
    isUser: true,
    content: message,
    isTyping: false,
    isThinking: false,
  }
  //第一条默认发送的用户消息”你好“不放入会话列表
  // if(messages.value.length > 0){
  //   messages.value.push(userMsg)
  // }
  if (message !== '你好') {
    messages.value.push(userMsg)
  }


  // 添加机器人加载消息
  const botMsg = {
    isUser: false,
    content: '', // 增量填充
    isTyping: true, // 显示加载动画
    isThinking: false,
  }
  messages.value.push(botMsg)
  const lastMsg = messages.value[messages.value.length - 1]
  scrollToBottom()

  axios
    .post(
      '/api/xiaozhi/chat',
      { memoryId: uuid.value, message },
      {
        responseType: 'stream', // 必须为合法值 "text"
        onDownloadProgress: (e) => {
          const fullText = e.event.target.responseText
          const lines = fullText.split('\n')
          let content = ''
          lines.forEach((line) => {
            if (line.startsWith('data:')) {
              content += line.substring(5)
            }
          })
          lastMsg.content = content
          scrollToBottom()
        },
      }
    )
    .then(() => {
      // 流结束后隐藏加载动画
      messages.value.at(-1).isTyping = false
      isSending.value = false
    })
    .catch((error) => {
      console.error('流式错误:', error)
      messages.value.at(-1).content = '请求失败，请重试'
      messages.value.at(-1).isTyping = false
      isSending.value = false
    })
}

// 初始化 UUID
const initUUID = () => {
  let storedUUID = localStorage.getItem('user_uuid')
  if (!storedUUID) {
    storedUUID = uuidToNumber(uuidv4())
    localStorage.setItem('user_uuid', storedUUID)
  }
  uuid.value = storedUUID
}

const uuidToNumber = (uuid) => {
  let number = 0
  for (let i = 0; i < uuid.length && i < 6; i++) {
    const hexValue = uuid[i]
    number = number * 16 + (parseInt(hexValue, 16) || 0)
  }
  return number % 1000000
}

import { marked } from 'marked'

// ... existing code ...

// 转换特殊字符
const convertStreamOutput = (output) => {
  return marked.parse(output)
}

const newChat = () => {
  createNewSession()
}

</script>
<style scoped>
.app-layout {
  display: flex;
  height: 100vh;
}

.sidebar {
  width: 200px;
  background-color: #f4f4f9;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.logo-text {
  font-size: 18px;
  font-weight: bold;
  margin-top: 10px;
}

.new-chat-button {
    width: 100%;
    margin-top: 20px;
    margin-bottom: 20px;
  }
  
  .session-list {
    width: 100%;
    flex: 1;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
  
  .session-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px;
    background-color: #fff;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.3s;
    border: 1px solid transparent;
  }
  
  .session-item:hover {
    background-color: #e6f7ff;
  }
  
  .session-item.active {
    background-color: #e6f7ff;
    border-color: #1890ff;
    color: #1890ff;
  }
  
  .session-title {
    font-size: 14px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 120px;
  }
  
  .delete-icon {
    font-size: 12px;
    color: #999;
    padding: 4px;
  }
  
  .delete-icon:hover {
    color: #ff4d4f;
  }

.main-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  background-color: #fff;
  margin-bottom: 10px;
  display: flex;
  flex-direction: column;
}

.message {
  margin-bottom: 10px;
  padding: 10px;
  border-radius: 4px;
  display: flex;
  /* align-items: center; */
}

.message-content {
    /* flex: 1; */ /* 移除 flex: 1，允许内容决定宽度 */
    width: 100%; /* 允许撑满容器 */
    word-break: break-word; 
    line-height: 1.6;
    font-size: 16px; 
    
    /* ... */
    
    /* 确保第一个子元素没有上边距 */
    & > *:first-child {
      margin-top: 0;
    }
    
    /* 确保最后一个子元素没有下边距 */
    & > *:last-child {
      margin-bottom: 0;
    }

    p {
      margin: 0.5em 0;
    }
    
    ul, ol {
      padding-left: 1.5em;
      margin: 0.5em 0;
    }

    li {
      margin: 0.3em 0;
    }
    
    /* 表格样式 */
    table {
      border-collapse: collapse;
      width: 100%;
      margin: 0.5em 0;
    }
    th, td {
      border: 1px solid #ddd;
      padding: 8px;
    }
  }

.user-message {
  background-color: #e0f7fa;
  align-self: flex-end;
  flex-direction: row-reverse;
  /* max-width: 80%; */
}

.bot-message {
    max-width: 80%;
    background-color: #f1f8e9;
    align-self: flex-start;
  }

.message-icon {
  margin: 0 10px;
  font-size: 1.2em;
}

.loading-dots {
  padding-left: 5px;
}

.dot {
  display: inline-block;
  margin-left: 5px;
  width: 8px;
  height: 8px;
  background-color: #000000;
  border-radius: 50%;
  animation: pulse 1.2s infinite ease-in-out both;
}

.dot:nth-child(2) {
  animation-delay: -0.6s;
}

@keyframes pulse {
  0%,
  100% {
    transform: scale(0.6);
    opacity: 0.4;
  }

  50% {
    transform: scale(1);
    opacity: 1;
  }
}
.input-container {
  display: flex;
}

.input-container .el-input {
  flex: 1;
  margin-right: 10px;
}

/* 媒体查询，当设备宽度小于等于 768px 时应用以下样式 */
@media (max-width: 768px) {
  .main-content {
    padding: 10px 0 10px 0;
  }
  .app-layout {
    flex-direction: column;
  }

  .sidebar {
    /* display: none; */
    width: 100%;
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
    padding: 10px;
  }

  .logo-section {
    flex-direction: row;
    align-items: center;
  }

  .logo-text {
    font-size: 20px;
  }

  .logo-section img {
    width: 40px;
    height: 40px;
  }

  .new-chat-button {
    margin-right: 30px;
    width: auto;
    margin-top: 5px;
  }
}

/* 媒体查询，当设备宽度大于 768px 时应用原来的样式 */
@media (min-width: 769px) {
  .main-content {
    padding: 0 0 10px 10px;
  }

  .app-layout {
    display: flex;
    height: 100vh;
  }

  .sidebar {
    width: 200px;
    background-color: #f4f4f9;
    padding: 20px;
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  .logo-section {
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  .logo-text {
    font-size: 18px;
    font-weight: bold;
    margin-top: 10px;
  }

  .new-chat-button {
    width: 100%;
    margin-top: 20px;
    margin-bottom: 20px;
  }
  
  .session-list {
    width: 100%;
    flex: 1;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
  
  .session-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px;
    background-color: #fff;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.3s;
    border: 1px solid transparent;
  }
  
  .session-item:hover {
    background-color: #e6f7ff;
  }
  
  .session-item.active {
    background-color: #e6f7ff;
    border-color: #1890ff;
    color: #1890ff;
  }
  
  .session-title {
    font-size: 14px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 120px;
  }
  
  .delete-icon {
    font-size: 12px;
    color: #999;
    padding: 4px;
  }
  
  .delete-icon:hover {
    color: #ff4d4f;
  }
}
</style>
