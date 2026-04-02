/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.domain.usecase.soundscapes

/**
 * Runtime quality gates used by Soundscapes v1 validation.
 * These are referenced by diagnostics and spike testing scripts.
 */
object SoundscapesQualityGates {
    const val MAX_LAYERS = 10
    const val TARGET_CPU_PERCENT_MAX = 30
    const val TARGET_DRIFT_MS_MAX = 30
    const val TARGET_SCENE_OPEN_P95_MS = 2500
    const val TARGET_CRASH_FREE_PERCENT = 99.5
}

