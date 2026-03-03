package example.config;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.pinecone.PineconeEmbeddingStore;
import example.store.MongoChatMemoryStore;
import io.pinecone.clients.Pinecone;
import org.openapitools.db_control.client.model.DeletionProtection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class XiaozhiConfig {

    @Value("${langchain4j.pinecone.api-key}")
    private String pineconeApiKey;

    @Value("${langchain4j.pinecone.index-name}")
    private String pineconeIndexName;

    @Bean("chatMemoryProviderXiaozhi")
    public ChatMemoryProvider chatMemoryProviderXiaozhi(MongoChatMemoryStore mongoChatMemoryStore) {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)
                .chatMemoryStore(mongoChatMemoryStore)
                .build();
    }

    @Bean
    ContentRetriever contentRetrieverXiaozhi(EmbeddingModel embeddingModel) {
        // 使用 FileSystemDocumentLoader 读取指定目录下的知识库文档
        Document document1 = FileSystemDocumentLoader.loadDocument("/Users/tanbaocheng/Desktop/java-ai-langchain4j/knowledge/医院信息.md");
        Document document2 = FileSystemDocumentLoader.loadDocument("/Users/tanbaocheng/Desktop/java-ai-langchain4j/knowledge/科室信息.md");
        Document document3 = FileSystemDocumentLoader.loadDocument("/Users/tanbaocheng/Desktop/java-ai-langchain4j/knowledge/神经内科.md");
        List<Document> documents = Arrays.asList(document1, document2, document3);

        // 初始化 Pinecone 客户端并检查/创建索引
        Pinecone pinecone = new Pinecone.Builder(pineconeApiKey).build();
        try {
            pinecone.describeIndex(pineconeIndexName);
        } catch (Exception e) {
            // 索引不存在，创建索引
            // DashScope text-embedding-v3 默认维度 1024
            pinecone.createServerlessIndex(pineconeIndexName, "cosine", 1024, "aws", "us-east-1", DeletionProtection.DISABLED);
            // 等待索引就绪（简单休眠，实际生产可能需要轮询）
            try {
                Thread.sleep(20000); // 增加等待时间到20秒
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        // 使用 Pinecone 向量存储
        PineconeEmbeddingStore embeddingStore = PineconeEmbeddingStore.builder()
                .apiKey(pineconeApiKey)
                .index(pineconeIndexName)
                .build();
        
        // 使用注入的 EmbeddingModel (DashScope text-embedding-v3) 进行嵌入

        // 使用文档分割器并进行嵌入
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(300, 0))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        ingestor.ingest(documents);

        // 从嵌入存储（EmbeddingStore）里检索和查询内容相关的信息
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(2)
                .minScore(0.6)
                .build();
    }
}
