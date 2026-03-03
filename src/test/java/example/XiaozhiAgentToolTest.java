package example;

import example.assistant.XiaozhiAgent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CompletableFuture;

@SpringBootTest
public class XiaozhiAgentToolTest {

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
    public void testBookAppointment() {
        Long memoryId = 2001L;
        
        // 1. 查询号源
        String q1 = "你好，我想查一下2025-04-14上午内科有没有号？";
        System.out.println("User: " + q1);
        String a1 = chat(memoryId, q1);
        System.out.println("Xiaozhi: " + a1);

        // 2. 预约挂号
        String q2 = "好的，我要预约，我叫李四，身份证号是110101199001015678";
        System.out.println("User: " + q2);
        String a2 = chat(memoryId, q2);
        System.out.println("Xiaozhi: " + a2);
        
        // 3. 再次确认预约（应该提示已有预约）
        String q3 = "我刚才预约成功了吗？我想再约一次同样的。";
        System.out.println("User: " + q3);
        String a3 = chat(memoryId, q3);
        System.out.println("Xiaozhi: " + a3);

        // 4. 取消预约
        String q4 = "帮我取消刚才的预约。";
        System.out.println("User: " + q4);
        String a4 = chat(memoryId, q4);
        System.out.println("Xiaozhi: " + a4);
    }
}
