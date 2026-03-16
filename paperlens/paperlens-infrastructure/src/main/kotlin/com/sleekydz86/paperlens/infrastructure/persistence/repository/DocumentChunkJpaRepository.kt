package com.sleekydz86.paperlens.infrastructure.persistence.repository

import com.sleekydz86.paperlens.infrastructure.persistence.entity.DocumentChunkEntity
import org.springframework.data.jpa.repository.JpaRepository

interface DocumentChunkJpaRepository : JpaRepository<DocumentChunkEntity, Long> {
    fun findByDocumentIdOrderByChunkIndex(documentId: Long): List<DocumentChunkEntity>
    fun deleteByDocumentId(documentId: Long)
}
