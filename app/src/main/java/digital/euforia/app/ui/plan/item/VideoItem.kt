package digital.euforia.app.ui.plan.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import digital.euforia.app.R
import digital.euforia.app.ui.plan.PlanViewItems
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.widget.noRippleClickable

fun LazyListScope.videoItem(url: String?, onClick: () -> Unit) =
    item(key = PlanViewItems.VIDEO, contentType = PlanViewItems.VIDEO) {

        val localizedRes = LocalLocalizedRes.current
        url?.let {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                Text(
                    text = localizedRes.string(R.string.today_info_video_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = Bold),
                    color = White,
                    modifier = Modifier.padding(
                        start = 16.dp,
                        top = 32.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    )
                )

                Box(
                    modifier = Modifier
                        .noRippleClickable(onClick = onClick)
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    AsyncImage(
                        modifier = Modifier.fillMaxWidth(),
                        model = url,
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                    )

                    Icon(
                        modifier = Modifier
                            .align(Center)
                            .background(
                                shape = CircleShape,
                                color = White.copy(alpha = 0.6f)
                            )
                            .padding(8.dp)
                            .size(24.dp),
                        painter = painterResource(id = R.drawable.ic_play),
                        tint = Color.Unspecified,
                        contentDescription = null
                    )
                }
            }
        }
    }