package sugarcube.rh_rag_assistant.ingestion;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class IngestionController {
    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping(value = "ingest", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Integer> ingest(@RequestParam("file") MultipartFile file) {
        return Map.of("chunksStored", ingestionService.ingest(file.getResource()));
    }
}
