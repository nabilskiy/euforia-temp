package digital.euforia.app.ui.util

import android.os.Build
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

fun ComponentActivity.setupEdgeToEdge() {
    // Request fully transparent system bars via Activity 1.9+ API
    enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.auto(
            Color.TRANSPARENT,
            Color.TRANSPARENT
        ),
        navigationBarStyle = SystemBarStyle.auto(
            Color.TRANSPARENT,
            Color.TRANSPARENT
        )
    )

    val w = window
    // Ensure window does not fit system windows so content can draw behind
    WindowCompat.setDecorFitsSystemWindows(w, false)

    // Explicitly set transparent colors too (some OEMs ignore styles alone)
    w.statusBarColor = Color.TRANSPARENT
    w.navigationBarColor = Color.TRANSPARENT
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        w.navigationBarDividerColor = Color.TRANSPARENT
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Disable contrast enforcement that adds an opaque scrim on the nav bar (Android 10+)
        w.isNavigationBarContrastEnforced = false
    }

    WindowInsetsControllerCompat(w, w.decorView).apply {
        // We use light (white) navigation icons assuming a dark BottomNavigation background
        isAppearanceLightNavigationBars = false
        // Force light (white) status bar icons for better contrast on dark content/backgrounds
        isAppearanceLightStatusBars = false
    }
}