package com.example.niharika.spring.ai.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Configuration
public class RagConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RagConfiguration.class);

    @Value("vectorstore.json")
    private String vectorStoreName;

    @Value("classpath:/data/models.json")
    private Resource models;

    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        return new TokenTextSplitter(
                800,   // default chunk size
                100,   // overlap
                5,     // min chunk length
                10000, // max chunk length
                true   // keep separator
        );
    }

    @Bean(name = "fileVectorStore")
    SimpleVectorStore simpleVectorStore(EmbeddingModel embeddingModel,
                                        TokenTextSplitter textSplitter) throws IOException {
        var simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();
        var vectorStoreFile = getVectorStoreFile();

        if (vectorStoreFile.exists()) {
            log.info("Vector Store File Exists, loading from: {}", vectorStoreFile.getAbsolutePath());
            simpleVectorStore.load(vectorStoreFile);
        } else {
            log.info("Vector Store File Does Not Exist, loading documents from: {}", models.getFilename());
            TextReader textReader = new TextReader(models);
            textReader.getCustomMetadata().put("filename", "models.json");

            List<Document> documents = textReader.get();

            // Apply chunking
            List<Document> splitDocuments = textSplitter.apply(documents);

            log.info("Split {} documents into {} chunks", documents.size(), splitDocuments.size());

            simpleVectorStore.add(splitDocuments);
            simpleVectorStore.save(vectorStoreFile);
        }

        return simpleVectorStore;
    }

    private File getVectorStoreFile() {
        Path path = Paths.get("src", "main", "resources", "data");
        String absolutePath = path.toFile().getAbsolutePath() + "/" + vectorStoreName;
        return new File(absolutePath);
    }

    /**Using PgVector as Vector Store**/
    @Bean(name ="pgVectorStore")
    public VectorStore pgVectorStore(JdbcTemplate jdbcTemplate,
                                     EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .indexType(PgVectorStore.PgIndexType.HNSW)  // Fast similarity search
                .initializeSchema(true)  // Creates table if not exists
                .build();
    }

    @Bean
    public TextSplitter textSplitter() {
        return new TokenTextSplitter(
                800,   // tokens per chunk
                100,   // overlap between chunks
                5,     // minimum chunk size
                10000, // maximum chunk size
                true   // keep separator
        );
    }

}