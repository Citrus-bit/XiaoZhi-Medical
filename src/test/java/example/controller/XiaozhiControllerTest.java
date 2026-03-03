package example.controller;

import dev.langchain4j.service.TokenStream;
import example.assistant.XiaozhiAgent;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.util.function.Consumer;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(XiaozhiController.class)
public class XiaozhiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private XiaozhiAgent xiaozhiAgent;

    // Mocks to satisfy @MapperScan in Main class
    @MockBean
    private DataSource dataSource;
    @MockBean
    private SqlSessionFactory sqlSessionFactory;

    @Test
    public void testChatEndpoint() throws Exception {
        // Mock agent response
        String expectedReply = "你好，我是硅谷小智。";
        
        TokenStream mockStream = mock(TokenStream.class);
        when(mockStream.onPartialResponse(any())).thenAnswer(invocation -> {
            Consumer<String> handler = invocation.getArgument(0);
            handler.accept(expectedReply);
            return mockStream;
        });
        when(mockStream.onCompleteResponse(any())).thenReturn(mockStream);
        when(mockStream.onError(any())).thenReturn(mockStream);
        when(mockStream.ignoreErrors()).thenReturn(mockStream);
        when(mockStream.onRetrieved(any())).thenReturn(mockStream);
        when(mockStream.onToolExecuted(any())).thenReturn(mockStream);

        when(xiaozhiAgent.chat(anyLong(), anyString())).thenReturn(mockStream);

        // Prepare request JSON
        String requestJson = "{\"memoryId\": 123, \"message\": \"你好\"}";

        // Perform POST request
        mockMvc.perform(post("/xiaozhi/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("data:" + expectedReply)));
    }
}
