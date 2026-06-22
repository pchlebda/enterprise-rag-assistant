package com.enterpriserag.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

interface OutboxEventJpaRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {

    List<OutboxEventJpaEntity> findTop10ByPublishedAtIsNullOrderByCreatedAtAsc();

    @Modifying
    @Transactional
    @Query("UPDATE OutboxEventJpaEntity e SET e.publishedAt = :now, e.attempts = e.attempts + 1 WHERE e.id = :id")
    void markPublished(@Param("id") UUID id, @Param("now") Instant now);
}
