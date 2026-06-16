package com.enterpriserag.adapter.out.llm;

import com.enterpriserag.domain.rag.model.ChatQuery;
import com.enterpriserag.domain.rag.model.ChatResponse;
import com.enterpriserag.domain.rag.port.out.ChatModelPort;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * OpenAI adapter — active only under the 'openai' Spring profile.
 * Requires OPENAI_API_KEY env var. LangChain4j is used purely as an
 * infrastructure detail; the domain never sees LangChain4j types.
 */
@Component
@Profile("openai")
public class OpenAiChatAdapter implements ChatModelPort {

    private final ChatLanguageModel model;
    private final String modelName;

    public OpenAiChatAdapter(
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.model-name:gpt-4o-mini}") String modelName) {
        this.modelName = modelName;
        this.model = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .build();
    }

    @Override
    public ChatResponse chat(ChatQuery query) {
        String answer = model.generate(query.userMessage());
        return new ChatResponse(answer, modelName);
    }
}
