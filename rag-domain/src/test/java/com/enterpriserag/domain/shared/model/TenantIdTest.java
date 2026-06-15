package com.enterpriserag.domain.shared.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TenantIdTest {

    @Test
    void of_wraps_given_uuid() {
        var uuid = UUID.randomUUID();
        assertThat(TenantId.of(uuid).value()).isEqualTo(uuid);
    }

    @Test
    void generate_produces_unique_ids() {
        assertThat(TenantId.generate()).isNotEqualTo(TenantId.generate());
    }

    @Test
    void null_value_is_rejected() {
        assertThatThrownBy(() -> TenantId.of(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void toString_delegates_to_uuid() {
        var uuid = UUID.randomUUID();
        assertThat(TenantId.of(uuid).toString()).isEqualTo(uuid.toString());
    }

    @Test
    void record_equality_is_value_based() {
        var uuid = UUID.randomUUID();
        assertThat(TenantId.of(uuid)).isEqualTo(TenantId.of(uuid));
    }
}
