package digital.euforia.app.ui.util

// LocalizedResources.kt
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.LocaleList
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

@Stable
class LocalizedResources(
    val context: Context,
    private val res: Resources,
    val locale: Locale
) {
    fun string(@StringRes id: Int, vararg args: Any): String =
        res.getString(id, *args)

    fun quantityString(@PluralsRes id: Int, quantity: Int, vararg args: Any): String =
        res.getQuantityString(id, quantity, *args)
}

val LocalLangTag = compositionLocalOf { "en" }
val LocalLocalizedRes = staticCompositionLocalOf<LocalizedResources> {
    error("LocalizedResources is not provided")
}

@Composable
fun rememberLocalizedResources(langTag: String): LocalizedResources {
    val base = LocalContext.current
    val sysCfg = LocalConfiguration.current

    return remember(langTag, base) {
        val cfg = Configuration(sysCfg)
        cfg.setLocales(LocaleList.forLanguageTags(langTag))
        val locCtx = base.createConfigurationContext(cfg)
        val locale = Locale.forLanguageTag(langTag)
        LocalizedResources(
            context = locCtx,
            res = locCtx.resources,
            locale = locale
        )
    }
}

/**
 * Wrapper for subtree that switches without recreation
 */
@Composable
fun LocalizedScope(
    langTag: String,
    content: @Composable () -> Unit
) {
    val localizedRes = rememberLocalizedResources(langTag)
    CompositionLocalProvider(
        LocalLangTag provides langTag,
        LocalLocalizedRes provides localizedRes
    ) { content() }
}
