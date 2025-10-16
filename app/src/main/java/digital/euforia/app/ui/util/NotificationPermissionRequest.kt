package digital.euforia.app.ui.util
import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NotificationPermissionRequest(
    onGranted: () -> Unit
) {
    // На Android < 13 пермішн не потрібен
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        onGranted()
        return
    }

    val context = LocalContext.current
    val permission = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    var requestedOnce by rememberSaveable { mutableStateOf(false) }

    when {
        permission.status.isGranted -> onGranted()

        permission.status.shouldShowRationale -> {
            LaunchedEffect(Unit) {
                if (!requestedOnce) {
                    requestedOnce = true
                    permission.launchPermissionRequest()
                }
            }
//            Column {
//                Text("Для отримання сповіщень надайте доступ.")
//                Button(onClick = {
//                    requestedOnce = true
//                    permission.launchPermissionRequest()
//                }) { Text("Дозволити сповіщення") }
//            }
        }

        // !shouldShowRationale: або перший запит, або “Don’t ask again”.
        // Розрізняємо простим прапорцем requestedOnce.
        requestedOnce -> {
//            Column {
//                Text("Доступ заборонено. Увімкніть сповіщення в налаштуваннях.")
//                Button(onClick = {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
//                }) { Text("Відкрити налаштування") }
//            }
        }

        else -> {
//            // Перше відвідування — показуємо коротке пояснення і кнопку
//            Column {
//                Text("Цей застосунок надсилає корисні сповіщення.")
//                Button(onClick = {
            LaunchedEffect(Unit) {
                requestedOnce = true
                permission.launchPermissionRequest()
            }
//                }) { Text("Продовжити") }
//            }
        }
    }
}
