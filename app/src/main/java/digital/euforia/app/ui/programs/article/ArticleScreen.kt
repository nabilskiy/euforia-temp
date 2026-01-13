package digital.euforia.app.ui.programs.article

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import digital.euforia.app.domain.model.article.ArticleBlock
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun ArticleScreen(
    navController: NavHostController,
    viewModel: ArticleViewModel
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect)
    }

    ArticleContent(articleBlocks = state.articleBlocks)
}

@Composable
private fun ArticleContent(articleBlocks: List<ArticleBlock>) {
}

private fun handleSideEffect(sideEffect: ArticleSideEffect) {
    when (sideEffect) {
        else -> {}
    }
}