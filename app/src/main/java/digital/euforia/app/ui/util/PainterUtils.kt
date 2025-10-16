package digital.euforia.app.ui.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import digital.euforia.app.R

@Composable
fun painterForImageName(context: Context, imageName: String): Painter {
    val resId = context.getDrawableResIdOrStub(imageName)
    return painterResource(id = resId)
}

fun Context.getDrawableResIdOrStub(imageName: String): Int {
    val id = resources.getIdentifier(imageName, "drawable", packageName)
    return if (id != 0) id else R.drawable.img_intro_scene_preview_1
}