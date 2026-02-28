package example.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;

public interface Assistant {
    String chat(@UserMessage String userMessage);
    
    String chat(@MemoryId String memoryId, @UserMessage String userMessage);
}
