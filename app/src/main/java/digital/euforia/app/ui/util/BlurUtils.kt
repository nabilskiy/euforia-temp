package digital.euforia.app.ui.util

import android.content.Context
import android.graphics.Bitmap

@Suppress("DEPRECATION")
fun blurBitmapRS(context: Context, src: Bitmap, radius: Float): Bitmap {
    // radius в ScriptIntrinsicBlur має бути в діапазоні (0; 25]
    val r = radius.coerceIn(0.1f, 25f)

    val inputBmp = if (src.config == Bitmap.Config.ARGB_8888 && src.isMutable)
        src else src.copy(Bitmap.Config.ARGB_8888, true)

    val rs = android.renderscript.RenderScript.create(context)
    val inAlloc = android.renderscript.Allocation.createFromBitmap(
        rs, inputBmp,
        android.renderscript.Allocation.MipmapControl.MIPMAP_NONE,
        android.renderscript.Allocation.USAGE_SCRIPT
    )
    val outAlloc = android.renderscript.Allocation.createTyped(rs, inAlloc.type)
    val blur = android.renderscript.ScriptIntrinsicBlur.create(
        rs, android.renderscript.Element.U8_4(rs)
    )
    blur.setRadius(r)
    blur.setInput(inAlloc)
    blur.forEach(outAlloc)
    outAlloc.copyTo(inputBmp)
    rs.destroy()
    return inputBmp
}