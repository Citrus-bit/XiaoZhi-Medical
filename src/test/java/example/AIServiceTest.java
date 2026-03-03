package example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import dev.langchain4j.model.openai.OpenAiChatModel;
import example.assistant.Assistant;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import example.store.RedisChatMemoryStore;
import example.store.MongoChatMemoryStore;

@SpringBootTest
public class AIServiceTest {

    @Autowired
    private OpenAiChatModel openAiChatModel;

    // 注入我们自定义的 RedisChatMemoryStore
    // 注意：如果本地没有运行 Redis (localhost:6379)，Spring Context 启动可能会报错或连接失败
    @Autowired(required = false)
    private RedisChatMemoryStore redisChatMemoryStore;

    @Autowired(required = false)
    private MongoChatMemoryStore mongoChatMemoryStore;

    @Test
    public void testChatWithRedisMemory() {
        if (redisChatMemoryStore == null) {
            System.err.println("RedisChatMemoryStore 未注入，可能是因为 Redis 连接失败。跳过测试。");
            return;
        }

        String chatId = "user-123";

        // 创建带有持久化 ChatMemory 的 AIService
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(openAiChatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(10)
                        .chatMemoryStore(redisChatMemoryStore)
                        .build())
                .build();

        // 第一轮对话：告诉 AI 我的名字
        System.out.println("--- Round 1 ---");
        try {
            String answer1 = assistant.chat(chatId, "你好，我叫 Alice");
            System.out.println("Answer 1: " + answer1);

            // 第二轮对话：询问 AI 我的名字
            System.out.println("--- Round 2 ---");
            String answer2 = assistant.chat(chatId, "我的名字是什么？");
            System.out.println("Answer 2: " + answer2);
        } catch (Exception e) {
            System.err.println("测试执行出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testChatWithMongoMemory() {
        if (mongoChatMemoryStore == null) {
            System.err.println("MongoChatMemoryStore 未注入，可能是因为 MongoDB 连接失败。跳过测试。");
            return;
        }

        String chatId = "user-mongo-123";

        // 创建带有持久化 ChatMemory 的 AIService
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(openAiChatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(10)
                        .chatMemoryStore(mongoChatMemoryStore)
                        .build())
                .build();

        // 第一轮对话：告诉 AI 我的名字
        System.out.println("--- Mongo Round 1 ---");
        try {
            String answer1 = assistant.chat(chatId, "你好，我叫 Bob");
            System.out.println("Answer 1: " + answer1);

            // 第二轮对话：询问 AI 我的名字
            System.out.println("--- Mongo Round 2 ---");
            String answer2 = assistant.chat(chatId, "我的名字是什么？");
            System.out.println("Answer 2: " + answer2);
        } catch (Exception e) {
            System.err.println("测试执行出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
