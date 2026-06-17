package com.enterpriserag.adapter.in.web.v1.dto;

import java.util.UUID;

public record DocumentUploadResponse(UUID documentId, String status) {}
