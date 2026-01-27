package digital.euforia.app.ui.programs.programdetails

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.HazeMaterials
import digital.euforia.app.R
import digital.euforia.app.ui.theme.AppBarBackground
import digital.euforia.app.ui.theme.NavBarBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes

@Composable
fun AboutPopup(
    text: String,
    hazeState: HazeState,
    onClick: () -> Unit = {}
) {
    BackHandler(
        enabled = true,
        onBack = onClick
    )
    val localizedRes = LocalLocalizedRes.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .hazeEffect(
                state = hazeState,
                style = HazeMaterials.regular(AppBarBackground)
            )
            .zIndex(1f)

    ) {
        Column(
            modifier = Modifier.align(Alignment.Center).fillMaxWidth()
                .padding(20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(NavBarBackground)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = localizedRes.string(R.string.package_info_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = White
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onClick
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = null,
                        tint = White.copy(alpha = 0.6f)
                    )
                }
            }

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = White.copy(alpha = 0.8f)
            )
        }
    }
}