package com.enterpriserag.adapter.out.persistence;

import com.enterpriserag.domain.document.model.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface DocumentJpaRepository extends JpaRepository<DocumentJpaEntity, UUID> {
    Optional<DocumentJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);
    List<DocumentJpaEntity> findAllByTenantId(UUID tenantId);

    @Modifying
    @Transactional
    @Query("UPDATE DocumentJpaEntity e SET e.status = :status WHERE e.id = :id")
    void updateStatusById(@Param("id") UUID id, @Param("status") DocumentStatus status);
}
