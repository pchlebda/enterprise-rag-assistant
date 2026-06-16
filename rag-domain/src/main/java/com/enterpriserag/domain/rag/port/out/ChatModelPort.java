package com.enterpriserag.domain.rag.port.out;

import com.enterpriserag.domain.rag.model.ChatQuery;
import com.enterpriserag.domain.rag.model.ChatResponse;

public interface ChatModelPort {

    ChatResponse chat(ChatQuery query);
}
