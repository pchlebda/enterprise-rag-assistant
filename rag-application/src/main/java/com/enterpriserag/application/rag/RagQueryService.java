package com.enterpriserag.application.rag;

import com.enterpriserag.domain.document.port.out.EmbeddingModelPort;
import com.enterpriserag.domain.rag.model.Answer;
import com.enterpriserag.domain.rag.model.Citation;
import com.enterpriserag.domain.rag.model.ChatQuery;
import com.enterpriserag.domain.rag.model.Question;
import com.enterpriserag.domain.rag.port.in.AskQuestionUseCase;
import com.enterpriserag.domain.rag.port.out.ChatModelPort;
import com.enterpriserag.domain.rag.port.out.RetrievalPort;

import java.util.List;

public class RagQueryService implements AskQuestionUseCase {

    static final String NO_CONTEXT_MODEL_ID = "no-context";
    private static final int SNIPPET_MAX_LENGTH = 240;

    private final EmbeddingModelPort embeddingModelPort;
    private final RetrievalPort retrievalPort;
    private final ChatModelPort chatModelPort;
    private final int topK;
    private final double minScore;

    public RagQueryService(
            EmbeddingModelPort embeddingModelPort,
            RetrievalPort retrievalPort,
            ChatModelPort chatModelPort,
            int topK,
            double minScore) {
        this.embeddingModelPort = embeddingModelPort;
        this.retrievalPort = retrievalPort;
        this.chatModelPort = chatModelPort;
        this.topK = topK;
        this.minScore = minScore;
    }

    @Override
    public Answer ask(Question question) {
        var queryEmbedding = embeddingModelPort.embedAll(List.of(question.text())).get(0);
        var candidates = retrievalPort.search(question.tenantId(), queryEmbedding, topK);
        var grounded = candidates.stream().filter(c -> c.score() >= minScore).toList();

        if (grounded.isEmpty()) {
            return new Answer(PromptAssembler.NO_CONTEXT_ANSWER, List.of(), NO_CONTEXT_MODEL_ID);
        }

        var prompt = PromptAssembler.assemble(question.text(), grounded);
        var response = chatModelPort.chat(new ChatQuery(prompt));

        var citations = grounded.stream()
                .map(c -> new Citation(c.documentId(), c.filename(), c.pageNumber(), snippet(c.content()), c.score()))
                .toList();

        return new Answer(response.answer(), citations, response.modelId());
    }

    private String snippet(String content) {
        return content.length() <= SNIPPET_MAX_LENGTH
                ? content
                : content.substring(0, SNIPPET_MAX_LENGTH) + "...";
    }
}
