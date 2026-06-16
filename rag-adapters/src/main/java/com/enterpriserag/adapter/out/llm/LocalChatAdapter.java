package com.enterpriserag.adapter.out.llm;

import com.enterpriserag.domain.rag.model.ChatQuery;
import com.enterpriserag.domain.rag.model.ChatResponse;
import com.enterpriserag.domain.rag.port.out.ChatModelPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Deterministic local adapter — active when the 'openai' profile is NOT set.
 * Used in local development and CI so no API key or network is required.
 * Replace with an Ollama or ONNX adapter for a richer local experience (M4+).
 */
@Component
@Profile("!openai")
public class LocalChatAdapter implements ChatModelPort {

    @Override
    public ChatResponse chat(ChatQuery query) {
        return new ChatResponse(
                "Echo [local-adapter]: " + query.userMessage(),
                "local-echo"
        );
    }
}
