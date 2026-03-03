# 硅谷小智 (Silicon Valley Xiaozhi) - 医疗 AI 助手

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.6-green.svg)](https://spring.io/projects/spring-boot)
[![LangChain4j](https://img.shields.io/badge/LangChain4j-1.0.0--beta3-blue.svg)](https://github.com/langchain4j/langchain4j)
[![Vue 3](https://img.shields.io/badge/Vue-3.5.13-4FC08D.svg)](https://vuejs.org/)
[![Element Plus](https://img.shields.io/badge/Element%20Plus-2.8.4-409EFF.svg)](https://element-plus.org/)

这是一个基于 Java (Spring Boot) 和 Vue 3 的全栈 AI 医疗助手项目。它利用了 DeepSeek 大模型的能力，结合 RAG（检索增强生成）技术和 Function Calling（函数调用），提供智能化的医疗咨询、导诊和挂号服务。

## ✨ 主要功能

*   **智能对话**：基于 DeepSeek-V3 模型，提供自然流畅的医疗咨询服务。
*   **流式响应 (SSE)**：支持 Server-Sent Events，实现打字机效果的实时回复，提升用户体验。
*   **RAG 检索增强**：集成 Pinecone 向量数据库和阿里通义千问 Embedding 模型，能够基于本地医疗知识库（如医院介绍、科室专家信息）回答问题，减少幻觉。
*   **工具调用 (Function Calling)**：AI 可以自主调用后端工具，实现查询号源、预约挂号、取消预约等实际业务操作。
*   **会话管理**：
    *   支持创建多个会话窗口。
    *   自动保存历史对话记录（本地存储）。
    *   新会话自动生成标题。
    *   支持删除不需要的会话。
*   **记忆功能**：基于 Redis/MongoDB 实现的长短期记忆，让 AI 能够记住用户的上下文（如姓名、病情描述）。

## 🛠 技术栈

### 后端 (Backend)
*   **核心框架**: Spring Boot 3.2.6
*   **AI 框架**: LangChain4j 1.0.0-beta3
*   **LLM 模型**: DeepSeek-Chat (OpenAI API 兼容模式)
*   **Embedding 模型**: 阿里通义千问 (DashScope text-embedding-v3)
*   **向量数据库**: Pinecone
*   **数据存储**:
    *   MySQL (业务数据)
    *   Redis (缓存/会话记忆)
    *   MongoDB (聊天记录存档)
*   **ORM**: MyBatis-Plus

### 前端 (Frontend)
*   **框架**: Vue 3 (Composition API)
*   **构建工具**: Vite
*   **UI 组件库**: Element Plus
*   **HTTP 客户端**: Axios
*   **Markdown 渲染**: Marked
*   **图标**: FontAwesome

## 🚀 快速开始

### 前置要求
*   JDK 17+
*   Node.js 18+
*   MySQL 8.0+
*   Redis
*   MongoDB (可选)
*   Maven 3.6+

### 1. 克隆项目
```bash
git clone https://github.com/your-username/java-ai-langchain4j.git
cd java-ai-langchain4j
```

### 2. 后端配置与启动
1.  修改 `src/main/resources/application.properties`，配置你的 API Key 和数据库连接信息：
    ```properties
    # DeepSeek API 配置
    langchain4j.open-ai.chat-model.api-key=your_deepseek_api_key
    
    # 阿里 DashScope API 配置 (Embedding)
    langchain4j.community.dashscope.embedding-model.api-key=your_dashscope_api_key
    
    # Pinecone 配置
    langchain4j.pinecone.api-key=your_pinecone_api_key
    
    # 数据库配置
    spring.datasource.password=your_mysql_password
    spring.data.redis.password=your_redis_password
    ```
2.  启动后端服务：
    ```bash
    mvn spring-boot:run
    ```
    或者使用 IDE 直接运行 `example.Main` 类。

### 3. 前端启动
1.  进入前端目录：
    ```bash
    cd xiaozhi-ui
    ```
2.  安装依赖：
    ```bash
    npm install
    ```
3.  启动开发服务器：
    ```bash
    npm run dev
    ```
4.  访问浏览器：打开 `http://localhost:5173` 即可开始对话。

### 4. 一键启动 (可选)
项目根目录下提供了便捷脚本：
```bash
./run-all.sh
```

## 📂 项目结构

```
java-ai-langchain4j/
├── src/main/java/example/      # 后端源码
│   ├── assistant/              # AI 助手定义 (LangChain4j @AiService)
│   ├── controller/             # Web 接口 (SSE 接口)
│   ├── tool/                   # AI 可调用的工具函数 (挂号、查询等)
│   └── ...
├── xiaozhi-ui/                 # 前端源码
│   ├── src/components/         # Vue 组件 (ChatWindow.vue)
│   └── ...
└── ...
```

## 🤝 贡献
欢迎提交 Issue 和 Pull Request！

)
