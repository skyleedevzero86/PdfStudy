package com.sleekydz86.paperlens.infrastructure.persistence.adapter

import com.sleekydz86.paperlens.domain.document.DocumentChunk
import com.sleekydz86.paperlens.domain.port.DocumentChunkRepositoryPort
import com.sleekydz86.paperlens.infrastructure.persistence.mapper.DocumentChunkMapper
import com.sleekydz86.paperlens.infrastructure.persistence.repository.DocumentChunkJpaRepository
import com.sleekydz86.paperlens.infrastructure.persistence.repository.DocumentJpaRepository
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.LocalDateTime

@Component
class DocumentChunkRepositoryAdapter(
    private val chunkJpaRepository: DocumentChunkJpaRepository,
    private val documentJpaRepository: DocumentJpaRepository,
    private val entityManager: EntityManager,
) : DocumentChunkRepositoryPort {

    override fun save(chunk: DocumentChunk): DocumentChunk {
        val document = documentJpaRepository.findById(chunk.documentId).orElseThrow()
        val entity = DocumentChunkMapper.toEntity(chunk, document)
        val saved = chunkJpaRepository.save(entity)
        return DocumentChunkMapper.toDomain(saved)
    }

    override fun saveAll(chunks: List<DocumentChunk>): List<DocumentChunk> {
        if (chunks.isEmpty()) return emptyList()
        val documentId = chunks.first().documentId
        val document = documentJpaRepository.findById(documentId).orElseThrow()
        val entities = chunks.map { DocumentChunkMapper.toEntity(it, document) }
        return chunkJpaRepository.saveAll(entities).map { DocumentChunkMapper.toDomain(it) }
    }

    override fun findByDocumentIdOrderByChunkIndex(documentId: Long): List<DocumentChunk> =
        entityManager.createNativeQuery(
            """
            SELECT id, document_id, page_from, page_to, chunk_index, content, token_count, created_at
            FROM document_chunks
            WHERE document_id = ?
            ORDER BY chunk_index
            """.trimIndent()
        )
            .setParameter(1, documentId)
            .resultList
            .map { row -> toDomainChunk(row as Array<*>) }

    override fun deleteByDocumentId(documentId: Long) {
        chunkJpaRepository.deleteByDocumentId(documentId)
    }

    @Transactional
    override fun updateEmbedding(chunkId: Long, embedding: FloatArray) {
        val vector = embedding.joinToString(",", "[", "]")
        entityManager.createNativeQuery(
            """
            UPDATE document_chunks
            SET embedding = ?::vector
            WHERE id = ?
            """.trimIndent()
        )
            .setParameter(1, vector)
            .setParameter(2, chunkId)
            .executeUpdate()
    }

    private fun toDomainChunk(row: Array<*>): DocumentChunk =
        DocumentChunk(
            id = (row[0] as Number).toLong(),
            documentId = (row[1] as Number).toLong(),
            pageFrom = (row[2] as Number).toInt(),
            pageTo = (row[3] as Number).toInt(),
            chunkIndex = (row[4] as Number).toInt(),
            content = row[5] as String,
            tokenCount = (row[6] as Number).toInt(),
            embedding = null,
            createdAt = toLocalDateTime(row[7]),
        )

    private fun toLocalDateTime(value: Any?): LocalDateTime =
        when (value) {
            is LocalDateTime -> value
            is Timestamp -> value.toLocalDateTime()
            else -> LocalDateTime.now()
        }
}
