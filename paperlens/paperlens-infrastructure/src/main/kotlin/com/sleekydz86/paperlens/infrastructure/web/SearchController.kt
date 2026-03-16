package com.sleekydz86.paperlens.infrastructure.web

import com.sleekydz86.paperlens.application.dto.SearchRequest
import com.sleekydz86.paperlens.application.usecase.SearchUseCase
import com.sleekydz86.paperlens.domain.search.SearchMode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/search")
class SearchController(private val searchUseCase: SearchUseCase) {

    @GetMapping
    fun search(
        @RequestParam query: String,
        @RequestParam(defaultValue = "HYBRID") mode: SearchMode,
        @RequestParam(required = false) docType: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ) = ResponseEntity.ok(
        searchUseCase.search(SearchRequest(query, mode, docType, page, size))
    )
}
