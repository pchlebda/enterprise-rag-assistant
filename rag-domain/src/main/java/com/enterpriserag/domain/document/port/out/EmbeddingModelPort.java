package com.enterpriserag.domain.document.port.out;

import java.util.List;

public interface EmbeddingModelPort {
    List<float[]> embedAll(List<String> texts);
    int dimensions();
    String modelId();
}
