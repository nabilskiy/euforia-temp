package digital.euforia.app.ui.util.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import digital.euforia.app.ui.theme.NavBarBackground
import digital.euforia.app.ui.theme.White


@Composable
fun OptionsMenu(
    expanded: Boolean, onExpandedChange: (Boolean) -> Unit, menuItems: List<MenuItem>
) {
    DropdownMenu(
        containerColor = NavBarBackground,
        shape = RoundedCornerShape(20.dp),
        expanded = expanded,
        onDismissRequest = { onExpandedChange(false) }
    ) {
        menuItems.forEachIndexed { index, item ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(item.titleRes),
                        color = White,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal)
                    )
                },
                onClick = {
                    item.onClick()
                    onExpandedChange(false)
                }
            )
            if (index != menuItems.lastIndex) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(1.dp)
                        .background(White.copy(alpha = 0.2f))
                )
            }
        }
    }
}

data class MenuItem(
    val titleRes: Int,
    val onClick: () -> Unit
)