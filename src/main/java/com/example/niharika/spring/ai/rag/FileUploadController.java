package com.example.niharika.spring.ai.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;

import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.DoubleStream;

/**
 * @author Niharika Patel
 * Controller to handle file uploads (PDF, TEXT Files, Documents) that needs to be added to your vector DB
 * to enable querying using the RAG approach.
 */
@RestController
@RequestMapping("/rag")
public class FileUploadController {

    private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);

    private final ChatClient chatClient ;
    private final VectorStore vectorStore;
    private final TextSplitter textSplitter;


    private static final String DEFAULT_SYSTEM_PROMPT =
      """
      You are a helpful assistant that answers questions based on the provided context.
      Use the information from the documents to answer questions accurately.
      If you cannot find the answer in the provided context, say so clearly.                                                                                                                           
      """;
    public FileUploadController(ChatClient.Builder chatBuilder,
                                @Qualifier("pgVectorStore") VectorStore vectorStore, 
                                TextSplitter textSplitter) {
        this.vectorStore = vectorStore;
        this.textSplitter = textSplitter;



       // Configure the SearchRequest for the QA Advisor
        var search =  SearchRequest.builder()
                        .topK(5)  // Return top 5 most relevant documents
                        .similarityThreshold(0.7)
                        .build();

        // Create the QuestionAnswerAdvisor with the VectorStore and SearchRequest
        var qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(search)
                .build();

        // Build the ChatClient with the default system prompt and the QA Advisor
        this.chatClient = chatBuilder
                .defaultSystem(DEFAULT_SYSTEM_PROMPT)
                .defaultAdvisors(qaAdvisor)
                .build();

        
        log.info("RAG ChatClient initialized with VectorStore advisor");
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

    /**
     *
     * @param file PDF file that needs to indexed in vector db
     * @return String response indicating success or failure
     * @throws IOException
     * @author Niharika Patel
     */
    @PostMapping("/load/pdf")
    /**
     * Endpoint to upload PDF file with TOC into the vector store
     */
    public ResponseEntity<String> loadPdfToVectorStore(@RequestParam("file") MultipartFile file) {
        String filename = null;
        try {
            filename = file.getOriginalFilename();
            String contentType = file.getContentType();

            log.info("Processing PDF upload: {} (type: {})", filename, contentType);

            if (contentType != null && contentType.equals("application/pdf")) {
                Resource resource = new InputStreamResource(file.getInputStream());

                var pdfReader = new ParagraphPdfDocumentReader(resource);
                List<Document> documents = pdfReader.get();

                log.info("Read {} documents from PDF", documents.size());

                // Split documents into smaller chunks
                List<Document> splitDocs = textSplitter.apply(documents);

                log.info("Split into {} chunks", splitDocs.size());

                // Add to vector store
                vectorStore.accept(splitDocs);

                log.info("Successfully indexed '{}' with {} chunks into vector store",
                        filename, splitDocs.size());

                return ResponseEntity.ok(
                        String.format("Successfully indexed '%s' (%d chunks) into the vector store.",
                                filename, splitDocs.size())
                );
            } else {
                return ResponseEntity.badRequest()
                        .body("Invalid file type. Please upload a PDF file.");
            }
        } catch (Exception e) {
            log.error("Error processing file upload: {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process the uploaded file: " + e.getMessage());
        }
    }

    @PostMapping("/load/documents")
    public ResponseEntity<String> loadDataToVectorStore(@RequestParam("file") MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            String contentType = file.getContentType();
            
            log.info("Received file: {} with content type: {}", filename, contentType);
            
            Resource resource = new InputStreamResource(file.getInputStream());

            var pdfReader = new PagePdfDocumentReader(resource);
            List<Document> documents = pdfReader.get();
            
            log.info("Read {} pages from PDF", documents.size());
            
            List<Document> splitDocs = textSplitter.apply(documents);
            
            log.info("Split into {} chunks", splitDocs.size());
            
            vectorStore.accept(splitDocs);
            
            log.info("Successfully loaded '{}' with {} chunks into vector store", 
                    filename, splitDocs.size());

            return ResponseEntity.ok(
                    String.format("Successfully indexed '%s' (%d chunks) into the vector store.",
                            filename, splitDocs.size())
            );
        } catch (IOException e) {
            log.error("Error processing file upload: {}", file.getOriginalFilename(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process the uploaded file: " + e.getMessage());
        }
    }

    @GetMapping("/query")
    public ResponseEntity<String> query(@RequestParam("question") String question) {
        try {
            log.info("Received query: {}", question);
            
            // Test: First check if vector store has any documents
            List<Document> similarDocs = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(question)
                            .topK(3)
                            .similarityThreshold(0.7)
                            .build()
            );
            
            log.info("Found {} similar documents for query", similarDocs.size());
            
            if (similarDocs.isEmpty()) {
                log.warn("No documents found in vector store for query: {}", question);
                return ResponseEntity.ok(
                    "No relevant documents found in the vector store. " +
                    "Please upload documents first using /rag/load/pdf or /rag/load/documents"
                );
            }
            
            // Now query with ChatClient (advisor is already configured in constructor)
            String response = chatClient.prompt()
                    .user(question)
                    .call()
                    .content();
            
            log.info("Generated response for query: {}", question);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing query: {}", question, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process query: " + e.getMessage());
        }
    }
    
    @GetMapping("/test-search")
    public ResponseEntity<?> testSearch(@RequestParam("question") String question) {
        try {
            log.info("Testing similarity search for: {}", question);
            
            List<Document> results = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(question)
                            .topK(5)
                            .similarityThreshold(0.7)
                            .build()
            );
            
            log.info("Found {} documents", results.size());
            
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            log.error("Error in test search", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping("/count")
    public ResponseEntity<String> getDocumentCount() {
        try {
            // Try to search with a very generic query to estimate document count
            List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder().query("document").topK(100)
                        .build()
            );
            
            return ResponseEntity.ok(
                String.format("Approximately %d documents/chunks in vector store", docs.size())
            );
        } catch (Exception e) {
            log.error("Error counting documents", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error counting documents: " + e.getMessage());
        }
    }
}