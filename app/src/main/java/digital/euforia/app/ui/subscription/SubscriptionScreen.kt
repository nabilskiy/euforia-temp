//package digital.euforia.app.ui.subscription
//
//import android.view.View
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.fragment.app.FragmentActivity
//import androidx.fragment.app.FragmentContainerView
//import digital.euforia.app.ui.subscription.subs2.Subscription2Fragment
//
//@Composable
//fun SubscriptionScreen(
//    navController: androidx.navigation.NavHostController,
//    from: String, tag: String
//) {
//
//    val fragment = remember { newInstance(screenId = 2, from = null, tag = null) }
//
//    AndroidView(
//        factory = { context ->
//            val containerId = View.generateViewId()
//
//            FragmentContainerView(context).apply {
//                id = containerId
//                val activity = context as? FragmentActivity
//                activity?.supportFragmentManager
//                    ?.beginTransaction()
//                    ?.replace(id, fragment)
//                    ?.commitNow()
//            }
//        },
//        modifier = Modifier.fillMaxSize()
//    )
//}
//
//fun newInstance(
//    screenId: Int,
//    from: String?,
//    tag: String?
//): SubscriptionFragment {
//    when (screenId) {
//        8 -> return Subscription8Fragment.newInstance(from, tag)
//        7 -> return Subscription7Fragment.newInstance(from, tag)
//        6 -> return Subscription6Fragment.newInstance(from, tag)
//        3 -> return Subscription3Fragment.newInstance(from, tag)
//        2 -> return Subscription2Fragment.newInstance(from, tag)
//        else -> return Subscription1Fragment.newInstance(from, tag)
//    }
//}