package example;

import example.assistant.XiaozhiAgent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CompletableFuture;

@SpringBootTest
public class XiaozhiAgentTest {

    @Autowired
    private XiaozhiAgent xiaozhiAgent;

    private String chat(Long memoryId, String userMessage) {
        CompletableFuture<String> future = new CompletableFuture<>();
        StringBuilder sb = new StringBuilder();
        xiaozhiAgent.chat(memoryId, userMessage)
                .onPartialResponse(sb::append)
                .onCompleteResponse(response -> future.complete(sb.toString()))
                .onError(future::completeExceptionally)
                .start();
        return future.join();
    }

    @Test
    public void testXiaozhiChat() {
        Long memoryId = 1L;
        String userMessage = "你好，我是小明，我有点发烧";
        String response = chat(memoryId, userMessage);
        System.out.println("Xiaozhi response: " + response);

        String userMessage2 = "我刚才说我叫什么？";
        String response2 = chat(memoryId, userMessage2);
        System.out.println("Xiaozhi response 2: " + response2);
    }

    @Test
    public void testMemoryIsolation() {
        // 用户 A
        Long memoryIdA = 1001L;
        chat(memoryIdA, "你好，我是用户A，我喜欢吃苹果");
        String responseA = chat(memoryIdA, "我喜欢吃什么？");
        System.out.println("User A Response: " + responseA);

        // 用户 B
        Long memoryIdB = 1002L;
        chat(memoryIdB, "你好，我是用户B，我喜欢吃香蕉");
        String responseB = chat(memoryIdB, "我喜欢吃什么？");
        System.out.println("User B Response: " + responseB);

        // 验证互不干扰
        assert responseA.contains("苹果");
        assert responseB.contains("香蕉");
        assert !responseA.contains("香蕉");
        assert !responseB.contains("苹果");
    }
}
