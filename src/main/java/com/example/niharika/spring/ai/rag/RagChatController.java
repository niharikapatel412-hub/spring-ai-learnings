package com.example.niharika.spring.ai.rag;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/** Reference Video:
 * https://www.youtube.com/watch?v=6Pgmr7xMjiY&t=1905s **/
@RestController
@RequestMapping("/rag")
public class RagChatController {

    private final ChatClient chatClient;

    private static final String DEFAULT_SYSTEM_PROMPT ="""
    You are a helpful assistant that answers questions based on the provided context.
    Use the information from the documents to answer questions accurately.
    If you cannot find the answer in the provided context, say so clearly.
            """;


    public RagChatController(ChatClient.Builder builder, @Qualifier("pgVectorStore") VectorStore vectorStore) {

        this.chatClient = builder
                .defaultSystem(DEFAULT_SYSTEM_PROMPT)
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
                .build();

    }

    @GetMapping("/chat")
    public String chat() {
        return chatClient.prompt()
                .user("How did the Federal Reserve's recent interest rate cut impact various asset classes according to the analysis")
                .call()
                .content();
    }


    @PostMapping("/chat/stream")
    public Flux<String> chatStream(@RequestBody PromptRequest promptRequest){
        var conversationId = "user1";
        return chatClient.prompt().user(promptRequest.prompt())
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream().content();
    }

    record PromptRequest(String prompt) {
    }

}
