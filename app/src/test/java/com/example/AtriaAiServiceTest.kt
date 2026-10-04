package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.api.atria.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AtriaAiServiceTest {

    private lateinit var context: Context
    private lateinit var aiService: AtriaAiService
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        aiService = AtriaAiService(context)
        aiService.clearLocalApiKey()
    }

    @Test
    fun testMissingApiKeyReturnsStructuredAuthError() = runBlocking {
        // Without any key configured, service must return clean user-friendly auth error
        val result = aiService.sendMessage("سوال تست")
        assertTrue("Result must be error", result is AtriaResult.Error)
        val error = result as AtriaResult.Error
        assertTrue("Must be flagged as auth error", error.isAuthError)
        assertTrue("Message must explain key is missing in Persian", error.userFriendlyMessage.contains("کلید ارتباطی"))
    }

    @Test
    fun testLocalApiKeyOverrideSaveAndRetrieve() {
        val dummyKey = "test_bearer_token_xyz"
        aiService.saveLocalApiKey(dummyKey)
        assertEquals("Resolved key must match saved override", dummyKey, aiService.getResolvedApiKey())

        aiService.clearLocalApiKey()
        assertNotEquals("Cleared key must not equal saved override", dummyKey, aiService.getResolvedApiKey())
    }

    @Test
    fun testChatCompletionRequestJsonSerialization() {
        val request = ChatCompletionRequest(
            model = "Atria-Dawn-Preview",
            messages = listOf(
                ChatMessageDto(role = "system", content = "دستورالعمل سیستم"),
                ChatMessageDto(role = "user", content = "تنظیم دیش یاهست")
            ),
            temperature = 0.7
        )

        val adapter = moshi.adapter(ChatCompletionRequest::class.java)
        val json = adapter.toJson(request)

        assertTrue("JSON must contain model Atria-Dawn-Preview", json.contains("\"model\":\"Atria-Dawn-Preview\""))
        assertTrue("JSON must contain system role", json.contains("\"role\":\"system\""))
        assertTrue("JSON must contain user role", json.contains("\"role\":\"user\""))
        assertTrue("JSON must contain temperature 0.7", json.contains("\"temperature\":0.7"))
    }

    @Test
    fun testChatCompletionResponseJsonDeserialization() {
        val json = """
            {
                "id": "chatcmpl-test-123",
                "model": "Atria-Dawn-Preview",
                "choices": [
                    {
                        "index": 0,
                        "message": {
                            "role": "assistant",
                            "content": "برای تنظیم یاهست، آزیموت در تهران حدود ۱۷۸ درجه است."
                        },
                        "finish_reason": "stop"
                    }
                ]
            }
        """.trimIndent()

        val adapter = moshi.adapter(ChatCompletionResponse::class.java)
        val response = adapter.fromJson(json)

        assertNotNull("Response must not be null", response)
        assertEquals("Atria-Dawn-Preview", response?.model)
        assertEquals(1, response?.choices?.size)
        val reply = response?.choices?.firstOrNull()?.message?.content
        assertEquals("برای تنظیم یاهست، آزیموت در تهران حدود ۱۷۸ درجه است.", reply)
    }

    @Test
    fun testChatCompletionErrorJsonDeserialization() {
        val json = """
            {
                "error": {
                    "message": "Invalid API key provided",
                    "type": "invalid_request_error",
                    "code": "invalid_api_key"
                }
            }
        """.trimIndent()

        val adapter = moshi.adapter(ChatCompletionResponse::class.java)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertNotNull(response?.error)
        assertEquals("Invalid API key provided", response?.error?.message)
        assertEquals("invalid_api_key", response?.error?.code)
    }
}
