/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.service.soundscapes

import android.content.Context
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SoundscapePlaybackControllerEntryPoint {
    fun soundscapePlaybackController(): SoundscapePlaybackController
}

private val interruptionLock = Any()
private val activeTokens = linkedSetOf<String>()
private var shouldResumeWhenClear = false

private fun getController(context: Context): SoundscapePlaybackController {
    val appContext = context.applicationContext
    return EntryPointAccessors.fromApplication(
        appContext,
        SoundscapePlaybackControllerEntryPoint::class.java
    ).soundscapePlaybackController()
}

fun pauseActiveSoundscapeIfNeeded(context: Context) {
    val controller = getController(context)

    if (controller.playback.value.isPlaying) {
        controller.setPlaying(false)
    }
}

fun beginSoundscapeInterruption(context: Context, token: String) {
    if (token.isBlank()) return

    val controller = getController(context)
    synchronized(interruptionLock) {
        val wasEmpty = activeTokens.isEmpty()
        val added = activeTokens.add(token)
        if (!added) return

        if (wasEmpty) {
            shouldResumeWhenClear = controller.playback.value.isPlaying
            if (shouldResumeWhenClear) {
                controller.setPlaying(false)
            }
        }
    }
}

fun endSoundscapeInterruption(context: Context, token: String) {
    if (token.isBlank()) return

    val controller = getController(context)
    synchronized(interruptionLock) {
        val removed = activeTokens.remove(token)
        if (!removed) return

        if (activeTokens.isEmpty()) {
            if (shouldResumeWhenClear) {
                controller.setPlaying(true)
            }
            shouldResumeWhenClear = false
        }
    }
}
