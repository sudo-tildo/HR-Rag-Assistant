package sugarcube.rh_rag_assistant.ingestion;

import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IngestionService {
    private final VectorStore vectorStore;
    public IngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public int ingest(Resource pdf) {
        List<Document> documents = new TikaDocumentReader(pdf).read();
        List<Document> chunks = TokenTextSplitter.builder().build().apply(documents);
        vectorStore.add(chunks);
        return chunks.size();
    }
}
