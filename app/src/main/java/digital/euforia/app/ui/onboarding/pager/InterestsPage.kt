package digital.euforia.app.ui.onboarding.pager

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import digital.euforia.app.R
import digital.euforia.app.domain.model.onboarding.Interest
import digital.euforia.app.ui.onboarding.OnboardingViewModel
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.painterForImageName
import digital.euforia.app.ui.util.widget.AnimatedListCheckItem
import digital.euforia.app.ui.util.widget.fadeTop

@Composable
fun InterestsPage(
    viewModel: OnboardingViewModel,
    interests: List<Interest>,
    selectedInterests: Set<Int>,
) {
    InterestsPageContent(
        selectedInterests = selectedInterests,
        interests = interests,
        onInterestToggled = viewModel::onInterestToggled
    )
}

@Composable
private fun InterestsPageContent(
    selectedInterests: Set<Int>,
    interests: List<Interest>,
    onInterestToggled: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // 2 columns
        modifier = Modifier.fillMaxSize()
            .fadeTop(),
        horizontalArrangement = spacedBy(16.dp),
        verticalArrangement = spacedBy(16.dp),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 30.dp,
            end = 16.dp,
            bottom = 108.dp
        ),
    ) {
        itemsIndexed(interests) { index, interest ->
            InterestItemView(
                interest = interest,
                isSelected = selectedInterests.contains(index),
                onClick = { onInterestToggled(index) }
            )
        }

        item { Spacer(Modifier.fillMaxWidth().navigationBarsPadding()) }
    }
}

@Composable
private fun InterestItemView(
    interest: Interest,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
//    val shape = RoundedCornerShape(32.dp)
    val checkIconRes =
        if (isSelected) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_unchecked

    AnimatedListCheckItem(
        isSelected = isSelected,
        minHeight = 64.dp,
        padding = PaddingValues(8.dp),
//        shape = shape,
        selectedBorderWidth = 1.dp,
        borderWidth = 0.dp,
        onClick = onClick
    ) {
        Column(
            verticalArrangement = spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                Image(
                    modifier = Modifier.clip(RoundedCornerShape(16.dp)),
                    painter = painterForImageName(context, interest.imageName),
                    contentDescription = null,
                    //                modifier = Modifier.size(48.dp)
                )
                Icon(
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(32.dp),
                    painter = painterResource(id = checkIconRes),
                    contentDescription = null,
                    tint = Color.Unspecified,
                )
            }

            Text(
                modifier = Modifier.padding(vertical = 10.dp),
                text = interest.name,
                color = White,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

    }
}