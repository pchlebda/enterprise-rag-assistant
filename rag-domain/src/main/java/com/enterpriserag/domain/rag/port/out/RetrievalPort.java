package com.enterpriserag.domain.rag.port.out;

import com.enterpriserag.domain.rag.model.RetrievedChunk;
import com.enterpriserag.domain.shared.model.TenantId;

import java.util.List;

public interface RetrievalPort {

    List<RetrievedChunk> search(TenantId tenantId, float[] queryEmbedding, int topK);
}
