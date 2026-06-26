package com.enterpriserag.domain.rag.port.in;

import com.enterpriserag.domain.rag.model.Answer;
import com.enterpriserag.domain.rag.model.Question;

public interface AskQuestionUseCase {

    Answer ask(Question question);
}
