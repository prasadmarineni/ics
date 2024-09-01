package com.dailycodebuffer.ics;

import jakarta.annotation.PostConstruct;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
public class PdfDataLoader {

    private final VectorStore vectorStore;

    private final JdbcClient jdbcClient;

    @Value("classpath:/BNS.pdf")
    private Resource pdfResource;

    public PdfDataLoader(VectorStore vectorStore, JdbcClient jdbcClient) {
        this.vectorStore = vectorStore;
        this.jdbcClient = jdbcClient;
    }

    @PostConstruct
    public void init() {
        Integer count =
                jdbcClient.sql("select COUNT(*) from vector_store")
                        .query(Integer.class)
                        .single();

        System.out.println("No of Records in the PG Vector Store = " + count);

        if(count <= 2) {
            System.out.println("Loading Indian Constitution in the PG Vector Store");
            PdfDocumentReaderConfig config
                    = PdfDocumentReaderConfig.builder()
                    .withPagesPerDocument(1)
                    .build();

            PagePdfDocumentReader reader = new PagePdfDocumentReader(pdfResource, config);

            var textSplitter = new TokenTextSplitter();
            List<Document> list = textSplitter.apply(reader.get());
            
            vectorStore.accept(list);

            System.out.println("Application is ready to Serve the Requests");
        }
    }
}
