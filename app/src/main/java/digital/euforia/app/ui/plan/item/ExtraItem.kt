package digital.euforia.app.ui.plan.item

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import digital.euforia.app.R
import digital.euforia.app.domain.model.plan.ExtraPackage
import digital.euforia.app.ui.plan.PlanViewItems
import digital.euforia.app.ui.theme.SecondaryText
import digital.euforia.app.ui.theme.White

fun LazyListScope.extraItem(
    extraPackage: ExtraPackage?,
    onClick: () -> Unit
) = item(key = PlanViewItems.EXTRA, contentType = PlanViewItems.EXTRA) {
    if (extraPackage != null) {
        val urls = extraPackage.imgUrls
        val reversedUrls = remember(urls) { urls.asReversed() }
        val hazeState = rememberHazeState()

        Box(modifier = Modifier.fillMaxWidth().padding(top = 64.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = spacedBy(8.dp)
            ) {
                PreviewsRow(urlList = urls)
                PreviewsRow(urlList = reversedUrls, isReversed = true)
            }
            DescriptionView(onClick = onClick, hazeState)
        }
    }
}

@Composable
fun BoxScope.DescriptionView(onClick: () -> Unit, hazeState: HazeState) {

    Column(
        modifier = Modifier.align(Alignment.Center).fillMaxWidth().padding(horizontal = 48.dp)
            .hazeSource(hazeState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            modifier = Modifier.padding(top = 4.dp),
            text = stringResource(R.string.today_info_step_extra_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = Bold),
            color = White,
            textAlign = TextAlign.Start,
        )
        Text(
            text = stringResource(R.string.today_info_step_extra_text),
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText,
            textAlign = TextAlign.Start,
        )
        StartButton(onClick = onClick, hazeState = hazeState)
    }
}


@Composable
fun StartButton(onClick: () -> Unit, modifier: Modifier = Modifier, hazeState: HazeState) {
    Box(
        modifier = modifier
            .hazeEffect(
                state = hazeState,
                style = HazeMaterials.ultraThin(White.copy(alpha = 0.2f)).copy(blurRadius = 24.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {

        }
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = stringResource(R.string.today_info_step_scenes_button),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = SemiBold),
            color = White
        )
//        Text(
//            modifier = Modifier.padding(horizontal = 16.dp),
//            text = stringResource(R.string.today_info_step_scenes_button),
//            style = MaterialTheme.typography.titleMedium.copy(fontWeight = SemiBold),
//            color = White
//        )
    }
//    }
}

@Composable
private fun PreviewsRow(
    urlList: List<String>,
    from: Float = 200f,
    to: Float = 300f,
    isReversed: Boolean = false
) {
    val state = rememberLazyListState()

    LazyRow(
        state = state,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = spacedBy(8.dp),
        userScrollEnabled = false
    ) {
        items(items = urlList, key = { it }) { PreviewImage(url = it) }
    }

    LaunchedEffect(from, to, urlList.size, isReversed) {
        val start = if (isReversed) to else from
        val firstTarget = if (isReversed) from else to
        val anim = Animatable(start)
        var last = start

        // If reversed, pre-scroll to the starting offset so the first leg isn't clamped at 0.
        if (isReversed) {
            // Align the actual list position with the animation's starting value.
            state.dispatchRawDelta(start)
        }

        while (true) {
            anim.animateTo(
                targetValue = firstTarget,
                animationSpec = tween(
                    durationMillis = 10000,
                    easing = LinearEasing
                )
            ) {
                val delta = value - last
                last = value
                state.dispatchRawDelta(delta)
            }
            anim.animateTo(
                targetValue = start,
                animationSpec = tween(
                    durationMillis = 10000,
                    easing = LinearEasing
                )
            ) {
                val delta = value - last
                last = value
                state.dispatchRawDelta(delta)
            }
        }
    }
}

@Composable
fun PreviewImage(url: String) {
    AsyncImage(
        modifier = Modifier.clip(RoundedCornerShape(20.dp)).alpha(0.3f).size(120.dp),
        model = url,
        contentDescription = null,
        contentScale = ContentScale.Crop
    )

}