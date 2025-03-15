package com.example.staysafe.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.OptIn
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult

@OptIn(UnstableApi::class)
suspend fun getBitmapFromUrl(context: Context, imageUrl: String): Bitmap? {
    Log.d("BitMapUrl", "getBitmapFromUrl called with imageUrl: $imageUrl")
    val loader = ImageLoader(context)
    val request = ImageRequest.Builder(context)
        .data(imageUrl)
        .allowHardware(false)
        .build()

    val result = loader.execute(request)
    return if (result is SuccessResult) {
        drawableToBitmap(result.drawable)
    } else {
        null
    }
}

// Helper function to convert any drawable to a Bitmap
fun drawableToBitmap(drawable: Drawable): Bitmap {
    return if (drawable is BitmapDrawable) {
        drawable.bitmap
    } else {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth.takeIf { it > 0 } ?: 100,
            drawable.intrinsicHeight.takeIf { it > 0 } ?: 100,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap
    }
}
