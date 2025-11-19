package com.example.niharika.spring.ai.rag;

import com.example.niharika.spring.ai.models.Models;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SimpleVectorStoreController {


    public static final String WELCOME_MESSAGE = "Welcome to the Simple Vector Store API!";

    private final ChatClient chatClient;


    public SimpleVectorStoreController(ChatClient.Builder builder , @Qualifier("simpleVectorStore") VectorStore vectorStore) {
        this.chatClient = builder
                .defaultSystem("You are a helpful assistant that answers questions based on the provided context.")
                .defaultAdvisors(new org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor(vectorStore))
                .build();
    }


    @GetMapping("/models")
    public Models faq(@RequestParam(value = "message", defaultValue = "Give me a list of all the models from OpenAI along with their context window.") String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .entity(Models.class);
    }
}
