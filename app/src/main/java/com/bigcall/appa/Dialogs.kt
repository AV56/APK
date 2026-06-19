package com.bigcall.appa

import android.app.Activity
import android.app.AlertDialog
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.util.TypedValue

object Dialogs {

    /** Big, high-contrast "Call NAME?" confirmation with large CALL / CANCEL buttons. */
    fun confirmCall(activity: Activity, name: String, number: String) {
        if (!AppData.confirmCall) {
            Speaker.announce("Calling $name")
            CallHelper.call(activity, number)
            return
        }
        val pad = Ui.dp(activity, 24)
        val root = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Palette.bg())
            setPadding(pad, pad, pad, pad)
        }

        val title = TextView(activity).apply {
            text = "Call\n$name?"
            setTextColor(Palette.fg())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.scaledSp(34f))
            gravity = Gravity.CENTER
            setPadding(0, Ui.dp(activity, 12), 0, Ui.dp(activity, 28))
        }
        root.addView(title)

        val dialog = AlertDialog.Builder(activity).setView(root).create()

        val callBtn = Button(activity).apply {
            text = "CALL"
            isAllCaps = false
            setTextColor(Palette.onColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.scaledSp(30f))
            background = Ui.roundRect(Palette.call, Ui.dp(activity, 18).toFloat())
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(activity, 96)
            )
            setOnClickListener {
                dialog.dismiss()
                Speaker.announce("Calling $name")
                CallHelper.call(activity, number)
            }
        }
        root.addView(callBtn)
        root.addView(Ui.spacer(activity, 18))

        val cancelBtn = Button(activity).apply {
            text = "Cancel"
            isAllCaps = false
            setTextColor(Palette.fg())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.scaledSp(26f))
            background = Ui.roundRect(Palette.card(), Ui.dp(activity, 18).toFloat(), Palette.fg(), Ui.dp(activity, 2))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(activity, 80)
            )
            setOnClickListener { dialog.dismiss() }
        }
        root.addView(cancelBtn)

        dialog.show()
        Speaker.announce("Call $name?")
    }

    /** Simple large text input dialog (used for naming a new group). */
    fun textInput(activity: Activity, title: String, initial: String, onOk: (String) -> Unit) {
        val pad = Ui.dp(activity, 24)
        val root = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Palette.bg())
            setPadding(pad, pad, pad, pad)
        }
        val tv = TextView(activity).apply {
            text = title
            setTextColor(Palette.fg())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.scaledSp(24f))
            setPadding(0, 0, 0, Ui.dp(activity, 12))
        }
        val edit = EditText(activity).apply {
            setText(initial)
            setTextColor(Palette.fg())
            setHintTextColor(Palette.card())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.scaledSp(26f))
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
            setBackgroundColor(Palette.card())
            setPadding(Ui.dp(activity,16), Ui.dp(activity,16), Ui.dp(activity,16), Ui.dp(activity,16))
        }
        root.addView(tv)
        root.addView(edit)

        AlertDialog.Builder(activity)
            .setView(root)
            .setPositiveButton("Save") { _, _ ->
                val t = edit.text.toString().trim()
                if (t.isNotEmpty()) onOk(t)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
