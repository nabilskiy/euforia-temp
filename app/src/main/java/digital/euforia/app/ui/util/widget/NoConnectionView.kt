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
import digital.euforia.app.ui.theme.NavBarBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes

class NoConnectionView {
}

@Composable
fun NoConnectionView(
    modifier: Modifier = Modifier,
    titleRes: Int = R.string.no_internet_connection_title,
    messageRes: Int = R.string.no_internet_connection_message,
    onRetryClick: () -> Unit = { },
    onDownloadClick: () -> Unit = { }
) {
    val localizedRes = LocalLocalizedRes.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = localizedRes.string(titleRes),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            textAlign = TextAlign.Center
        )
        Text(
            text = localizedRes.string(messageRes),
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
            modifier = Modifier.fillMaxWidth().padding(start = 56.dp, end = 56.dp,  top = 12.dp)
                .height(48.dp),
//            contentPadding = PaddingValues(vertical = 12.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = White,
                contentColor = NavBarBackground
            ),
            onClick = onDownloadClick
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