package digital.euforia.app.ui.util.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import digital.euforia.app.R
import digital.euforia.app.ui.theme.White

@Composable
fun AnimatedListCheckItem(
    isSelected: Boolean,
    minHeight: Dp = 82.dp,
    padding: PaddingValues = PaddingValues(16.dp),
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    borderWidth: Dp = 1.dp,
    selectedBorderWidth: Dp = 2.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {

    val borderWidth = if (isSelected) selectedBorderWidth else borderWidth
    val borderColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.1f)
    val backgroundColor =
        if (isSelected) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)

    AnimatedSizeBox(onClick = onClick) {
        Row(
            modifier = Modifier
                .heightIn(min = minHeight)
                .background(color = backgroundColor, shape = shape)
                .border(width = borderWidth, shape = shape, color = borderColor)
                .padding(padding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(12.dp)
        ) {
            content()

        }
    }
}