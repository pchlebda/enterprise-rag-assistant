package com.enterpriserag.adapter.out.llm;

import com.enterpriserag.domain.document.port.out.EmbeddingModelPort;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Deterministic local adapter — active when the 'openai' profile is NOT set.
 * In-process ONNX model, no API key or network required. Mirrors LocalChatAdapter.
 */
@Component
@Profile("!openai")
public class LocalEmbeddingAdapter implements EmbeddingModelPort {

    private final AllMiniLmL6V2EmbeddingModel model = new AllMiniLmL6V2EmbeddingModel();

    @Override
    public List<float[]> embedAll(List<String> texts) {
        var segments = texts.stream().map(TextSegment::from).toList();
        return model.embedAll(segments).content().stream()
                .map(Embedding::vector)
                .toList();
    }

    @Override
    public int dimensions() {
        return 384;
    }

    @Override
    public String modelId() {
        return "local-minilm-l6-v2";
    }
}
