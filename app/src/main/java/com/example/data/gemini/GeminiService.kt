package com.example.data.gemini

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

/**
 * Gemini AI Service for Persian Technical Assistant and Visual Board Inspection.
 * Automatically falls back to internal Persian Knowledge Engine when offline.
 */
class GeminiService(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val systemPrompt = """
        تو «ماهواره‌یار هوشمند»، دستیار ارشد و متخصص فنی در حوزه ماهواره، دیش، DVB-S/S2، سیستم‌های دریافت ماهواره‌ای و تعمیرات تخصصی رسیور هستی.
        به زبان فارسی روان، دقیق، مهندسی و با رعایت نکات ایمنی و استانداردهای فنی پاسخ بده.
        در عیب‌یابی رسیورها به تست ولتاژهای اساسی (3.3V, 1.8V, 1.1V, 12V, 13/18V تیونر)، قطعات معیوب رایج و هشدار ایمنی برق SMPS اشاره کن.
    """.trimIndent()

    suspend fun askAssistant(prompt: String, bitmap: Bitmap? = null): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getOfflineKnowledgeResponse(prompt)
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val contentsArray = JSONArray()
            val userContent = JSONObject()
            val partsArray = JSONArray()

            val textPart = JSONObject()
            textPart.put("text", prompt)
            partsArray.put(textPart)

            if (bitmap != null) {
                val imagePart = JSONObject()
                val inlineData = JSONObject()
                inlineData.put("mimeType", "image/jpeg")
                inlineData.put("data", bitmapToBase64(bitmap))
                imagePart.put("inlineData", inlineData)
                partsArray.put(imagePart)
            }

            userContent.put("parts", partsArray)
            contentsArray.put(userContent)

            val rootJson = JSONObject()
            rootJson.put("contents", contentsArray)

            val systemInstruction = JSONObject()
            val sysParts = JSONArray()
            val sysText = JSONObject()
            sysText.put("text", systemPrompt)
            sysParts.put(sysText)
            systemInstruction.put("parts", sysParts)
            rootJson.put("systemInstruction", systemInstruction)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = rootJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext getOfflineKnowledgeResponse(prompt)
            }

            val responseBody = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.getJSONObject("content")
                val parts = content.getJSONArray("parts")
                if (parts.length() > 0) {
                    return@withContext parts.getJSONObject(0).getString("text")
                }
            }

            getOfflineKnowledgeResponse(prompt)
        } catch (e: Exception) {
            getOfflineKnowledgeResponse(prompt)
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    /**
     * Comprehensive Offline Technical Knowledge Base in Persian.
     * Provides instantaneous answers when offline or on rooftop without internet.
     */
    private fun getOfflineKnowledgeResponse(prompt: String): String {
        val p = prompt.lowercase()
        return when {
            p.contains("روشن") || p.contains("پاور") || p.contains("خاموش") -> """
                🔧 **راهنمای عیب‌یابی بخش تغذیه رسیور (پاور):**
                ۱. **بررسی آداپتور خارجی:** در رسیورهای مینی با مولتی‌متر ولتاژ 12V را زیر بار تست کنید. افت ولتاژ به زیر 10V باعث ریستارت می‌شود.
                ۲. **پاور داخلی (SMPS):** خازن‌های الکترولیتی مسیر 5V و 12V (معمولاً 1000uF یا 470uF) را از نظر بادکردگی یا کاهش ظرفیت (ESR بالا) بررسی و تعویض کنید.
                ۳. **دیود شاتکی:** سلامت دیودهای سریع خروجی ثانویه ترانس چاپر را با تست دیود بررسی کنید.
                ⚠️ **هشدار ایمنی:** خازن بزرگ اولیه (400V) ممکن است شارژ باشد! قبل از دست زدن تخلیه شود.
            """.trimIndent()

            p.contains("سیگنال") || p.contains("کیفیت") || p.contains("snr") -> """
                📡 **راهنمای رفع مشکل سیگنال و تیونر:**
                ۱. **ولتاژ قطبیت تیونر:** با مولتی‌متر روی مغزی کابل متصل به تیونر ولتاژ بگیرید:
                   - در فرکانس‌های عمودی (V) باید حدود **13 تا 14 ولت** باشد.
                   - در فرکانس‌های افقی (H) باید حدود **18 تا 19 ولت** باشد.
                ۲. **نبود ولتاژ:** بررسی آیسی تغذیه تیونر (کد معمولاً S8116 یا LM317 یا A8293) و مقاومت‌های فیوزی اطراف آن.
                ۳. **تنظیم دقیق LNB:** اسکیو یا چرخش LNB تاثیر چشمگیری روی SNR دارد (برای یاهست روی ساعت ۶ و هاتبرد ساعت ۴:۳۰ تا ۵).
            """.trimIndent()

            p.contains("بوت") || p.contains("boot") || p.contains("هنگ") -> """
                💾 **عیب‌یابی مشکل Boot Loop و چراغ قرمز:**
                ۱. **رگولاتورهای برد اصلی:** ولتاژهای 3.3V (فلش)، 1.8V یا 1.5V (رم) و 1.1V یا 1.2V (هسته پردازنده) را چک کنید.
                ۲. **نرم‌افزار فلش:** در صورت پریدن برنامه، آی‌سی فلش (SPI Flash معمولاً 25Q64 یا 25Q128) باید با پروگرامر پروگرام شود یا از کابل RS232 لودر استفاده شود.
                ۳. **داغی پردازنده:** گرم شدن غیرعادی CPU نشانه سوختگی داخلی است.
            """.trimIndent()

            p.contains("قیچی") || p.contains("lnb") -> """
                🛰️ **اصول نصب قیچی (چند LNB روی یک دیش):**
                - **قانون انعکاس آینه‌ای:** وقتی روبروی کاسه دیش ایستاده‌اید:
                  - ماهواره‌های واقع در شرق ماهواره مرکزی، LNB آن‌ها در **سمت چپ (غرب)** قرار می‌گیرد.
                  - ماهواره‌های واقع در غرب ماهواره مرکزی، LNB آن‌ها در **سمت راست (شرق)** قرار می‌گیرد.
                - **مثال دیش مرکز هاتبرد (13°E):**
                  - برای گرفتن یاهست (52.5°E): LNB یاهست در سمت چپ و کمی پایین‌تر قرار می‌گیرد (~38 سانتی‌متر فاصله).
                  - برای گرفتن یوتل‌ست (7°E): LNB یوتل‌ست چسبیده به LNB هاتبرد در سمت راست و کمی بالاتر نصب می‌شود (~5.5 سانتی‌متر فاصله).
            """.trimIndent()

            p.contains("موتور") || p.contains("usals") || p.contains("دایسک") -> """
                🔄 **تنظیم موتور گردان و USALS:**
                ۱. **تراز بودن کامل دکل پایه:** اگر پایه حتی ۲ درجه کج باشد، ماهواره‌های شرقی یا غربی از دست می‌روند.
                ۲. **تنظیم صفر موتور روی دیش:** خط صفر موتور و خط مرکز دیش باید کاملاً در یک راستا باشند.
                ۳. **تنظیم زاویه عرض جغرافیایی (Latitude):** روی درجه‌بندی پایه موتور، زاویه عرض شهر خود (مثلاً تهران ۳۵.۷) را تنظیم کنید.
                ۴. **موقعیت‌یابی USALS:** در رسیور طول و عرض دقیق شهر را وارد کنید، موتور خودکار روی جهت مورد نظر می‌چرخد.
            """.trimIndent()

            else -> """
                📡 **پاسخ فنی ماهواره‌یار:**
                در ارتباط با سوال شما درباره «$prompt»:
                - این موضوع به بخش تجهیزات، تنظیم زاویه دیش و سازگاری استاندارد DVB-S2 مربوط است.
                - توصیه می‌شود ابتدا از تراز بودن پایه دکل، سلامت کابل تمام‌مس RG6 و تست ولتاژ خروجی رسیور (13V/18V) اطمینان حاصل فرمایید.
                - برای اطلاعات تکمیلی می‌توانید از ابزارهای محاسباتی، بانک فرکانس و بخش دیاگرام‌های عیب‌یابی در منوی برنامه استفاده کنید.
            """.trimIndent()
        }
    }
}
