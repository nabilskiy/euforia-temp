package digital.euforia.app.ui.util.widget

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes

const val LIBRARY_TITLE_SHARED_KEY = "library_title"

@OptIn(ExperimentalSharedTransitionApi::class)
fun LazyListScope.sharedTitleItem(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    titleRes: Int,
    sharedElementKey: String,
    modifier: Modifier,
) {
    item(key = "title_shared_$sharedElementKey") {
        with(sharedTransitionScope) {
            Text(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .sharedElement(
                        rememberSharedContentState(key = sharedElementKey),
                        animatedVisibilityScope,
                        boundsTransform = { _, _ -> tween(durationMillis = 300) },
                    ),
                text = LocalLocalizedRes.current.string(titleRes),
                style = MaterialTheme.typography.displaySmall,
                color = White,
            )
        }
    }
}
