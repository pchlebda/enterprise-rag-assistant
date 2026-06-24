package com.enterpriserag.domain.document.port.out;

import com.enterpriserag.domain.document.model.ChunkDraft;

import java.util.List;

public interface ChunkerPort {
    List<ChunkDraft> chunk(byte[] fileContent, String contentType);
}
