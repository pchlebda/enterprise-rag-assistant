package com.enterpriserag.adapter.out.storage;

import com.enterpriserag.domain.document.port.out.FileStoragePort;
import com.enterpriserag.domain.shared.model.DocumentId;
import com.enterpriserag.domain.shared.model.TenantId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class LocalFileStorageAdapter implements FileStoragePort {

    private final Path baseDir;

    public LocalFileStorageAdapter(@Value("${storage.local.base-dir:#{systemProperties['java.io.tmpdir']}/rag-storage}") String baseDir) {
        this.baseDir = Path.of(baseDir);
    }

    @Override
    public String store(TenantId tenantId, DocumentId documentId, String filename, byte[] content) {
        try {
            Path tenantDir = baseDir.resolve(tenantId.value().toString());
            Files.createDirectories(tenantDir);
            Path target = tenantDir.resolve(documentId.value() + "_" + filename);
            Files.write(target, content);
            return target.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store file: " + filename, e);
        }
    }
}
