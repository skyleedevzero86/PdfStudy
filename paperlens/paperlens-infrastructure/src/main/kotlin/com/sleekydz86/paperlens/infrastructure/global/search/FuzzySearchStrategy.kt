package com.sleekydz86.paperlens.infrastructure.global.search

import com.sleekydz86.paperlens.application.dto.SearchRequest
import com.sleekydz86.paperlens.application.dto.SearchResponse
import com.sleekydz86.paperlens.application.dto.SearchResult
import com.sleekydz86.paperlens.application.strategy.SearchStrategy
import com.sleekydz86.paperlens.domain.search.SearchMode
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

@Component
class FuzzySearchStrategy(
    private val jdbcTemplate: JdbcTemplate,
) : SearchStrategy {

    override val mode: SearchMode = SearchMode.FUZZY

    override fun search(request: SearchRequest): SearchResponse {
        val baseWhere = """
            deleted_at IS NULL
              AND (similarity(d.title, ?) > 0.1 OR similarity(coalesce(d.description,''), ?) > 0.1)
              AND (?::text IS NULL OR d.document_type = ?)
        """.trimIndent()
        val countSql = "SELECT COUNT(*) FROM documents d WHERE $baseWhere"
        val totalElements = jdbcTemplate.queryForObject(countSql, Long::class.java, request.query, request.query, request.docType, request.docType) ?: 0L
        val totalPages = if (request.size > 0) ((totalElements + request.size - 1) / request.size).toInt() else 0

        val sql = """
            SELECT d.id, d.title, d.summary_short, d.document_type,
                   similarity(d.title, ?) AS score
            FROM documents d
            WHERE $baseWhere
            ORDER BY score DESC
            LIMIT ? OFFSET ?
        """.trimIndent()
        val results = jdbcTemplate.query(sql, { rs, _ ->
            SearchResult(
                documentId = rs.getLong("id"),
                title = rs.getString("title"),
                summaryShort = rs.getString("summary_short"),
                documentType = rs.getString("document_type"),
                score = rs.getDouble("score"),
                highlights = emptyList(),
            )
        }, request.query, request.query, request.docType, request.docType, request.size, request.page * request.size)
        return SearchResponse(
            results = results,
            totalElements = totalElements,
            totalPages = totalPages,
            page = request.page,
            size = request.size,
            query = request.query,
            mode = mode,
        )
    }
}