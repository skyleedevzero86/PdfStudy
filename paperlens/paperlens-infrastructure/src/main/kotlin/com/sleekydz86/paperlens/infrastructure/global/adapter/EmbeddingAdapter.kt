package com.sleekydz86.paperlens.infrastructure.global.adapter

import com.sleekydz86.paperlens.application.exception.EmbeddingNotAvailableException
import com.sleekydz86.paperlens.application.port.EmbeddingPort
import com.sleekydz86.paperlens.infrastructure.global.cache.RedisCacheService
import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class EmbeddingAdapter(
    @Lazy
    private val embeddingModel: EmbeddingModel,
    private val redisCacheService: RedisCacheService,
) : EmbeddingPort {

    override fun embed(text: String): FloatArray {
        redisCacheService.getEmbedding(text)?.let { return it }
        return try {
            embeddingModel.embed(text).also { redisCacheService.putEmbedding(text, it) }
        } catch (_: Exception) {
            throw embeddingNotAvailable()
        }
    }

    override fun embedBatch(texts: List<String>): List<FloatArray> {
        val indexedTexts = texts.withIndex()
        val cached = mutableMapOf<Int, FloatArray>()
        val missingIndexes = mutableListOf<Int>()
        val missingTexts = mutableListOf<String>()

        indexedTexts.forEach { (index, text) ->
            val embedding = redisCacheService.getEmbedding(text)
            if (embedding != null) {
                cached[index] = embedding
            } else {
                missingIndexes.add(index)
                missingTexts.add(text)
            }
        }

        if (missingTexts.isNotEmpty()) {
            val generated = try {
                embeddingModel.embed(missingTexts)
            } catch (_: Exception) {
                throw embeddingNotAvailable()
            }

            missingIndexes.zip(generated).forEach { (index, embedding) ->
                cached[index] = embedding
                redisCacheService.putEmbedding(texts[index], embedding)
            }
        }

        return texts.indices.map { index -> cached.getValue(index) }
    }

    private fun embeddingNotAvailable() =
        EmbeddingNotAvailableException(
            "Embedding model initialization failed. The cached ONNX file may be corrupted. " +
                "Delete %TEMP%\\spring-ai-onnx-generative or set " +
                "spring.ai.embedding.transformer.onnx.model-uri to a valid local ONNX file, then restart the server."
        )
}
