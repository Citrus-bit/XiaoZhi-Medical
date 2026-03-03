package example.controller;

import example.assistant.XiaozhiAgent;
import example.dto.ChatRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/xiaozhi")
public class XiaozhiController {

    private final XiaozhiAgent xiaozhiAgent;

    @Autowired
    public XiaozhiController(XiaozhiAgent xiaozhiAgent) {
        this.xiaozhiAgent = xiaozhiAgent;
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody ChatRequest request) {
        if (request.getMemoryId() == null) {
            throw new IllegalArgumentException("memoryId is required");
        }

        SseEmitter emitter = new SseEmitter();
        xiaozhiAgent.chat(request.getMemoryId(), request.getMessage())
                .onPartialResponse(token -> {
                    try {
                        emitter.send(token);
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                })
                .onCompleteResponse(response -> emitter.complete())
                .onError(emitter::completeWithError)
                .start();
        return emitter;
    }
}
