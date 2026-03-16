package com.sleekydz86.paperlens.infrastructure.web

import com.sleekydz86.paperlens.application.dto.QaRequest
import com.sleekydz86.paperlens.application.usecase.AiUseCase
import com.sleekydz86.paperlens.infrastructure.persistence.entity.UserEntity
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/ai")
class AiController(
    private val aiUseCase: AiUseCase,
    @org.springframework.beans.factory.annotation.Value("\${app.ai.chat-model-name:huggingface}") private val chatModelName: String,
) {

    @PostMapping("/qa")
    fun ask(
        @RequestBody request: QaRequest,
        @AuthenticationPrincipal user: UserEntity,
    ): ResponseEntity<*> {
        val start = System.currentTimeMillis()
        val response = aiUseCase.answerQuestion(request)
        val latency = System.currentTimeMillis() - start
        aiUseCase.logQuery(user.id, request.documentId, request.question, response.answer, latency, chatModelName)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/similar/{documentId}")
    fun similar(@PathVariable documentId: Long) =
        ResponseEntity.ok(aiUseCase.findSimilarDocuments(documentId))
}