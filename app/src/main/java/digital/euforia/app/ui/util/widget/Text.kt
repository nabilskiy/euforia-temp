package digital.euforia.app.ui.util.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import digital.euforia.app.R
import digital.euforia.app.ui.theme.MaxBackground
import digital.euforia.app.ui.theme.MaxGradient
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.LocalizedResources

@Composable
fun MaxTextView(modifier: Modifier = Modifier) {
    val localizedRes = LocalLocalizedRes.current
    Box(modifier = modifier) {
        Text(
            modifier = Modifier.background(MaxBackground, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            text = localizedRes.string(R.string.cell_premium_lock),
            style = MaterialTheme.typography.labelMedium.copy(
                brush = MaxGradient
            )
        )
    }

}