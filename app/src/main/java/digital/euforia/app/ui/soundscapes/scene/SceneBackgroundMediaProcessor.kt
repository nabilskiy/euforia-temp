/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.ui.soundscapes.scene

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

internal data class ImportedLocalImage(val imageUrl: String)

internal data class ImportedLocalVideo(
    val videoUrl: String,
    val previewImageUrl: String?,
)

internal suspend fun importLocalImage(context: Context, sourceUri: Uri): ImportedLocalImage? =
    withContext(Dispatchers.IO) {
        val sourceBitmap = context.contentResolver.openInputStream(sourceUri)?.use { input ->
            BitmapFactory.decodeStream(input)
        } ?: return@withContext null
        val cropped = sourceBitmap.centerCropToRatio(9f / 16f)
        val outputDir = File(context.filesDir, "soundscapes/imported/images").apply { mkdirs() }
        val outputFile = File(outputDir, "bg_${UUID.randomUUID()}.jpg")
        FileOutputStream(outputFile).use { out ->
            cropped.compress(Bitmap.CompressFormat.JPEG, 92, out)
            out.flush()
        }
        if (cropped !== sourceBitmap) {
            sourceBitmap.recycle()
            cropped.recycle()
        } else {
            sourceBitmap.recycle()
        }
        ImportedLocalImage(imageUrl = Uri.fromFile(outputFile).toString())
    }

internal suspend fun importLocalImageWithManualCrop(
    context: Context,
    sourceUri: Uri,
    cropFrameWidthPx: Int,
    cropFrameHeightPx: Int,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
): ImportedLocalImage? = withContext(Dispatchers.IO) {
    val sourceBitmap = context.contentResolver.openInputStream(sourceUri)?.use { input ->
        BitmapFactory.decodeStream(input)
    } ?: return@withContext null
    val cropped = sourceBitmap.cropByViewportTransform(
        cropFrameWidthPx = cropFrameWidthPx,
        cropFrameHeightPx = cropFrameHeightPx,
        scale = scale.coerceIn(1f, 5f),
        offsetX = offsetX,
        offsetY = offsetY,
    )
    val outputDir = File(context.filesDir, "soundscapes/imported/images").apply { mkdirs() }
    val outputFile = File(outputDir, "bg_${UUID.randomUUID()}.jpg")
    FileOutputStream(outputFile).use { out ->
        cropped.compress(Bitmap.CompressFormat.JPEG, 92, out)
        out.flush()
    }
    if (cropped !== sourceBitmap) {
        sourceBitmap.recycle()
        cropped.recycle()
    } else {
        sourceBitmap.recycle()
    }
    ImportedLocalImage(imageUrl = Uri.fromFile(outputFile).toString())
}

internal suspend fun importLocalVideo(context: Context, sourceUri: Uri): ImportedLocalVideo? =
    withContext(Dispatchers.IO) {
        val videoDir = File(context.filesDir, "soundscapes/imported/video").apply { mkdirs() }
        val videoFile = File(videoDir, "video_${UUID.randomUUID()}.mp4")
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(videoFile).use { output ->
                input.copyTo(output)
            }
        } ?: return@withContext null

        val previewFile = File(videoDir, "preview_${UUID.randomUUID()}.jpg")
        val previewSaved = runCatching {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoFile.absolutePath)
            val frame = retriever.getFrameAtTime(0L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            retriever.release()
            if (frame != null) {
                val cropped = frame.centerCropToRatio(9f / 16f)
                FileOutputStream(previewFile).use { out ->
                    cropped.compress(Bitmap.CompressFormat.JPEG, 88, out)
                    out.flush()
                }
                if (cropped !== frame) {
                    frame.recycle()
                    cropped.recycle()
                } else {
                    frame.recycle()
                }
                true
            } else {
                false
            }
        }.getOrDefault(false)

        ImportedLocalVideo(
            videoUrl = Uri.fromFile(videoFile).toString(),
            previewImageUrl = if (previewSaved) Uri.fromFile(previewFile).toString() else null,
        )
    }

private fun Bitmap.centerCropToRatio(targetRatio: Float): Bitmap {
    val srcRatio = width.toFloat() / height.toFloat()
    return if (srcRatio > targetRatio) {
        val newWidth = (height * targetRatio).toInt().coerceAtLeast(1)
        val x = ((width - newWidth) / 2).coerceAtLeast(0)
        Bitmap.createBitmap(this, x, 0, newWidth, height)
    } else {
        val newHeight = (width / targetRatio).toInt().coerceAtLeast(1)
        val y = ((height - newHeight) / 2).coerceAtLeast(0)
        Bitmap.createBitmap(this, 0, y, width, newHeight)
    }
}

private fun Bitmap.cropByViewportTransform(
    cropFrameWidthPx: Int,
    cropFrameHeightPx: Int,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
): Bitmap {
    val frameW = cropFrameWidthPx.coerceAtLeast(1).toFloat()
    val frameH = cropFrameHeightPx.coerceAtLeast(1).toFloat()
    val baseScale = maxOf(frameW / width.toFloat(), frameH / height.toFloat())
    val effectiveScale = baseScale * scale
    val displayW = width * effectiveScale
    val displayH = height * effectiveScale
    val leftInDisplay = (displayW - frameW) / 2f - offsetX
    val topInDisplay = (displayH - frameH) / 2f - offsetY
    val cropLeft = (leftInDisplay / effectiveScale).toInt().coerceIn(0, (width - 1).coerceAtLeast(0))
    val cropTop = (topInDisplay / effectiveScale).toInt().coerceIn(0, (height - 1).coerceAtLeast(0))
    val cropWidth = (frameW / effectiveScale).toInt().coerceAtLeast(1)
        .coerceAtMost(width - cropLeft)
    val cropHeight = (frameH / effectiveScale).toInt().coerceAtLeast(1)
        .coerceAtMost(height - cropTop)
    val cropped = Bitmap.createBitmap(this, cropLeft, cropTop, cropWidth, cropHeight)
    return cropped.centerCropToRatio(9f / 16f)
}
