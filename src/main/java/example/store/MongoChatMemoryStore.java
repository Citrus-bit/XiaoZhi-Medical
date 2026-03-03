package example.store;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MongoChatMemoryStore implements ChatMemoryStore {

    private final MongoTemplate mongoTemplate;

    public MongoChatMemoryStore(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        ChatMemoryDocument doc = mongoTemplate.findById(memoryId.toString(), ChatMemoryDocument.class);
        if (doc != null && doc.getMessagesJson() != null) {
            return ChatMessageDeserializer.messagesFromJson(doc.getMessagesJson());
        }
        return new ArrayList<>();
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String json = ChatMessageSerializer.messagesToJson(messages);
        ChatMemoryDocument doc = new ChatMemoryDocument();
        doc.setId(memoryId.toString());
        doc.setMessagesJson(json);
        mongoTemplate.save(doc);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        ChatMemoryDocument doc = new ChatMemoryDocument();
        doc.setId(memoryId.toString());
        mongoTemplate.remove(doc);
    }

    @Document(collection = "chat_memory")
    public static class ChatMemoryDocument {
        @Id
        private String id;
        private String messagesJson;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMessagesJson() {
            return messagesJson;
        }

        public void setMessagesJson(String messagesJson) {
            this.messagesJson = messagesJson;
        }
    }
}
