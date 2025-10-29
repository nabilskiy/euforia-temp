package digital.euforia.app.ui.player.audio.page

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Medium
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import digital.euforia.app.R
import digital.euforia.app.domain.model.TimeOfDay
import digital.euforia.app.domain.model.getLabelRes
import digital.euforia.app.ui.player.audio.AppBarHeight
import digital.euforia.app.ui.player.audio.AvatarUi
import digital.euforia.app.ui.theme.AppBarBackground
import digital.euforia.app.ui.theme.AvatarBackground
import digital.euforia.app.ui.theme.Black
import digital.euforia.app.ui.theme.DarkGray
import digital.euforia.app.ui.theme.ExtraButtonColor
import digital.euforia.app.ui.theme.PrimaryBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.theme.appbarMedium
import digital.euforia.app.ui.util.widget.noRippleClickable
import kotlinx.coroutines.Dispatchers

private val AvatarShape = RoundedCornerShape(16.dp)

@Composable
fun AvatarsPage(
    avatarsList: List<AvatarUi>,
    selectedAvatar: AvatarUi?,
    onAvatarClick: (AvatarUi?) -> Unit
) {
    val gridState = rememberLazyGridState()
    val hazeState = rememberHazeState()
    val statusBarPadding = WindowInsets.statusBars
        .only(WindowInsetsSides.Top)
        .asPaddingValues().calculateTopPadding()

    // Calculate when the first row goes under the app bar area
    val density = LocalDensity.current
    val topThresholdPx = with(density) { (16.dp).roundToPx() }
    val shouldBlur by remember(gridState) {
        derivedStateOf {
            val firstIndex = gridState.firstVisibleItemIndex
            val firstOffset = gridState.firstVisibleItemScrollOffset
            // Start blurring when we've scrolled past the initial top padding,
            // or when the first visible item is no longer in the very first row.
            firstIndex > 0 || firstOffset > topThresholdPx
        }
    }

    Box(modifier = Modifier.background(PrimaryBackground)) {
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState),
            state = gridState,
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = AppBarHeight + statusBarPadding + 16.dp,
                bottom = 16.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = -1) {
                AvatarItem(
                    avatar = null,
                    isSelected = selectedAvatar == null,
                    onClick = onAvatarClick
                )
            }
            items(items = avatarsList, key = { it.id }) { avatar ->
                AvatarItem(
                    avatar = avatar,
                    isSelected = avatar == selectedAvatar,
                    onClick = onAvatarClick
                )
            }
        }
        val appBarModifier = if (shouldBlur) {
            Modifier
                .hazeEffect(
                    hazeState,
                    style = HazeMaterials.regular(AppBarBackground)
                )
                .zIndex(1f)
        } else {
            Modifier.zIndex(1f)
        }
        AppBar(
            modifier = appBarModifier,
            onActionsClick = {}
        )
    }
}


@Composable
fun AvatarItem(
    avatar: AvatarUi?,
    isSelected: Boolean,
    shape: RoundedCornerShape = AvatarShape,
    hazeState: HazeState? = null,
    onClick: (AvatarUi?) -> Unit
) {
    val borderWidth = if (isSelected) 2.dp else 0.dp
    val borderColor = if (isSelected) White else Color.Transparent
    val border = if (isSelected) BorderStroke(2.dp, White) else null
    val context = LocalContext.current
    Surface(
        shape = shape,
        color = AvatarBackground,
        border = border,
        modifier = Modifier
            .aspectRatio(1f)
            .noRippleClickable { onClick(avatar) }
    ) {
        Box(Modifier.fillMaxSize()) {
            if (avatar != null) {
                AvatarImage(url = avatar.imageUrl)
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_not_chosen),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            if (avatar?.isCustom == false) {
                Surface(
                    color = ExtraButtonColor.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(BottomCenter)
                        .padding(bottom = 6.dp)
//                        .background(color = White.copy(alpha = 0.3f), shape = CircleShape)
                ) {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 5.dp, vertical = 3.dp),
                        text = stringResource(R.string.vibes_avatars_demo).uppercase(),
                        color = White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            if (isSelected) {
                Box(Modifier.fillMaxSize().background(White.copy(alpha = 0.2f)))
            }
        }
    }
}

@Composable
private fun AvatarImage(url: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val request = remember(url) {
        ImageRequest.Builder(context)
            .data(url)
            .crossfade(false)
            .build()
    }
    AsyncImage(
        model = request,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.fillMaxSize()
    )
}

@Composable
private fun BoxScope.AppBar(modifier: Modifier, onActionsClick: () -> Unit) {
    Row(
        modifier = modifier
            .noRippleClickable {}.statusBarsPadding().padding(horizontal = 16.dp)
            .align(Alignment.TopCenter)
            .heightIn(min = AppBarHeight)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.size(24.dp))
        Text(
            text = stringResource(R.string.vibes_avatars_title),
            color = White,
            style = appbarMedium,
        )

        Icon(
            modifier = Modifier.size(24.dp).noRippleClickable { onActionsClick() },
            painter = painterResource(id = R.drawable.ic_menu),
            contentDescription = null,
            tint = Color.Unspecified
        )
    }
//    Box(
//        modifier = modifier
//            .align(Alignment.TopCenter)
//            .fillMaxWidth()
//            .heightIn(min = AppBarHeight)
//            .background(Color.Black.copy(alpha = 0.6f))
//            .blur(16.dp)
//    )
}