package com.example.niharika.spring.ai.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;

//Reference : Dan Vega's Spring AI RAG example
//https://github.com/danvega/spring-ai-workshop/

@RestController
public class RAGController {

    private final ChatClient chatClient;

    public RAGController(ChatClient.Builder builder, @Qualifier("fileVectorStore") VectorStore vectorStore) {
            this.chatClient = builder
                    .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
                    .build();
    }

    @GetMapping("/rag/models")
    public String faq(@RequestParam(value = "message", defaultValue = "Give me a list of all the models from OpenAI along with their context window.") String message) {
        String systemPrompt = """
    You are a JSON generator. Respond strictly in JSON matching this schema:
    {
      "models": [
        {"company": "string", "model": "string", "contextWindowSize": "integer"}
      ]
    }
    Do not include explanations, markdown, or text outside the JSON object.
    """;

        var response = chatClient.prompt()
                .system(systemPrompt)
                .user(message)
                .call()
                .content();
        return response;
    }

}