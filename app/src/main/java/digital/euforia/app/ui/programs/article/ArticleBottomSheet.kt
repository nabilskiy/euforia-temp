package digital.euforia.app.ui.programs.article

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import digital.euforia.app.R
import digital.euforia.app.domain.model.article.ArticleBlock
import digital.euforia.app.ui.theme.BottomSheetBackground
import digital.euforia.app.ui.theme.White
import org.orbitmvi.orbit.compose.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleBottomSheet(
    viewModel: ArticleViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val state by viewModel.collectAsState()

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
        Column(modifier = Modifier.fillMaxSize()) {
            Icon(
                modifier = Modifier.padding(top = 16.dp),
                painter = painterResource(R.drawable.ic_close),
                contentDescription = null,
                tint = White
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.articleBlocks.forEachIndexed { index, block ->
                    when (block) {
                        is ArticleBlock.Paragraph -> {
                            item {
                                TextItem(textBlock = block)
                            }

                        }
                        is ArticleBlock.H1 -> {
                            item {
                                Text(
                                    text = block.text,
                                    color = White
                                )
                            }
                        }
                        is ArticleBlock.H2 -> {
                            item {
                                Text(
                                    text = block.text,
                                    color = White
                                )
                            }
                        }
                        is ArticleBlock.H3 -> {
                            item {
                                Text(
                                    text = block.text,
                                    color = White
                                )
                            }
                        }
                        is ArticleBlock.Bullet -> {
                            item {
                                Text(
                                    text = "• ${block.text}",
                                    color = White
                                )
                            }
                        }
                        else -> {
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TextItem(textBlock: ArticleBlock.Paragraph) {
    Text(
        text = textBlock.text,
        color = White
    )
}