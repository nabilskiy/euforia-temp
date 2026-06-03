package digital.euforia.app.ui.programs.article

import android.widget.TextView
import androidx.annotation.FontRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.core.MarkwonTheme

/**
 * Renders article markdown via Markwon (same engine as Twain).
 * [headingBreakHeight] is set to 0 so H1/H2 do not draw a line under the title
 * (e.g. `## **STRESS**` from the API).
 */
@Composable
fun ArticleMarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    color: Color,
    @FontRes fontResource: Int? = null,
) {
    val context = LocalContext.current
    val markwon = remember(context) {
        Markwon.builder(context)
            .usePlugin(
                object : AbstractMarkwonPlugin() {
                    override fun configureTheme(builder: MarkwonTheme.Builder) {
                        builder.headingBreakHeight(0)
                    }
                }
            )
            .build()
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            TextView(ctx).apply {
                setTextColor(color.toArgb())
                fontResource?.let { typeface = ResourcesCompat.getFont(ctx, it) }
            }
        },
        update = { textView ->
            markwon.setMarkdown(textView, markdown.replace("\r\n", "\n").trimIndent())
        },
    )
}
