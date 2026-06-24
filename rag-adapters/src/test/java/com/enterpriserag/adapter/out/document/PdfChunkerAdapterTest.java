package com.enterpriserag.adapter.out.document;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PdfChunkerAdapterTest {

    private final PdfChunkerAdapter adapter = new PdfChunkerAdapter();

    @Test
    void rejectsNonPdfContentType() {
        assertThatThrownBy(() -> adapter.chunk(new byte[]{1, 2, 3}, "text/plain"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void tagsChunksWithCorrectPageNumberAndOverlap() {
        String page1Words = repeatedWords("alpha", 500);
        String page2Words = repeatedWords("beta", 100);
        byte[] pdf = buildPdf(page1Words, page2Words);

        var drafts = adapter.chunk(pdf, "application/pdf");

        assertThat(drafts).isNotEmpty();
        assertThat(drafts).allSatisfy(d -> assertThat(d.pageNumber()).isBetween(1, 2));

        var page1Drafts = drafts.stream().filter(d -> d.pageNumber() == 1).toList();
        var page2Drafts = drafts.stream().filter(d -> d.pageNumber() == 2).toList();
        assertThat(page1Drafts).hasSizeGreaterThan(1);
        assertThat(page2Drafts).hasSize(1);

        // consecutive chunks on the same page must overlap by exactly the configured overlap window (50 words)
        String firstChunkTail = lastWords(page1Drafts.get(0).content(), 50);
        String secondChunkHead = firstWords(page1Drafts.get(1).content(), 50);
        assertThat(secondChunkHead).isEqualTo(firstChunkTail);
    }

    private String repeatedWords(String word, int count) {
        return IntStream.range(0, count).mapToObj(i -> word + i).collect(Collectors.joining(" "));
    }

    private String lastWords(String text, int n) {
        var words = text.split("\\s+");
        int start = Math.max(0, words.length - n);
        return String.join(" ", Arrays.asList(words).subList(start, words.length));
    }

    private String firstWords(String text, int n) {
        var words = text.split("\\s+");
        int end = Math.min(n, words.length);
        return String.join(" ", Arrays.asList(words).subList(0, end));
    }

    private byte[] buildPdf(String... pageTexts) {
        try (var document = new PDDocument()) {
            for (String pageText : pageTexts) {
                var page = new PDPage();
                document.addPage(page);
                try (var contentStream = new PDPageContentStream(document, page)) {
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 10);
                    contentStream.newLineAtOffset(25, 700);
                    for (String line : wrapIntoLines(pageText, 80)) {
                        contentStream.showText(line);
                        contentStream.newLineAtOffset(0, -12);
                    }
                    contentStream.endText();
                }
            }
            var out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<String> wrapIntoLines(String text, int maxLineLength) {
        var words = text.split("\\s+");
        var lines = new ArrayList<String>();
        var current = new StringBuilder();
        for (String word : words) {
            if (current.length() + word.length() + 1 > maxLineLength) {
                lines.add(current.toString());
                current = new StringBuilder();
            }
            if (!current.isEmpty()) current.append(' ');
            current.append(word);
        }
        if (!current.isEmpty()) lines.add(current.toString());
        return lines;
    }
}
