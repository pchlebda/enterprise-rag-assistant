package com.enterpriserag.domain.rag.model;

import com.enterpriserag.domain.shared.model.TenantId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuestionTest {

    private final TenantId tenantId = TenantId.generate();

    @Test
    void createsWithValidFields() {
        var question = new Question(tenantId, "What is the refund policy?");

        assertThat(question.tenantId()).isEqualTo(tenantId);
        assertThat(question.text()).isEqualTo("What is the refund policy?");
    }

    @Test
    void rejectsNullTenantId() {
        assertThatThrownBy(() -> new Question(null, "text"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsBlankText() {
        assertThatThrownBy(() -> new Question(tenantId, "  "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
