package com.bigcall.appa

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.widget.LinearLayout
import android.widget.TextView

/** Small helpers for building large, high-contrast UI in code (no XML layouts to break). */
object Ui {
    fun dp(c: Context, v: Int): Int = (v * c.resources.displayMetrics.density).toInt()

    fun scaledSp(v: Float): Float = v * AppData.textScale

    fun roundRect(color: Int, radius: Float, strokeColor: Int? = null, strokeW: Int = 0): GradientDrawable {
        val d = GradientDrawable()
        d.setColor(color)
        d.cornerRadius = radius
        if (strokeColor != null && strokeW > 0) d.setStroke(strokeW, strokeColor)
        return d
    }

    fun circle(color: Int, strokeColor: Int? = null, strokeW: Int = 0): GradientDrawable {
        val d = GradientDrawable()
        d.shape = GradientDrawable.OVAL
        d.setColor(color)
        if (strokeColor != null && strokeW > 0) d.setStroke(strokeW, strokeColor)
        return d
    }

    /** Crop a bitmap into a circle for contact photos. */
    fun circleBitmap(src: Bitmap): Bitmap {
        val size = minOf(src.width, src.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint().apply { isAntiAlias = true }
        val rect = Rect(0, 0, size, size)
        val rectF = RectF(rect)
        canvas.drawOval(rectF, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        val srcRect = Rect(
            (src.width - size) / 2,
            (src.height - size) / 2,
            (src.width - size) / 2 + size,
            (src.height - size) / 2 + size
        )
        canvas.drawBitmap(src, srcRect, rect, paint)
        return output
    }

    fun spacer(c: Context, heightDp: Int): TextView {
        val t = TextView(c)
        t.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(c, heightDp)
        )
        return t
    }
}
