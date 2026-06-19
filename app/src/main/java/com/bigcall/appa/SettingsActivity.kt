package com.bigcall.appa

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private val PICK_SOS = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppData.ensureLoaded(this)
        Speaker.init(this)
        buildUi()
    }

    private fun buildUi() {
        val scroll = ScrollView(this).apply { setBackgroundColor(Palette.bg()); isFillViewport = true }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(Ui.dp(this@SettingsActivity,18), Ui.dp(this@SettingsActivity,18),
                Ui.dp(this@SettingsActivity,18), Ui.dp(this@SettingsActivity,28))
        }

        content.addView(heading("Settings"))

        // Colours
        content.addView(label("Colours"))
        content.addView(actionBtn("Yellow on Black") { AppData.theme = 0; saveAndRefresh() })
        content.addView(Ui.spacer(this,8))
        content.addView(actionBtn("White on Black") { AppData.theme = 1; saveAndRefresh() })
        content.addView(Ui.spacer(this,8))
        content.addView(actionBtn("Black on White") { AppData.theme = 2; saveAndRefresh() })

        // Text size
        content.addView(label("Text Size"))
        val sizeRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        sizeRow.addView(actionBtnW("Smaller", 1f) {
            AppData.textScale = (AppData.textScale - 0.2f).coerceAtLeast(1.0f); saveAndRefresh()
        })
        sizeRow.addView(Ui.spacer(this,8).apply { layoutParams = LinearLayout.LayoutParams(Ui.dp(this@SettingsActivity,12), 1) })
        sizeRow.addView(actionBtnW("Bigger", 1f) {
            AppData.textScale = (AppData.textScale + 0.2f).coerceAtMost(2.4f); saveAndRefresh()
        })
        content.addView(sizeRow)
        content.addView(TextView(this).apply {
            text = "Sample text at this size"
            setTextColor(Palette.fg())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.scaledSp(24f))
            setPadding(0, Ui.dp(this@SettingsActivity,10), 0, 0)
        })

        // SOS contact
        content.addView(label("SOS Emergency Button"))
        content.addView(TextView(this).apply {
            text = if (AppData.sosNumber.isBlank()) "Not set yet" else "Calls: " + AppData.sosName
            setTextColor(Palette.fg())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.scaledSp(20f))
            setPadding(0,0,0,Ui.dp(this@SettingsActivity,8))
        })
        content.addView(actionBtn("Choose SOS Contact") { pickSos() })

        // Toggles
        content.addView(label("Options"))
        content.addView(actionBtn(toggleText("Confirm before calling", AppData.confirmCall)) {
            AppData.confirmCall = !AppData.confirmCall; saveAndRefresh()
        })
        content.addView(Ui.spacer(this,8))
        content.addView(actionBtn(toggleText("Read names aloud", AppData.speakNames)) {
            AppData.speakNames = !AppData.speakNames; saveAndRefresh()
        })

        // Manage groups
        if (AppData.getGroups().isNotEmpty()) {
            content.addView(label("Your Groups"))
            for (g in AppData.getGroups()) {
                val row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(0,Ui.dp(this@SettingsActivity,6),0,Ui.dp(this@SettingsActivity,6))
                }
                row.addView(TextView(this).apply {
                    text = g.name
                    setTextColor(Palette.fg())
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.scaledSp(22f))
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                })
                row.addView(Button(this).apply {
                    text = "Rename"
                    isAllCaps = false
                    setTextColor(Palette.fg())
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.scaledSp(18f))
                    background = Ui.roundRect(Palette.card(), Ui.dp(this@SettingsActivity,12).toFloat(), Palette.fg(), Ui.dp(this@SettingsActivity,2))
                    setOnClickListener {
                        Dialogs.textInput(this@SettingsActivity, "Rename group", g.name) {
                            AppData.renameGroup(this@SettingsActivity, g.id, it); saveAndRefresh()
                        }
                    }
                })
                row.addView(Ui.spacer(this,8).apply { layoutParams = LinearLayout.LayoutParams(Ui.dp(this@SettingsActivity,10),1) })
                row.addView(Button(this).apply {
                    text = "Delete"
                    isAllCaps = false
                    setTextColor(Palette.onColor)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.scaledSp(18f))
                    background = Ui.roundRect(Palette.sos, Ui.dp(this@SettingsActivity,12).toFloat())
                    setOnClickListener {
                        android.app.AlertDialog.Builder(this@SettingsActivity)
                            .setMessage("Delete group \"" + g.name + "\"?")
                            .setPositiveButton("Delete") { _, _ -> AppData.removeGroup(this@SettingsActivity, g.id); saveAndRefresh() }
                            .setNegativeButton("Cancel", null).show()
                    }
                })
                content.addView(row)
            }
        }

        // Done
        content.addView(Ui.spacer(this,16))
        content.addView(actionBtn("Done") { finish() })

        scroll.removeAllViews()
        scroll.addView(content)
        setContentView(scroll)
    }

    private fun toggleText(name: String, on: Boolean) = name + ":  " + (if (on) "ON" else "OFF")

    private fun saveAndRefresh() { AppData.save(this); buildUi() }

    private fun heading(t: String) = TextView(this).apply {
        text = t
        setTextColor(Palette.fg())
        setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.scaledSp(30f))
        setPadding(0,0,0,Ui.dp(this@SettingsActivity,10))
    }

    private fun label(t: String) = TextView(this).apply {
        text = t
        setTextColor(Palette.fg())
        setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.scaledSp(22f))
        setPadding(0,Ui.dp(this@SettingsActivity,22),0,Ui.dp(this@SettingsActivity,10))
    }

    private fun actionBtn(t: String, onClick: () -> Unit): View = Button(this).apply {
        text = t
        isAllCaps = false
        setTextColor(Palette.fg())
        setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.scaledSp(22f))
        background = Ui.roundRect(Palette.card(), Ui.dp(this@SettingsActivity,16).toFloat(), Palette.fg(), Ui.dp(this@SettingsActivity,2))
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this@SettingsActivity,84))
        setOnClickListener { onClick() }
    }

    private fun actionBtnW(t: String, weight: Float, onClick: () -> Unit): View = Button(this).apply {
        text = t
        isAllCaps = false
        setTextColor(Palette.fg())
        setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.scaledSp(22f))
        background = Ui.roundRect(Palette.card(), Ui.dp(this@SettingsActivity,16).toFloat(), Palette.fg(), Ui.dp(this@SettingsActivity,2))
        layoutParams = LinearLayout.LayoutParams(0, Ui.dp(this@SettingsActivity,84), weight)
        setOnClickListener { onClick() }
    }

    private fun pickSos() {
        try {
            val i = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
            startActivityForResult(i, PICK_SOS)
        } catch (e: Exception) {
            Toast.makeText(this, "Could not open contacts", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_SOS && resultCode == Activity.RESULT_OK) {
            val uri = data?.data ?: return
            var cursor: Cursor? = null
            try {
                cursor = contentResolver.query(uri, null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val nIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val name = if (nIdx >= 0) cursor.getString(nIdx) else ""
                    val number = if (numIdx >= 0) cursor.getString(numIdx) else ""
                    if (!number.isNullOrBlank()) {
                        AppData.sosName = name ?: "Emergency"
                        AppData.sosNumber = number
                        AppData.save(this)
                        Toast.makeText(this, "SOS set to " + AppData.sosName, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
            } finally { cursor?.close() }
            buildUi()
        }
    }
}
