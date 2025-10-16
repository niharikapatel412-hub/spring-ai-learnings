package com.example.niharika.spring.ai.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

//Resources : https://spring.io/projects/spring-ai

@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/chat")
    public String helloWorld() {
        return chatClient
                .prompt("Tell me a joke")
                .call()
                .content();
    }

}
