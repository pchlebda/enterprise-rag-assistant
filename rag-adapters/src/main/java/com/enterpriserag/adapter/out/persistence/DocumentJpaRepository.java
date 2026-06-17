package com.enterpriserag.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface DocumentJpaRepository extends JpaRepository<DocumentJpaEntity, UUID> {
    Optional<DocumentJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);
    List<DocumentJpaEntity> findAllByTenantId(UUID tenantId);
}
