package digital.euforia.app.ui.util

import android.graphics.Color.*
import androidx.compose.ui.graphics.Color

fun String.toComposeColor(): Color {
    return Color(parseColor(this))
}