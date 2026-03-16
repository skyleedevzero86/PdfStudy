package com.sleekydz86.paperlens.infrastructure.web

import com.sleekydz86.paperlens.domain.port.DocumentRepositoryPort
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.nio.file.Files
import java.nio.file.Paths

@RestController
@RequestMapping("/api/viewer")
class ViewerController(private val documentRepository: DocumentRepositoryPort) {

    @GetMapping("/{id}/stream")
    fun streamPdf(@PathVariable id: Long): ResponseEntity<ByteArray> {
        val doc = documentRepository.findById(id)
            ?: throw NoSuchElementException("Document not found")
        val bytes = Files.readAllBytes(Paths.get(doc.storagePath))
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header("Content-Disposition", "inline; filename=\"${doc.originalFileName}\"")
            .body(bytes)
    }

    @GetMapping("/{id}/info")
    fun getInfo(@PathVariable id: Long): ResponseEntity<Map<String, Any>> {
        val doc = documentRepository.findById(id)
            ?: throw NoSuchElementException("Document not found")
        return ResponseEntity.ok(
            mapOf(
                "id" to doc.id,
                "title" to doc.title,
                "pageCount" to doc.pageCount,
                "status" to doc.status,
            )
        )
    }
}
