package com.example.data.local

import android.content.Context
import android.net.Uri
import com.example.data.model.GgufModelVariant
import com.example.data.model.OFFICIAL_QWENGRAM_VARIANTS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class GgufAssetScanner(private val context: Context) {

    private val modelsDir: File
        get() = File(context.filesDir, "models").apply { if (!exists()) mkdirs() }

    suspend fun scanModels(): List<GgufModelVariant> = withContext(Dispatchers.IO) {
        val detectedMap = mutableMapOf<String, ModelLocationInfo>()

        // 1. Scan assets root and assets/models/
        scanAssetsFolder("", detectedMap)
        scanAssetsFolder("models", detectedMap)

        // 2. Scan internal and external files directories
        scanDirectory(modelsDir, detectedMap)
        context.getExternalFilesDir("models")?.let { scanDirectory(it, detectedMap) }

        // 3. Build list for the 4 official variants
        val resultList = mutableListOf<GgufModelVariant>()
        val officialFilenames = OFFICIAL_QWENGRAM_VARIANTS.map { it.filename.lowercase() }.toSet()

        for (official in OFFICIAL_QWENGRAM_VARIANTS) {
            val key = official.filename.lowercase()
            val detected = detectedMap[key]

            if (detected != null) {
                resultList.add(
                    official.copy(
                        actualSizeBytes = detected.sizeBytes,
                        isDetectedInAssets = detected.isAsset,
                        isDetectedInStorage = !detected.isAsset,
                        assetPath = if (detected.isAsset) detected.path else null,
                        storagePath = if (!detected.isAsset) detected.path else null
                    )
                )
            } else {
                resultList.add(official)
            }
        }

        // 4. Also append any custom .gguf files found in assets or storage
        for ((key, loc) in detectedMap) {
            if (!officialFilenames.contains(key) && key.endsWith(".gguf")) {
                val fileName = File(loc.path).name
                resultList.add(
                    GgufModelVariant(
                        filename = fileName,
                        quantization = inferQuantizationFromName(fileName),
                        bits = inferBitsFromName(fileName),
                        expectedSize = formatSize(loc.sizeBytes),
                        actualSizeBytes = loc.sizeBytes,
                        isDetectedInAssets = loc.isAsset,
                        isDetectedInStorage = !loc.isAsset,
                        assetPath = if (loc.isAsset) loc.path else null,
                        storagePath = if (!loc.isAsset) loc.path else null,
                        descriptionAr = "نموذج GGUF مخصص تم اكتشافه في ${if (loc.isAsset) "Assets" else "التخزين"}.",
                        ramRequirement = "~1.5 GB RAM",
                        speedRating = "مخصص",
                        isOfficial = false
                    )
                )
            }
        }

        resultList
    }

    private fun scanAssetsFolder(folder: String, map: MutableMap<String, ModelLocationInfo>) {
        try {
            val list = context.assets.list(folder) ?: return
            for (name in list) {
                if (name.endsWith(".gguf", ignoreCase = true)) {
                    val fullPath = if (folder.isBlank()) name else "$folder/$name"
                    val size = getAssetSize(fullPath)
                    map[name.lowercase()] = ModelLocationInfo(
                        path = fullPath,
                        sizeBytes = size,
                        isAsset = true
                    )
                }
            }
        } catch (_: Exception) {}
    }

    private fun scanDirectory(dir: File, map: MutableMap<String, ModelLocationInfo>) {
        try {
            if (dir.exists() && dir.isDirectory) {
                dir.listFiles()?.forEach { file ->
                    if (file.isFile && file.name.endsWith(".gguf", ignoreCase = true)) {
                        map[file.name.lowercase()] = ModelLocationInfo(
                            path = file.absolutePath,
                            sizeBytes = file.length(),
                            isAsset = false
                        )
                    }
                }
            }
        } catch (_: Exception) {}
    }

    private fun getAssetSize(assetPath: String): Long {
        return try {
            val fd = context.assets.openFd(assetPath)
            val len = fd.length
            fd.close()
            len
        } catch (_: Exception) {
            try {
                val inputStream = context.assets.open(assetPath)
                val bytes = inputStream.available().toLong()
                inputStream.close()
                bytes
            } catch (_: Exception) {
                0L
            }
        }
    }

    suspend fun importGgufFromUri(uri: Uri, targetName: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val targetFile = File(modelsDir, targetName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(Exception("تعذر فتح مسار الملف"))
            Result.success(targetFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun inferQuantizationFromName(name: String): String {
        val upper = name.uppercase()
        return when {
            upper.contains("Q4_K_M") -> "Q4_K_M"
            upper.contains("Q6_K") -> "Q6_K"
            upper.contains("Q8_0") -> "Q8_0"
            upper.contains("BF16") -> "BF16"
            upper.contains("FP16") -> "FP16"
            upper.contains("Q5_K_M") -> "Q5_K_M"
            upper.contains("Q4_0") -> "Q4_0"
            else -> "GGUF"
        }
    }

    private fun inferBitsFromName(name: String): String {
        val upper = name.uppercase()
        return when {
            upper.contains("Q4") -> "4-bit"
            upper.contains("Q6") -> "6-bit"
            upper.contains("Q8") -> "8-bit"
            upper.contains("BF16") || upper.contains("FP16") -> "16-bit"
            upper.contains("Q5") -> "5-bit"
            else -> "Quantized"
        }
    }

    private fun formatSize(bytes: Long): String {
        if (bytes <= 0) return "غير معروف"
        val mb = bytes / (1024.0 * 1024.0)
        return if (mb >= 1024) {
            String.format("%.2f GB", mb / 1024.0)
        } else {
            String.format("%.0f MB", mb)
        }
    }

    private data class ModelLocationInfo(
        val path: String,
        val sizeBytes: Long,
        val isAsset: Boolean
    )
}
