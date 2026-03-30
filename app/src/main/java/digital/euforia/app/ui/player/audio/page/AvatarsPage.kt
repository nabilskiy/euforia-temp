package digital.euforia.app.ui.player.audio.page

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntSize
import java.io.File
import java.io.FileOutputStream
import android.content.Context
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.ui.draw.clip
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import digital.euforia.app.R
import digital.euforia.app.ui.player.audio.AppBarHeightLarge
import digital.euforia.app.ui.player.audio.AvatarUi
import digital.euforia.app.ui.theme.AppBarBackground
import digital.euforia.app.ui.theme.AvatarBackground
import digital.euforia.app.ui.theme.DayBlue
import digital.euforia.app.ui.theme.ExtraButtonColor
import digital.euforia.app.ui.theme.NavBarBackground
import digital.euforia.app.ui.theme.PrimaryBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.theme.YellowConfirm
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.theme.appbarMedium
import digital.euforia.app.ui.util.SubscriptionActivityLauncher
import digital.euforia.app.ui.util.widget.MaxView
import digital.euforia.app.ui.util.widget.MenuItem
import digital.euforia.app.ui.util.widget.OptionsMenu
import digital.euforia.app.ui.util.widget.fadeBottom
import digital.euforia.app.ui.util.widget.noRippleClickable

private val AvatarShape = RoundedCornerShape(16.dp)

@Composable
fun AvatarsPage(
    isPremium: Boolean,
    isEditMode: Boolean,
    avatarsList: List<AvatarUi>,
    selectedAvatar: AvatarUi?,
    selectedAvatarIds: List<Int>,
    onAvatarClick: (AvatarUi?) -> Unit,
    onEditClick: () -> Unit,
    onSelectAllClick: () -> Unit,
    onSelect: (Int) -> Unit,
    onDeleteSelectedClick: () -> Unit,
    onDeleteClick: (Int) -> Unit,
    onAddAvatarClick: () -> Unit,

    ) {
    val localizedRes = LocalLocalizedRes.current
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
            // Blur only after the first row is fully scrolled off
            val firstOffset = gridState.firstVisibleItemScrollOffset
            // Start blurring when we've scrolled past the initial top padding,
            // or when the first visible item is no longer in the very first row.
            firstIndex > 0 || firstOffset > topThresholdPx
        }
    }

    SubscriptionActivityLauncher { launchSubscriptionActivity ->
        Box(modifier = Modifier.background(PrimaryBackground)) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LazyVerticalGrid(
                    modifier = Modifier
                        .weight(1f)
                        .hazeSource(hazeState)
                        .fadeBottom(32.dp),
                    state = gridState,
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = AppBarHeightLarge + statusBarPadding + 16.dp,
                        bottom = 16.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (!isEditMode) {
                        item(key = -1) {
                            AvatarItem(
                                avatar = null,
                                isSelected = selectedAvatar == null,
                                onClick = onAvatarClick
                            )
                        }
                    }

                    items(items = avatarsList, key = { it.id }) { avatar ->
                        AvatarItem(
                            avatar = avatar,
                            isSelected = avatar == selectedAvatar,
                            isEditMode = isEditMode,
                            isSelectedInEditMode = selectedAvatarIds.contains(avatar.id),
                            onClick = {
                                if (isEditMode) {
                                    onSelect(it?.id ?: -1)
                                } else {
                                    onAvatarClick(it)
                                }
                            }
                        )
                    }
                }


                AddAvatarButton(
                    isPremium = isPremium,
                    onClick = {
                        if (isPremium) {
                            onAddAvatarClick()
                        } else {
                            launchSubscriptionActivity()
                        }
                    }
                )

                Text(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 24.dp,
                        bottom = 24.dp
                    ).navigationBarsPadding(),
                    text = localizedRes.string(R.string.vibes_avatars_info_text),
                    color = White.copy(0.7f),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal)
                )
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
                menuItems =
                    buildList {
                        if (isEditMode) {
                            val allSelected = selectedAvatarIds.size == avatarsList.size
                            if (!allSelected) {
                                add(
                                    MenuItem(
                                        R.string.select_all,
                                        onClick = onSelectAllClick
                                    )
                                )
                            }
                            add(
                                MenuItem(
                                    R.string.delete,
                                    onClick = onDeleteSelectedClick,
                                    color = Color.Red
                                )
                            )
                        } else {
                            add(MenuItem(R.string.edit, onClick = onEditClick))
                        }

                    },
                onEditClick = {
                    onEditClick()
                },
                onSelectAllClick = {
                    onSelectAllClick()
                },
                onDeleteClick = {
                    onDeleteSelectedClick()
                }
            )
        }
    }
}

@Composable
private fun AddAvatarButton(
    isPremium: Boolean,
    onClick: () -> Unit
) {
    val localizedRes = LocalLocalizedRes.current
    Box(modifier = Modifier.fillMaxWidth().padding(start = 44.dp, end = 44.dp, top = 12.dp)) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()

        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.96f else 1f,
            label = "button-scale"
        )

        OutlinedButton(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
                .height(48.dp),
            onClick = onClick,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = White,
                containerColor = White.copy(alpha = 0.1f)
            ),
            interactionSource = interactionSource,
            border = null
        ) {
            Text(
                modifier = Modifier,
                text = "+ ${localizedRes.string(R.string.vibes_avatars_add_button)}",
                color = White,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        if (!isPremium) {
            MaxView(modifier = Modifier.align(Alignment.TopEnd).padding(end = 24.dp))
        }
    }
}


@Composable
fun LazyGridItemScope.AvatarItem(
    avatar: AvatarUi?,
    isSelected: Boolean,
    isEditMode: Boolean = false,
    isSelectedInEditMode: Boolean = false,
    shape: RoundedCornerShape = AvatarShape,
    hazeState: HazeState? = null,
    onClick: (AvatarUi?) -> Unit
) {
    val localizedRes = LocalLocalizedRes.current
    val borderWidth = if (isSelected) 2.dp else 0.dp
    val borderColor = if (isSelected) White else Color.Transparent
    val selectionBorder = if (isSelected) BorderStroke(2.dp, White) else null
    val editSelectionBorder = if (isSelectedInEditMode) BorderStroke(2.dp, White) else null
    val context = LocalContext.current
    Surface(
        shape = shape,
        color = AvatarBackground,
        border = if (!isEditMode) selectionBorder else editSelectionBorder,
        modifier = Modifier
            .aspectRatio(1f)
            .noRippleClickable { onClick(avatar) }
            .animateItem()
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
                        text = localizedRes.string(R.string.vibes_avatars_demo).uppercase(),
                        color = White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            if (isEditMode) {
                if (isSelectedInEditMode) {
                    Box(Modifier.fillMaxSize().background(DayBlue.copy(alpha = 0.2f)))
                }
                Icon(
                    painter = painterResource(id = if (isSelectedInEditMode) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_unchecked),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                )
            } else {
                if (isSelected) {
                    Box(Modifier.fillMaxSize().background(White.copy(alpha = 0.2f)))
                }
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
private fun BoxScope.AppBar(
    modifier: Modifier,
    menuItems: List<MenuItem> = emptyList(),
    onEditClick: () -> Unit,
    onSelectAllClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val localizedRes = LocalLocalizedRes.current
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .noRippleClickable {}.statusBarsPadding().padding(horizontal = 16.dp)
            .align(Alignment.TopCenter)
            .heightIn(min = AppBarHeightLarge)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.size(24.dp))
        Text(
            text = localizedRes.string(R.string.vibes_avatars_title),
            color = White,
            style = appbarMedium,
        )

        Box {
            Icon(
                modifier = Modifier.size(24.dp).noRippleClickable { expanded = !expanded },
                painter = painterResource(id = R.drawable.ic_menu),
                contentDescription = null,
                tint = Color.Unspecified
            )
            OptionsMenu(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                menuItems = menuItems
            )
        }
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

@Composable
fun CropView(imageUri: Uri, onCancelClick: () -> Unit, onDoneClick: (Uri) -> Unit) {
    val localizedRes = LocalLocalizedRes.current
    val context = LocalContext.current
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .onGloballyPositioned {
                containerSize = it.size
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale *= zoom
                        offset += pan
                    }
                }
        ) {
            AsyncImage(
                model = imageUri,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                onSuccess = {
                    imageSize = IntSize(
                        it.result.drawable.intrinsicWidth,
                        it.result.drawable.intrinsicHeight
                    )
                },
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
            )
        }

        // Cropping Circle Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        ) {
            // This is a simple way to create a transparent hole in an overlay
            // In a more complex app, we'd use a Canvas with clipPath
        }

        val cropSizeDp = 300.dp
        val cropSizePx = with(LocalDensity.current) { cropSizeDp.toPx() }

        Box(
            modifier = Modifier
                .clip(CircleShape)
                .size(cropSizeDp)
                .align(Alignment.Center)
                .border(width = 2.dp, color = Color.White, shape = CircleShape)

        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxSize()
            ) {

                VerticalDivider(
                    thickness = 1.dp,
                    color = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxHeight()
                )
                VerticalDivider(
                    thickness = 1.dp,
                    color = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxHeight()
                )
            }

            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxSize()
            ) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                )
                HorizontalDivider(
                    thickness = 1.dp,
                    color = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(NavBarBackground)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = localizedRes.string(R.string.cancel),
                color = White,
                style = appbarMedium.copy(fontWeight = FontWeight.Medium),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .noRippleClickable(onCancelClick)
                    .navigationBarsPadding()
            )
            Text(
                text = localizedRes.string(R.string.done),
                color = YellowConfirm,
                style = appbarMedium.copy(fontWeight = FontWeight.Medium),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .noRippleClickable {
                        if (imageSize != IntSize.Zero && containerSize != IntSize.Zero) {
                            val croppedUri = cropBitmap(
                                context,
                                imageUri,
                                containerSize,
                                imageSize,
                                scale,
                                offset,
                                cropSizePx
                            )
                            onDoneClick(croppedUri ?: imageUri)
                        } else {
                            onDoneClick(imageUri)
                        }
                    }
                    .navigationBarsPadding()
            )
        }
    }
}

private fun cropBitmap(
    context: Context,
    uri: Uri,
    containerSize: IntSize,
    imageSize: IntSize,
    scale: Float,
    offset: Offset,
    cropSizePx: Float
): Uri? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return null

        // 1. Calculate how the image is fitted into the container (ContentScale.Fit)
        val containerWidth = containerSize.width.toFloat()
        val containerHeight = containerSize.height.toFloat()
        val bitmapWidth = originalBitmap.width.toFloat()
        val bitmapHeight = originalBitmap.height.toFloat()

        val fitScale = Math.min(containerWidth / bitmapWidth, containerHeight / bitmapHeight)
        val fittedWidth = bitmapWidth * fitScale
        val fittedHeight = bitmapHeight * fitScale

        // 2. Center of the container
        val centerX = containerWidth / 2f
        val centerY = containerHeight / 2f

        // 3. The crop area is centered in the container
        val cropLeft = centerX - cropSizePx / 2f
        val cropTop = centerY - cropSizePx / 2f

        // 4. Map crop area coordinates to original bitmap coordinates
        // Current image rect in container coordinates:
        // Left = centerX - fittedWidth/2 * scale + offset.x
        // ... but graphicsLayer transformations are applied around the center by default if not specified otherwise.

        // Let's simplify: 
        // A point (px, py) in original bitmap corresponds to:
        // x_cont = centerX + (px - bitmapWidth/2) * fitScale * scale + offset.x
        // y_cont = centerY + (py - bitmapHeight/2) * fitScale * scale + offset.y

        // We need px, py such that x_cont = cropLeft and x_cont = cropRight etc.
        // px = bitmapWidth/2 + (x_cont - centerX - offset.x) / (fitScale * scale)

        val totalScale = fitScale * scale
        val bitmapCropLeft = bitmapWidth / 2f + (cropLeft - centerX - offset.x) / totalScale
        val bitmapCropTop = bitmapHeight / 2f + (cropTop - centerY - offset.y) / totalScale
        val bitmapCropSize = cropSizePx / totalScale

        val srcRect = Rect(
            bitmapCropLeft.toInt().coerceIn(0, originalBitmap.width),
            bitmapCropTop.toInt().coerceIn(0, originalBitmap.height),
            (bitmapCropLeft + bitmapCropSize).toInt().coerceIn(0, originalBitmap.width),
            (bitmapCropTop + bitmapCropSize).toInt().coerceIn(0, originalBitmap.height)
        )

        val croppedBitmap = Bitmap.createBitmap(
            originalBitmap,
            srcRect.left,
            srcRect.top,
            srcRect.width(),
            srcRect.height()
        )

        val file = File(context.cacheDir, "cropped_avatar_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }

        Uri.fromFile(file)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}