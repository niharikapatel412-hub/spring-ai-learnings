package com.example.niharika.spring.ai.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;

import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;

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

/**
 * @author Niharika Patel
 * Controller to handle file uploads(PDF,TEXT Files, Documents) that needs to be added to your vector DB
 * to enable querying using the RAG approach.
 * Reference Video:
 **/

@RestController
@RequestMapping("/rag")
public class FileUploadController {

    private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final TextSplitter textSplitter;

    public FileUploadController(ChatClient.Builder builder,
                                @Qualifier("pgVectorStore") VectorStore vectorStore, TextSplitter textSplitter) {
        this.chatClient = builder
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
                .build();
        this.vectorStore = vectorStore;
        this.textSplitter = textSplitter;
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

    @PostMapping("/load/pdf")
    /**
     * Endpoint to upload PDF file with TOC into the vector store
     */
    public ResponseEntity<String> loadPdfToVectorStore(@RequestParam("file") MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            String contentType = file.getContentType();

            if (contentType != null ) {
                Resource resource = new InputStreamResource(file.getInputStream());

                var pdfReader = new ParagraphPdfDocumentReader(resource);
                vectorStore.accept(textSplitter.apply(pdfReader.get()));
                log.info("Pg VectorStore Loaded with File Uploaded by the User!");

            return ResponseEntity.ok(
                    String.format("Successfully indexed '%s' into the vector store.",
                            filename)
            );

        }
        } catch (Exception e) {
            log.error("Error processing file upload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process the uploaded file.");
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to process the uploaded file.");
    }


    @PostMapping("/load/documents")
    public ResponseEntity<String> loadDataToVectorStore(@RequestParam("file") MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            String contentType = file.getContentType();
            log.info("Received file: {} with content type: {}", filename, contentType);
            Resource resource = new InputStreamResource(file.getInputStream());

            var pdfReader = new PagePdfDocumentReader(resource);
            vectorStore.accept(textSplitter.apply(pdfReader.get()));
            log.info("Pg VectorStore new  loaded with file: {}", filename);

            return ResponseEntity.ok("Successfully indexed '%s' into the vector store.".formatted(filename));
        } catch (IOException e) {
            log.error("Error processing file upload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process the uploaded file.");
        }
    }


    @GetMapping("/query")
    public String query(@RequestParam("question") String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }
}