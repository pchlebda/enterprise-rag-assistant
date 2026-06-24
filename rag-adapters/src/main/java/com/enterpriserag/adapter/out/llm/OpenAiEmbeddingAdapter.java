package com.enterpriserag.adapter.out.llm;

import com.enterpriserag.domain.document.port.out.EmbeddingModelPort;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * OpenAI adapter — active only under the 'openai' Spring profile.
 * Not dimension-compatible with the vector(384) schema this milestone ships
 * (text-embedding-3-small is 1536-dim) — running this profile fails fast at
 * INSERT with a clear Postgres error. Multi-dimension support is deferred (plan.md §5).
 */
@Component
@Profile("openai")
public class OpenAiEmbeddingAdapter implements EmbeddingModelPort {

    private final EmbeddingModel model;
    private final String modelName;

    public OpenAiEmbeddingAdapter(
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.embedding-model-name:text-embedding-3-small}") String modelName) {
        this.modelName = modelName;
        this.model = OpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .build();
    }

    @Override
    public List<float[]> embedAll(List<String> texts) {
        var segments = texts.stream().map(TextSegment::from).toList();
        return model.embedAll(segments).content().stream()
                .map(Embedding::vector)
                .toList();
    }

    @Override
    public int dimensions() {
        return 1536;
    }

    @Override
    public String modelId() {
        return modelName;
    }
}
