package com.enterpriserag.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

interface DocumentChunkJpaRepository extends JpaRepository<DocumentChunkJpaEntity, UUID> {

    @Modifying
    @Transactional
    @Query("DELETE FROM DocumentChunkJpaEntity e WHERE e.documentId = :documentId")
    void deleteByDocumentId(@Param("documentId") UUID documentId);
}
