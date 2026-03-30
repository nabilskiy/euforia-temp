package digital.euforia.app.ui.programs.article

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.colintheshots.twain.MarkdownText
import digital.euforia.app.R
import digital.euforia.app.ui.programs.publication.PublicationType
import digital.euforia.app.ui.theme.BottomSheetBackground
import digital.euforia.app.ui.theme.NavBarBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.sharePublication
import digital.euforia.app.ui.util.widget.PublicationOptionMenu
import org.orbitmvi.orbit.compose.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleBottomSheet(
    viewModel: ArticleViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val state by viewModel.collectAsState()
    val bodyTextColor = White.copy(alpha = 0.8f)
    val secondaryTextColor = White.copy(alpha = 0.5f)

    LaunchedEffect(sheetState.isVisible) {
        if (sheetState.isVisible) {
            viewModel.loadArticleContent()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.padding(top = 16.dp).statusBarsPadding(),
        dragHandle = {},
        scrimColor = Color.Transparent,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = BottomSheetBackground,
        tonalElevation = 12.dp,
    ) {
        val context: Context = LocalContext.current

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onDismiss, modifier = Modifier) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = null,
                        tint = White
                    )
                }

                var isExpanded by remember { mutableStateOf(false) }
                PublicationOptionMenu(
                    expanded = isExpanded,
                    onExpandedChange = { isExpanded = it },
                    isFavourite = state.publicationInfo?.isFavourite ?: false,
                    onAddFavouriteClick = { viewModel.onFavouriteClicked() },
                    onShareClick = {
                        viewModel.onShareClicked()
                        sharePublication(
                            context = context,
                            publicationType = PublicationType.ARTICLE,
                            id = state.publicationInfo?.id ?: 0,
                        )
                    }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (state.articleBody.isNotEmpty()) {
                    articleItem(
                        articleBody = state.articleBody,
                        textColor = bodyTextColor
                    )
                    footerItem(
                        imageUrl = state.publicationInfo?.imageUrl.orEmpty(),
                        title = state.publicationInfo?.title.orEmpty(),
                        secondaryTextColor = secondaryTextColor,
                        isFavourite = state.publicationInfo?.isFavourite ?: false,
                        onShareClick = {
                            viewModel.onShareClicked()
                            sharePublication(
                                context = context,
                                publicationType = PublicationType.ARTICLE,
                                id = state.publicationInfo?.id ?: 0,
                            )
                        },
                        onFavouriteClick = { viewModel.onFavouriteClicked() }
                    )
                }
            }
        }
    }
}

private fun LazyListScope.articleItem(
    articleBody: String,
    textColor: Color
) = item(key = "article") {
    MarkdownText(
        markdown = articleBody.trimIndent(),
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize(),
        color = textColor,
        fontResource = R.font.inter_regular
    )
}

private fun LazyListScope.footerItem(
    imageUrl: String,
    title: String,
    secondaryTextColor: Color,
    isFavourite: Boolean,
    onShareClick: () -> Unit,
    onFavouriteClick: () -> Unit
) = item(key = "footer") {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .background(color = NavBarBackground)
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val localizedRes = LocalLocalizedRes.current
        AsyncImage(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(24.dp)),
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop
        )


        Text(
            text = title,
            color = White,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )

        Text(
            modifier = Modifier.padding(bottom = 12.dp),
            text = localizedRes.string(R.string.article_reader_share_label),
            color = secondaryTextColor,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal)
        )
        ArticleButton(
            textRes = if (isFavourite) R.string.remove_from_favorites else R.string.add_to_favorites,
            onClick = onFavouriteClick
        )
        ArticleButton(
            textRes = R.string.share,
            onClick = onShareClick
        )
    }
}

@Composable
private fun ArticleButton(textRes: Int, onClick: () -> Unit) {
    val localizedRes = LocalLocalizedRes.current

    FilledTonalButton(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 56.dp).height(48.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = White.copy(alpha = 0.1f),
            contentColor = White
        ),
        onClick = onClick
    ) {
        Text(
            text = localizedRes.string(textRes),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
    }
}