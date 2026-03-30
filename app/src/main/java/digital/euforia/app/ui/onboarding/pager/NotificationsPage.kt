package digital.euforia.app.ui.onboarding.pager

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants.IterateForever
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import digital.euforia.app.R
import digital.euforia.app.ui.onboarding.OnboardingViewModel
import digital.euforia.app.ui.util.LocalLocalizedRes

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NotificationsPage(
    viewModel: OnboardingViewModel,
    isPageOpened: () -> Boolean
) {
    val localizedRes = LocalLocalizedRes.current
//    var permissionRequested by remember { mutableStateOf(false) }
//
//    // Only request notification permission on Android 13+ (API 33+)
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//        val permissionState = rememberPermissionState(
//            permission = Manifest.permission.POST_NOTIFICATIONS
//        )
//
//        LaunchedEffect(isPageOpened()) {
//            if (isPageOpened() && !permissionRequested && !permissionState.status.isGranted) {
//                permissionRequested = true
//                permissionState.launchPermissionRequest()
//            }
//        }

        NotificationPageContent(
            isPageOpened = isPageOpened,
//            permissionStatus = permissionState.status,
//            onRequestPermission = {
//                permissionRequested = true
//                permissionState.launchPermissionRequest()
//            }
        )
//    } else {
//        // For older Android versions, just show the animation
//        NotificationPageContent(
//            isPageOpened = isPageOpened,
//            permissionStatus = null,
//            onRequestPermission = {}
//        )
//    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun NotificationPageContent(
    isPageOpened: () -> Boolean,
//    permissionStatus: com.google.accompanist.permissions.PermissionStatus?,
//    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimationView(shouldAnimate = isPageOpened)

        Spacer(modifier = Modifier.height(16.dp))

//        // Show permission request button if needed
//        if (permissionStatus != null && !permissionStatus.isGranted) {
//            if (permissionStatus.shouldShowRationale) {
//                Text(
//                    text = stringResource(R.string.request_permissions_notifications_text),
//                    style = MaterialTheme.typography.bodyMedium,
//                    modifier = Modifier.padding(bottom = 8.dp)
//                )
//            }
//
//            Button(
//                onClick = onRequestPermission,
//                modifier = Modifier.padding(top = 8.dp)
//            ) {
//                Text(stringResource(R.string.request_permissions_notifications_button))
//            }
//        }
    }
}

@Composable
private fun AnimationView(shouldAnimate: () -> Boolean) {
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.anim_notifications))
    LottieAnimation(
        composition = composition,
        iterations = IterateForever,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 78.dp)
            .navigationBarsPadding(),
        contentScale = ContentScale.FillWidth,
        isPlaying = shouldAnimate()
    )
}
