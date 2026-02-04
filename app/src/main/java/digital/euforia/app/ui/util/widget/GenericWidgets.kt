package digital.euforia.app.ui.util.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.Medium
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.euforia.app.R
import digital.euforia.app.ui.programs.HorizontalItemView
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes


fun <T> LazyListScope.genericRowItem(
    items: List<T>,
    title: String,
    description: String?,
    isPremium: Boolean,
    iconRes: Int,
    itemId: (T) -> Int,
    itemAlias: (T) -> String,
    itemTitle: (T) -> String,
    isPremiumContent: (T) -> Boolean,
    itemImageUrl: (T) -> String?,
    itemDuration: (T) -> Int,
    onMoreClick: () -> Unit,
    onItemClick: (T) -> Unit
) = item(key = "${title.lowercase()}_item") {

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        description?.let {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = description.uppercase(),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = Medium),
                color = White.copy(alpha = 0.6f),
            )
        }
        Text(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 6.dp),
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = Bold),
            color = White,
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            items.forEach { item ->
                item(key = "item_${itemAlias(item)}_${itemId(item)}") {
                    HorizontalItemView(
                        imageUrl = itemImageUrl(item),
                        isPremiumContent = isPremiumContent(item),
                        titleText = itemTitle(item),
                        duration = itemDuration(item),
                        isPremium = isPremium,
                        iconRes = iconRes,
                        onClick = { onItemClick(item) }
                    )
                }

            }
            if (items.size >= 5) moreItem(onMoreClick)
        }
    }
}


private fun LazyListScope.moreItem(onClick: () -> Unit) = item {
    val localizedRes = LocalLocalizedRes.current
    Column(
        modifier = Modifier.noRippleClickable { onClick() }.height(196.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            modifier = Modifier.padding(bottom = 8.dp).size(32.dp)
                .background(color = White.copy(alpha = 0.1f), shape = CircleShape).padding(4.dp),
            painter = painterResource(R.drawable.ic_next),
            contentDescription = null,
            tint = White.copy(alpha = 0.6f)
        )
        Text(
            text = localizedRes.string(R.string.read_more),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.titleSmall.copy(
                fontSize = 12.sp,
                fontWeight = SemiBold,
            ),
            color = White.copy(alpha = 0.6f),
        )
    }
}

