package com.enterpriserag.adapter.in.web.v1;

import com.enterpriserag.adapter.in.web.v1.dto.ChatQueryRequest;
import com.enterpriserag.adapter.in.web.v1.dto.ChatQueryResponse;
import com.enterpriserag.domain.rag.model.ChatQuery;
import com.enterpriserag.domain.rag.port.in.ChatUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
@Tag(name = "Chat", description = "LLM chat — raw in M1, RAG-grounded from M5")
public class ChatController {

    private final ChatUseCase chatUseCase;

    public ChatController(ChatUseCase chatUseCase) {
        this.chatUseCase = chatUseCase;
    }

    @PostMapping("/query")
    @Operation(summary = "Ask a question and receive an LLM-generated answer")
    public ResponseEntity<ChatQueryResponse> query(@Valid @RequestBody ChatQueryRequest request) {
        var result = chatUseCase.ask(new ChatQuery(request.message()));
        return ResponseEntity.ok(new ChatQueryResponse(result.answer(), result.modelId()));
    }
}
