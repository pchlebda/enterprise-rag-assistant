package com.enterpriserag.domain.rag.port.in;

import com.enterpriserag.domain.rag.model.ChatQuery;
import com.enterpriserag.domain.rag.model.ChatResponse;

public interface ChatUseCase {

    ChatResponse ask(ChatQuery query);
}
