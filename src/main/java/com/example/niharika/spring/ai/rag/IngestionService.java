package com.example.niharika.spring.ai.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;

import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class IngestionService implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final VectorStore vectorStore;

    @Value("classpath:data/article.pdf")
    private Resource systemDesignPdf;

    public IngestionService(@Qualifier("pgVectorStore") VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(String... args) throws Exception {
        var pdfReader = new ParagraphPdfDocumentReader(systemDesignPdf);
        TextSplitter textSplitter = new TokenTextSplitter();
        vectorStore.accept(textSplitter.apply(pdfReader.get()));
        log.info("Pg VectorStore Loaded with data!");
    }
}
