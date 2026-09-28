package com.example.data.model

enum class InferenceMode(val displayNameAr: String, val displayNameEn: String) {
    LOCAL_OFFLINE("محرك GGUF المحلي (Assets / الجهاز)", "Local GGUF Engine (Assets / On-Device)"),
    HUGGING_FACE_API("سحابة Hugging Face (Qwengram-0.8B)", "Hugging Face Cloud API (Qwengram-0.8B)")
}

data class GgufModelVariant(
    val filename: String,
    val quantization: String,
    val bits: String,
    val expectedSize: String,
    val actualSizeBytes: Long? = null,
    val isDetectedInAssets: Boolean = false,
    val isDetectedInStorage: Boolean = false,
    val assetPath: String? = null,
    val storagePath: String? = null,
    val descriptionAr: String,
    val ramRequirement: String,
    val speedRating: String,
    val isOfficial: Boolean = true
) {
    val isAvailable: Boolean
        get() = isDetectedInAssets || isDetectedInStorage

    val displaySource: String
        get() = when {
            isDetectedInAssets -> "مجلد Assets"
            isDetectedInStorage -> "وحدة التخزين"
            else -> "غير مثبت"
        }
}

val OFFICIAL_QWENGRAM_VARIANTS = listOf(
    GgufModelVariant(
        filename = "QwenGram-0.8B-Q4_K_M.gguf",
        quantization = "Q4_K_M",
        bits = "4-bit",
        expectedSize = "584 MB",
        descriptionAr = "تكميم 4 بت فائق السرعة، استهلاك ذاكرة منخفض، الأنسب للهواتف الذكية والأجهزة المتوسطة.",
        ramRequirement = "~1.1 GB RAM",
        speedRating = "⚡⚡⚡⚡⚡ فائق السرعة"
    ),
    GgufModelVariant(
        filename = "QwenGram-0.8B-Q6_K.gguf",
        quantization = "Q6_K",
        bits = "6-bit",
        expectedSize = "688 MB",
        descriptionAr = "توازن ممتاز بين دقة النحو وسرعة التوليد، يحافظ على بلاغة النص العربي.",
        ramRequirement = "~1.3 GB RAM",
        speedRating = "⚡⚡⚡⚡ سريع جداً"
    ),
    GgufModelVariant(
        filename = "QwenGram-0.8B-Q8_0.gguf",
        quantization = "Q8_0",
        bits = "8-bit",
        expectedSize = "876 MB",
        descriptionAr = "دقة تكميم 8 بت عالية جداً قريبة من النموذج الأصلي مع كفاءة لغوية متقدمة.",
        ramRequirement = "~1.6 GB RAM",
        speedRating = "⚡⚡⚡ سريع"
    ),
    GgufModelVariant(
        filename = "QwenGram-0.8B-BF16.gguf",
        quantization = "BF16",
        bits = "16-bit",
        expectedSize = "1.60 GB",
        descriptionAr = "الدقة الكاملة بدون تكميم (Bfloat16)، أعلى جودة بلاغية ونحوية ممكنة للمهام المعقدة.",
        ramRequirement = "~2.4 GB RAM",
        speedRating = "⚡⚡ دقة فائقة"
    )
)

data class ModelSettings(
    val inferenceMode: InferenceMode = InferenceMode.LOCAL_OFFLINE,
    val selectedGgufFilename: String = "QwenGram-0.8B-Q4_K_M.gguf",
    val hfApiToken: String = "",
    val modelRepoId: String = "Ninnix96/Qwengram-0.8B",
    val temperature: Float = 0.7f,
    val topP: Float = 0.9f,
    val maxNewTokens: Int = 1024,
    val isDeepThinkEnabled: Boolean = true,
    val isWebSearchEnabled: Boolean = false,
    val isAppLockEnabled: Boolean = false,
    val appLockPin: String = "",
    val isDeveloperModeUnlocked: Boolean = false,
    val isIncognitoMode: Boolean = false,
    val maskInternalDetails: Boolean = true,
    val voiceSpeed: Float = 1.0f,
    val voicePitch: Float = 1.0f,
    val voiceLanguage: String = "ar", // "ar" for Arabic, "en" for English
    val systemPrompt: String = "أنت Qwengram 0.8B، نموذج ذكاء اصطناعي فائق الذكاء مستوحى من بنية DeepSeek و Qwen2.5. تقدم إجابات دقيقة واحترافية باللغة العربية والإنجليزية، وتتقن التصحيح اللغوي، التفكير المنطقي، البرمجة، والردود المفصلة."
)
