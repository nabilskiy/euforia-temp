package digital.euforia.app.ui.sos.contacts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.chrisbanes.haze.hazeSource
import digital.euforia.app.R
import digital.euforia.app.data.analytics.AnalyticSender
import digital.euforia.app.domain.model.emergency.EmergencyContact
import digital.euforia.app.ui.navigation.NavBarlessScreen
import digital.euforia.app.ui.player.audio.AppBarHeightMedium
import digital.euforia.app.ui.sos.EmergencyVideoUi
import digital.euforia.app.ui.theme.DayBlue
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.callPhoneNumber
import digital.euforia.app.ui.util.widget.BlurredAppBar
import digital.euforia.app.ui.util.widget.noRippleClickable
import digital.euforia.app.ui.util.widget.titleItem
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun ContactsScreen(
    navController: NavHostController,
    viewModel: ContactsViewModel,
    navBarVisibilityState: MutableState<Boolean>,
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect)
    }
    NavBarlessScreen(navBarVisibilityState) {
        ContactsContent(
            emergencyContacts = state.contactsList,
            navController = navController,
            analyticSender = viewModel.analyticSender
        )
    }
}

@Composable
private fun ContactsContent(
    emergencyContacts: List<EmergencyContact> = emptyList(),
    navController: NavHostController,
    analyticSender: AnalyticSender
) {
    val listState = rememberLazyListState()
    val hazeState = dev.chrisbanes.haze.rememberHazeState()
    val density = LocalDensity.current
    val thresholdPx = with(density) { 16.dp.roundToPx() }
    val shouldBlur by remember(listState) {
        derivedStateOf {
            val firstIndex = listState.firstVisibleItemIndex
            val firstOffset = listState.firstVisibleItemScrollOffset
            firstIndex > 0 || firstOffset > thresholdPx
        }
    }
    Box() {
        BlurredAppBar(
            titleRes = R.string.sos_call_title,
            shouldBlur = shouldBlur,
            hazeState = hazeState,
            backTitleRes = R.string.sos_title,
            onBackClick = { navController.popBackStack() },
            navController = navController
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize().hazeSource(hazeState),
            state = listState,
            verticalArrangement = Arrangement.Absolute.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = AppBarHeightMedium + 16.dp,
                bottom = 56.dp
            ),
        ) {
            titleItem(R.string.sos_call_title)
            item(key = "contact_item") {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .background(
                            color = White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)

                ) {
                    emergencyContacts.forEachIndexed { index, emergencyContact ->
                        contactItem(
                            emergencyContact = emergencyContact,
                            isLast = index == emergencyContacts.size - 1,
                            onClick = { analyticSender.sosContactClick() }
                        )
//                        if (emergencyContact != emergencyContacts.last()) {
//                            HorizontalDivider(
//                                modifier = Modifier.fillMaxWidth(),
//                                color = White.copy(alpha = 0.2f)
//                            )
//                        }
                    }

                }
            }
        }
    }
}

@Composable
private fun contactItem(emergencyContact: EmergencyContact, isLast: Boolean, onClick: () -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    val iconAngle by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "faq_icon_rotation"
    )
    Column(modifier = Modifier.noRippleClickable {
        onClick()
        isExpanded = !isExpanded
    }.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = emergencyContact.countryNative,
                color = White,
                style = MaterialTheme.typography.bodyLarge
            )
            Icon(
                painter = painterResource(R.drawable.ic_next),
                contentDescription = null,
                tint = White,
                modifier = Modifier.rotate(iconAngle)
            )
        }
        if (!isLast) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = White.copy(alpha = 0.2f)
            )
        }
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier.padding(vertical = 12.dp),
                verticalArrangement = spacedBy(12.dp)
            ) {
                emergencyContact.contacts.forEachIndexed { index, contact ->
                    ContactView(contact)
                }
            }
        }
    }
}

@Composable
private fun ContactView(contact: EmergencyContact.Contact) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.noRippleClickable(
            onClick = {
                callPhoneNumber(context, contact.phone)
            }
        ).padding(start = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_phone),
                contentDescription = null,
                tint = DayBlue
            )
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = contact.phone,
                color = DayBlue,
                style = MaterialTheme.typography.titleLarge
            )
        }

        Text(
            text = contact.name,
            color = White,
            style = MaterialTheme.typography.bodySmall
        )
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = White.copy(alpha = 0.2f)
        )
    }
}

private fun handleSideEffect(sideEffect: ContactsSideEffect) {
    when (sideEffect) {
        else -> {}
    }
}