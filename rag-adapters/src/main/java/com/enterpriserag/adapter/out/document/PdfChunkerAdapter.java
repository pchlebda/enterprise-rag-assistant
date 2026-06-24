package com.enterpriserag.adapter.out.document;

import com.enterpriserag.domain.document.model.ChunkDraft;
import com.enterpriserag.domain.document.port.out.ChunkerPort;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class PdfChunkerAdapter implements ChunkerPort {

    private static final int WINDOW_SIZE_WORDS = 400;
    private static final int OVERLAP_WORDS = 50;

    @Override
    public List<ChunkDraft> chunk(byte[] fileContent, String contentType) {
        if (!"application/pdf".equals(contentType)) {
            throw new IllegalArgumentException("Unsupported content type for chunking: " + contentType);
        }

        var drafts = new ArrayList<ChunkDraft>();
        try (var document = PDDocument.load(new ByteArrayInputStream(fileContent))) {
            int pageCount = document.getNumberOfPages();
            var stripper = new PDFTextStripper();

            for (int page = 1; page <= pageCount; page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                String pageText = stripper.getText(document);
                drafts.addAll(chunkPage(page, pageText));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to parse PDF content", e);
        }
        return drafts;
    }

    /**
     * Sizing is a word-count approximation, not a true BPE token count — it avoids
     * pulling an OpenAI-specific tokenizer into the default/local code path.
     */
    private List<ChunkDraft> chunkPage(int pageNumber, String pageText) {
        var words = pageText.trim().split("\\s+");
        if (words.length == 0 || words[0].isBlank()) {
            return List.of();
        }

        var chunks = new ArrayList<ChunkDraft>();
        int start = 0;
        int step = WINDOW_SIZE_WORDS - OVERLAP_WORDS;
        while (start < words.length) {
            int end = Math.min(start + WINDOW_SIZE_WORDS, words.length);
            String content = String.join(" ", Arrays.asList(words).subList(start, end));
            chunks.add(new ChunkDraft(pageNumber, content, end - start));
            if (end == words.length) {
                break;
            }
            start += step;
        }
        return chunks;
    }
}
