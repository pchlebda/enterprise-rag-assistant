package com.enterpriserag.adapter.in.web.v1;

import com.enterpriserag.domain.rag.model.ChatResponse;
import com.enterpriserag.domain.rag.port.in.ChatUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ChatUseCase chatUseCase;

    @Test
    void validQueryReturns200WithAnswerAndModelId() throws Exception {
        when(chatUseCase.ask(any()))
                .thenReturn(new ChatResponse("RAG stands for Retrieval-Augmented Generation.", "local-echo"));

        mockMvc.perform(post("/api/v1/chat/query")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"message": "What is RAG?"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("RAG stands for Retrieval-Augmented Generation."))
                .andExpect(jsonPath("$.modelId").value("local-echo"));
    }

    @Test
    void blankMessageReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/chat/query")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"message": ""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingMessageFieldReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/chat/query")
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void messageExceeding4000CharsReturns400() throws Exception {
        String longMessage = "x".repeat(4001);
        mockMvc.perform(post("/api/v1/chat/query")
                        .contentType(APPLICATION_JSON)
                        .content("{\"message\": \"" + longMessage + "\"}"))
                .andExpect(status().isBadRequest());
    }
}
