package com.example.niharika.spring.ai.rag;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Reference Video:
 * https://www.youtube.com/watch?v=6Pgmr7xMjiY&t=1905s **/
@RestController
public class RagChatController {

    private final ChatClient chatClient;

    public RagChatController(ChatClient.Builder builder, @Qualifier("pgVectorStore") VectorStore vectorStore) {

        this.chatClient = builder
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
                .build();

    }

    @GetMapping("/")
    public String chat() {
        return chatClient.prompt()
                .user("How did the Federal Reserve's recent interest rate cut impact various asset classes according to the analysis")
                .call()
                .content();
    }

}
