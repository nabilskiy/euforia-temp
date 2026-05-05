/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.ui.soundscapes.scene

enum class SoundscapeErrorType {
    NETWORK,
    DOWNLOAD,
    PLAYBACK,
    UNKNOWN,
}

data class SoundscapeError(
    val type: SoundscapeErrorType,
    val message: String,
)

