package com.enterpriserag.application.chat;

import com.enterpriserag.domain.rag.model.ChatQuery;
import com.enterpriserag.domain.rag.model.ChatResponse;
import com.enterpriserag.domain.rag.port.in.ChatUseCase;
import com.enterpriserag.domain.rag.port.out.ChatModelPort;
import org.springframework.stereotype.Service;

@Service
public class ChatService implements ChatUseCase {

    private final ChatModelPort chatModelPort;

    public ChatService(ChatModelPort chatModelPort) {
        this.chatModelPort = chatModelPort;
    }

    @Override
    public ChatResponse ask(ChatQuery query) {
        return chatModelPort.chat(query);
    }
}
