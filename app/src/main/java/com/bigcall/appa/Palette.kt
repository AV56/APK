package com.bigcall.appa

import android.graphics.Color

/** High-contrast colour sets chosen for Retinitis Pigmentosa (low contrast sensitivity). */
object Palette {
    fun bg(): Int = when (AppData.theme) {
        2 -> Color.WHITE
        else -> Color.BLACK
    }

    fun fg(): Int = when (AppData.theme) {
        1 -> Color.WHITE
        2 -> Color.BLACK
        else -> Color.parseColor("#FFEB3B") // bright yellow
    }

    fun card(): Int = when (AppData.theme) {
        2 -> Color.parseColor("#ECECEC")
        else -> Color.parseColor("#1E1E1E")
    }

    fun cardBorder(): Int = fg()

    val call: Int = Color.parseColor("#00C853")   // strong green
    val sos: Int = Color.parseColor("#D50000")    // strong red
    val onColor: Int = Color.WHITE                // text on call/sos buttons
}
