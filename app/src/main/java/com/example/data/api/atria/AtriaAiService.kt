package com.example.data.api.atria

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

/**
 * Internal Technical AI Service implementing direct connection to the Chat Completions API.
 * Uses Retrofit 2 and OkHttp with comprehensive network, auth, and rate-limiting error handling.
 */
class AtriaAiService(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("ai_settings_prefs", Context.MODE_PRIVATE)

    companion object {
        const val BASE_URL = "https://api.atria-asi.ai/v1/"
        const val DEFAULT_MODEL = "Atria-Dawn-Preview"
        const val PREF_KEY_API_KEY = "internal_ai_api_key_override"
    }

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE // NEVER log authorization tokens
        })
        .build()

    private val apiService: AtriaApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(AtriaApiService::class.java)

    private val systemPrompt = """
        تو «ماهواره‌یار هوشمند»، دستیار ارشد و متخصص فنی در حوزه ماهواره، دیش، DVB-S/S2، سیستم‌های دریافت ماهواره‌ای و تعمیرات تخصصی رسیور هستی.
        به زبان فارسی روان، دقیق، مهندسی و با رعایت نکات ایمنی و استانداردهای فنی پاسخ بده.
        در عیب‌یابی رسیورها به تست ولتاژهای اساسی (3.3V, 1.8V, 1.1V, 12V, 13/18V تیونر)، قطعات معیوب رایج و هشدار ایمنی برق SMPS اشاره کن.
    """.trimIndent()

    /**
     * Resolves the API key from local environment configuration (.env via BuildConfig)
     * or local user settings preference.
     */
    fun getResolvedApiKey(): String {
        val overrideKey = prefs.getString(PREF_KEY_API_KEY, null)?.trim()
        if (!overrideKey.isNullOrBlank()) {
            return overrideKey
        }

        return try {
            val buildKey = BuildConfig.ATRIA_API_KEY.trim()
            if (buildKey == "MY_ATRIA_API_KEY" || buildKey.isBlank()) "" else buildKey
        } catch (e: Throwable) {
            ""
        }
    }

    /**
     * Saves an API key locally in secure private SharedPreferences.
     */
    fun saveLocalApiKey(apiKey: String) {
        prefs.edit().putString(PREF_KEY_API_KEY, apiKey.trim()).apply()
    }

    /**
     * Clears any local override key.
     */
    fun clearLocalApiKey() {
        prefs.edit().remove(PREF_KEY_API_KEY).apply()
    }

    /**
     * Sends a chat completion query to the internal AI backend with conversation memory.
     *
     * @param userPrompt The new question from the technician.
     * @param history Prior message pairs for context.
     * @return AtriaResult containing either the reply string or a structured user-friendly error.
     */
    suspend fun sendMessage(
        userPrompt: String,
        history: List<Pair<String, Boolean>> = emptyList()
    ): AtriaResult = withContext(Dispatchers.IO) {
        val apiKey = getResolvedApiKey()

        if (apiKey.isBlank()) {
            return@withContext AtriaResult.Error(
                userFriendlyMessage = "کلید ارتباطی هوش مصنوعی در تنظیمات محلی (.env) یا تنظیمات برنامه یافت نشد. لطفاً ATRIA_API_KEY را در فایل .env یا منوی تنظیمات وارد نمایید.",
                technicalDetail = "API Key is missing or blank",
                isAuthError = true
            )
        }

        try {
            val messagesDto = mutableListOf<ChatMessageDto>()
            // 1. System prompt
            messagesDto.add(ChatMessageDto(role = "system", content = systemPrompt))

            // 2. Conversation history (keep last 6 turns to avoid context overflow)
            val recentHistory = history.takeLast(6)
            for ((text, isUser) in recentHistory) {
                messagesDto.add(
                    ChatMessageDto(
                        role = if (isUser) "user" else "assistant",
                        content = text
                    )
                )
            }

            // 3. Current user prompt
            messagesDto.add(ChatMessageDto(role = "user", content = userPrompt))

            val request = ChatCompletionRequest(
                model = DEFAULT_MODEL,
                messages = messagesDto,
                temperature = 0.7
            )

            val authHeader = "Bearer $apiKey"
            val response = apiService.createChatCompletion(authHeader, request)

            if (response.isSuccessful) {
                val body = response.body()
                val reply = body?.choices?.firstOrNull()?.message?.content
                if (!reply.isNullOrBlank()) {
                    AtriaResult.Success(reply.trim())
                } else {
                    AtriaResult.Error(
                        userFriendlyMessage = "پاسخی از سرور هوش مصنوعی دریافت نشد. لطفاً دوباره تلاش نمایید.",
                        technicalDetail = "Response choices were empty"
                    )
                }
            } else {
                val code = response.code()
                val errorBody = response.errorBody()?.string() ?: ""

                when (code) {
                    401 -> AtriaResult.Error(
                        userFriendlyMessage = "کلید دسترسی هوش مصنوعی نامعتبر است یا منقضی شده است (خطای ۴۰۱). لطفاً کلید معتبر را در تنظیمات بررسی کنید.",
                        technicalDetail = "HTTP 401 Unauthorized: $errorBody",
                        isAuthError = true
                    )
                    429 -> AtriaResult.Error(
                        userFriendlyMessage = "محدودیت تعداد درخواست یا مصرف مجاز (Rate Limit - خطای ۴۲۹). لطفاً لحظاتی دیگر تلاش فرمایید.",
                        technicalDetail = "HTTP 429 Too Many Requests: $errorBody",
                        isRateLimitError = true
                    )
                    in 500..599 -> AtriaResult.Error(
                        userFriendlyMessage = "سرور هوش مصنوعی در حال حاضر با بار کاری سنگین یا اختلال موقت مواجه است (خطای $code).",
                        technicalDetail = "HTTP $code Server Error: $errorBody"
                    )
                    else -> AtriaResult.Error(
                        userFriendlyMessage = "خطا در پردازش درخواست هوش مصنوعی (کد خطای $code).",
                        technicalDetail = "HTTP $code: $errorBody"
                    )
                }
            }
        } catch (e: UnknownHostException) {
            AtriaResult.Error(
                userFriendlyMessage = "عدم دسترسی به اینترنت. لطفاً اتصال شبکه یا دیتای گوشی خود را بررسی فرمایید.",
                technicalDetail = e.message,
                isNetworkError = true
            )
        } catch (e: SocketTimeoutException) {
            AtriaResult.Error(
                userFriendlyMessage = "زمان پاسخگویی سرور هوش مصنوعی به پایان رسید (Timeout). لطفاً مجدداً امتحان کنید.",
                technicalDetail = e.message,
                isNetworkError = true
            )
        } catch (e: IOException) {
            AtriaResult.Error(
                userFriendlyMessage = "خطای ارتباطی شبکه در اتصال به سرور هوش مصنوعی رخ داده است.",
                technicalDetail = e.message,
                isNetworkError = true
            )
        } catch (e: Exception) {
            AtriaResult.Error(
                userFriendlyMessage = "خطای غیرمنتظره‌ای رخ داده است: ${e.localizedMessage ?: "نامشخص"}",
                technicalDetail = e.stackTraceToString()
            )
        }
    }
}
