package com.example.niharika.spring.ai.rag;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api")
public class RagChatController {

    private static final Logger log = LoggerFactory.getLogger(RagChatController.class);

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    @Value("classpath:/data/article.pdf")
    private Resource marketPDF;

    private static final String DEFAULT_SYSTEM_PROMPT = """
        You are a helpful assistant that answers questions based on the provided context.
        Use the information from the documents to answer questions accurately.
        If you cannot find the answer in the provided context, say so clearly.
        """;

    public RagChatController(ChatClient.Builder builder,
                             @Qualifier("pgVectorStore") VectorStore vectorStore) {

        this.vectorStore = vectorStore;

        this.chatClient = builder
                .defaultSystem(DEFAULT_SYSTEM_PROMPT)
                .defaultAdvisors(new QuestionAnswerAdvisor(this.vectorStore))
                .build();
    }


    @PostConstruct
    public void init() {
        log.info("Starting PDF ingestion into VectorStore...");
        var pdfReader = new ParagraphPdfDocumentReader(marketPDF);
        TextSplitter textSplitter = new TokenTextSplitter();
        vectorStore.accept(textSplitter.apply(pdfReader.get()));

        log.info("VectorStore loaded with data from article.pdf");
    }

    @GetMapping("/rag/chat")
    public String chat() {
        return chatClient.prompt()
                .user("How did the Federal Reserve's recent interest rate cut impact various asset classes according to the analysis")
                .call()
                .content();
    }

    @PostMapping("/rag/chat/stream")
    public Flux<String> chatStream(@RequestBody PromptRequest promptRequest) {
        var conversationId = "user1";
        return chatClient.prompt()
                .user(promptRequest.prompt())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
    }

    record PromptRequest(String prompt) { }
}
