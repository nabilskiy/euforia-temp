/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.ui.soundscapes.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import digital.euforia.app.R
import digital.euforia.app.ui.theme.White

@Composable
fun SceneSoundFloatingButton(
    imageUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val borderColor = White.copy(alpha = 0.95f)
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .border(1.dp, borderColor, CircleShape)
            .background(Color.Black.copy(alpha = 0.42f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                modifier = Modifier
                    .padding(10.dp)
                    .size(36.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_sounds),
                contentDescription = contentDescription,
                tint = White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
