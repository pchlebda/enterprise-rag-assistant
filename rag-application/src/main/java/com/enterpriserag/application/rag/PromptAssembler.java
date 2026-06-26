package com.enterpriserag.application.rag;

import com.enterpriserag.domain.rag.model.RetrievedChunk;

import java.util.List;

final class PromptAssembler {

    static final String NO_CONTEXT_ANSWER =
            "I don't have enough information in the indexed documents to answer that question.";

    private PromptAssembler() {
    }

    static String assemble(String question, List<RetrievedChunk> groundedChunks) {
        var context = new StringBuilder();
        for (int i = 0; i < groundedChunks.size(); i++) {
            var chunk = groundedChunks.get(i);
            context.append("[%d] (page %d): %s%n%n".formatted(i + 1, chunk.pageNumber(), chunk.content()));
        }

        return """
                You are a helpful assistant. Answer the question using ONLY the context below.
                If the context does not contain enough information to answer, respond exactly with:
                "%s"

                Context:
                %s
                Question: %s

                Answer:""".formatted(NO_CONTEXT_ANSWER, context, question);
    }
}
