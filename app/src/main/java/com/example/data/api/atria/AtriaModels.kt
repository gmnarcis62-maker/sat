package com.example.data.api.atria

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatMessageDto(
    @Json(name = "role") val role: String,
    @Json(name = "content") val content: String
)

@JsonClass(generateAdapter = true)
data class ChatCompletionRequest(
    @Json(name = "model") val model: String = "Atria-Dawn-Preview",
    @Json(name = "messages") val messages: List<ChatMessageDto>,
    @Json(name = "temperature") val temperature: Double? = 0.7
)

@JsonClass(generateAdapter = true)
data class ChatChoiceDto(
    @Json(name = "index") val index: Int? = null,
    @Json(name = "message") val message: ChatMessageDto? = null,
    @Json(name = "finish_reason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class ApiErrorDetail(
    @Json(name = "message") val message: String? = null,
    @Json(name = "type") val type: String? = null,
    @Json(name = "code") val code: String? = null
)

@JsonClass(generateAdapter = true)
data class ChatCompletionResponse(
    @Json(name = "id") val id: String? = null,
    @Json(name = "model") val model: String? = null,
    @Json(name = "choices") val choices: List<ChatChoiceDto>? = null,
    @Json(name = "error") val error: ApiErrorDetail? = null
)

sealed class AtriaResult {
    data class Success(val reply: String) : AtriaResult()
    data class Error(
        val userFriendlyMessage: String,
        val technicalDetail: String? = null,
        val isAuthError: Boolean = false,
        val isNetworkError: Boolean = false,
        val isRateLimitError: Boolean = false
    ) : AtriaResult()
}
