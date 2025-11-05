package com.example.niharika.spring.ai.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/rag")
public class RAGController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final TokenTextSplitter textSplitter;

    public RAGController(ChatClient.Builder builder,
                         @Qualifier("fileVectorStore") VectorStore vectorStore) {
        this.chatClient = builder
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
                .build();
        this.vectorStore = vectorStore;

        // Initialize text splitter with appropriate chunk size
        this.textSplitter = new TokenTextSplitter(
                800,   // tokens per chunk
                100,   // overlap between chunks
                5,     // minimum chunk size
                10000, // maximum chunk size
                true   // keep separator
        );
    }

    @GetMapping("/models")
    public String faq(@RequestParam(value = "message",
            defaultValue = "Give me a list of all the models from OpenAI along with their context window.")
                      String message) {
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

    @PostMapping("/load")
    public ResponseEntity<String> loadDataToVectorStore(@RequestParam("file") MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            String contentType = file.getContentType();

            List<Document> documents;

            // Check if it's a PDF
            if (contentType != null && contentType.equals("application/pdf")) {
                // Handle PDF files
                Resource resource = new InputStreamResource(file.getInputStream());

                PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(
                        resource,
                        PdfDocumentReaderConfig.builder()
                                .withPageTopMargin(0)
                                .withPageBottomMargin(0)
                                .withPagesPerDocument(1)
                                .build()
                );

                documents = pdfReader.get();

                // Add metadata to each document
                for (Document doc : documents) {
                    doc.getMetadata().put("filename", filename);
                    doc.getMetadata().put("contentType", contentType);
                }

            } else {
                // Handle text files (JSON, TXT, etc.)
                String content = new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);

                Document doc = new Document(
                        content,
                        java.util.Map.of(
                                "filename", filename,
                                "contentType", contentType != null ? contentType : "text/plain"
                        )
                );

                documents = List.of(doc);
            }

            // CRITICAL: Split documents into chunks
            List<Document> chunks = textSplitter.apply(documents);

            // Add chunks to vector store
            vectorStore.add(chunks);

            return ResponseEntity.ok(
                    String.format("Successfully indexed '%s': %d documents → %d chunks",
                            filename, documents.size(), chunks.size())
            );

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to read file: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing file: " + e.getMessage());
        }
    }

    // Optional: Endpoint to query the uploaded documents
    @GetMapping("/query")
    public String query(@RequestParam("question") String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }
}