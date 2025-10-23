package digital.euforia.app.ui.plan.item

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import digital.euforia.app.R
import digital.euforia.app.data.db.entity.Package
import digital.euforia.app.domain.model.plan.DailyTask
import digital.euforia.app.domain.model.plan.RankedPackage
import digital.euforia.app.ui.plan.PlanViewItems
import digital.euforia.app.ui.theme.Black
import digital.euforia.app.ui.theme.DescriptionText
import digital.euforia.app.ui.theme.FirstRank
import digital.euforia.app.ui.theme.ProgramButtonContainer
import digital.euforia.app.ui.theme.SecondRank
import digital.euforia.app.ui.theme.ThirdRank
import digital.euforia.app.ui.theme.White

fun LazyListScope.topProgramsItem(
    isDemo: Boolean,
    topPrograms: List<RankedPackage>
) = item(key = PlanViewItems.TOP, contentType = PlanViewItems.TOP) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = stringResource(R.string.today_info_step_programs_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = Bold),
            color = White,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(start = 16.dp, top = 64.dp, end = 16.dp, bottom = 8.dp)
                .fillMaxWidth()
        )

        topPrograms.forEach { pkg ->
            ProgramItem(pkg)
        }

        ProgramButton { }
    }
}

@Composable
private fun ProgramItem(pkg: RankedPackage) {
    Row(
        modifier = Modifier,
        horizontalArrangement = spacedBy(16.dp)
    ) {
        Box(modifier = Modifier.weight(0.3f)) {
            Text(
                text = pkg.rank.toString(),
                color = when (pkg.rank) {
                    1 -> FirstRank
                    2 -> SecondRank
                    else -> ThirdRank
                },
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 70.sp
                ),
            )
            AsyncImage(
                modifier = Modifier
                    .padding(top = 12.dp, start = 24.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop,
                model = pkg.imageUrl,
                contentDescription = null,
            )
        }

        Column(
            modifier = Modifier.weight(0.7f).padding(top = 12.dp),
            verticalArrangement = spacedBy(8.dp)
        ) {
            Text(
                text = pkg.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = SemiBold),
                color = White,
            )
            Text(
                text = pkg.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = Ellipsis,
                color = DescriptionText
            )
        }
    }
}

@Composable
fun ProgramButton(onClick: () -> Unit) {
    FilledTonalButton(
        modifier = Modifier.padding(top = 24.dp),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        onClick = onClick,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = ProgramButtonContainer,
        ),
    ) {
        Text(
            modifier = Modifier,
            text = stringResource(R.string.today_info_step_programs_button),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = SemiBold),
            maxLines = 3,
            overflow = Ellipsis,
            color = White
        )
    }
}