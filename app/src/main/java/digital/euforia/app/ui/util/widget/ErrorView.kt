package digital.euforia.app.ui.util.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import digital.euforia.app.R
import digital.euforia.app.data.network.NetworkNotAvailableException
import digital.euforia.app.ui.theme.NavBarBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import retrofit2.HttpException

@Composable
fun ErrorView(
    modifier: Modifier = Modifier,
    state: ErrorViewState = ErrorViewState.DefaultError,
    onRetryClick: () -> Unit = { },
    onDownloadsClick: () -> Unit = { }
) {
    val localizedRes = LocalLocalizedRes.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (state) {
            is ErrorViewState.EmptyState -> {
                Text(
                    text = localizedRes.string(state.getMessageRes()),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 48.dp),
                    color = White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }

            else -> {
                Text(
                    text = localizedRes.string(state.getTitleRes()),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = localizedRes.string(state.getMessageRes()),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 48.dp),
                    color = White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                FilledTonalButton(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 56.dp).height(48.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = White.copy(alpha = 0.1f),
                        contentColor = White
                    ),
                    onClick = onRetryClick
                ) {
                    Text(
                        text = localizedRes.string(R.string.text_load_again),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                FilledTonalButton(
                    modifier = Modifier.fillMaxWidth()
                        .padding(start = 56.dp, end = 56.dp, top = 12.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = White,
                        contentColor = NavBarBackground
                    ),
                    onClick = onDownloadsClick
                ) {
                    Text(
                        text = localizedRes.string(R.string.go_to_downloads),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = NavBarBackground
                    )
                }
            }
        }
    }
}

fun ErrorViewState.getTitleRes(): Int {
    return when (this) {
        ErrorViewState.NetworkError -> R.string.no_internet_connection_title
        else -> R.string.failed_load_data_title
    }
}

fun ErrorViewState.getMessageRes(): Int {
    return when (this) {
        ErrorViewState.NetworkError -> R.string.no_internet_connection_message
        ErrorViewState.NotFoundError -> R.string.resource_not_found_empty_text
        else -> R.string.no_data_empty_text
    }
}

fun Throwable.mapToErrorViewState() : ErrorViewState {
    return when (this) {
        // in case of network error -> ErrorViewState.NetworkError
        // in case of 404 not found error -> ErrorViewState.NotFoundError
        is NetworkNotAvailableException -> ErrorViewState.NetworkError
        is HttpException -> if (this.code() == 404) {
            ErrorViewState.NotFoundError
        } else {
            ErrorViewState.DefaultError
        }

        else -> ErrorViewState.DefaultError
    }
}

sealed class ErrorViewState {
    data object DefaultError : ErrorViewState()
    data object NetworkError : ErrorViewState()
    data object NotFoundError : ErrorViewState()
    data object EmptyState : ErrorViewState()
}