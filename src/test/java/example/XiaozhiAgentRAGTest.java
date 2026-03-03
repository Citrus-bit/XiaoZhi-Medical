package example;

import example.assistant.XiaozhiAgent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CompletableFuture;

@SpringBootTest
public class XiaozhiAgentRAGTest {

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
    public void testRAG() {
        Long memoryId = 3001L;
        
        // 1. Ask about hospital address (from 医院信息.md)
        String q1 = "北京协和医院东单院区的地址在哪里？";
        System.out.println("User: " + q1);
        String a1 = chat(memoryId, q1);
        System.out.println("Xiaozhi: " + a1);
        
        // 2. Ask about department experts (from 神经内科.md)
        String q2 = "神经内科有哪些专家？";
        System.out.println("User: " + q2);
        String a2 = chat(memoryId, q2);
        System.out.println("Xiaozhi: " + a2);
    }
}
