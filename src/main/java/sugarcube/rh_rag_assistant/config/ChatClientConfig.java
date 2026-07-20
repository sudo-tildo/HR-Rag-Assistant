package sugarcube.rh_rag_assistant.config;

import java.time.Duration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.redis.RedisChatMemoryRepository;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import redis.clients.jedis.RedisClient;
import sugarcube.rh_rag_assistant.chat.PromptLoggingAdvisor;

import static sugarcube.rh_rag_assistant.config.SystemPrompt.SYSTEM_PROMPT;

@Configuration
public class ChatClientConfig {

    @Value("${spring.app.memory.max-messages}")
    int maxMessages;
    @Value("${spring.app.rag.top-k}")
    int topK;
    @Value("${spring.app.rag.similarity-threshold}")
    double similarityThreshold;

    @Value("${spring.ai.chat.memory.redis.host:localhost}")
    String redisHost;
    @Value("${spring.ai.chat.memory.redis.port:6379}")
    int redisPort;
    @Value("${spring.ai.chat.memory.redis.time-to-live:PT30M}")
    Duration redisTimeToLive; // ISO-8601 (PT30M)

    @Bean
    RedisChatMemoryRepository redisChatMemoryRepository() {
        return RedisChatMemoryRepository.builder()
                .jedisClient(RedisClient.create(redisHost, redisPort))
                .initializeSchema(true)
                .timeToLive(redisTimeToLive)
                .build();
    }

    @Bean
    ChatMemory chatMemory(RedisChatMemoryRepository repository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .maxMessages(maxMessages)
                .build();
    }

    @Bean
    ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory,
                          VectorStore vectorStore,
                          @Value("classpath:/prompts/context-prompt.st") Resource contextPrompt) {

        PromptTemplate contextPromptTemplate = PromptTemplate.builder().resource(contextPrompt).build();

        return builder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder()
                                        .topK(topK).similarityThreshold(similarityThreshold).build())
                                .promptTemplate(contextPromptTemplate)
                                .build(),
                        new PromptLoggingAdvisor(1000))  // high-order, runs after RAG
                .build();
    }
}
