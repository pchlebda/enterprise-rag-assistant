package com.enterpriserag.domain.document.port.out;

import com.enterpriserag.domain.document.model.DocumentUploadedEvent;

public interface EventPublisherPort {
    void publish(DocumentUploadedEvent event);
}
