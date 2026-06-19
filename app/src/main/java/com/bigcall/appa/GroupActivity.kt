package com.bigcall.appa

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class GroupActivity : AppCompatActivity() {

    private var groupId: String = AppData.ALL_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppData.ensureLoaded(this)
        Speaker.init(this)
        groupId = intent.getStringExtra("groupId") ?: AppData.ALL_ID
    }

    override fun onResume() {
        super.onResume()
        buildUi()
    }

    private fun isAll() = groupId == AppData.ALL_ID

    private fun groupName(): String =
        if (isAll()) "All Contacts" else (AppData.findGroup(groupId)?.name ?: "Group")

    private fun contacts(): List<PhoneContact> {
        return if (isAll()) ContactsRepo.loadAll(this)
        else ContactsRepo.byIds(this, AppData.findGroup(groupId)?.contactIds ?: emptyList())
    }

    private fun buildUi() {
        val outer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Palette.bg())
        }

        // Header with Back + title
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(Ui.dp(this@GroupActivity,16), Ui.dp(this@GroupActivity,16),
                Ui.dp(this@GroupActivity,16), Ui.dp(this@GroupActivity,8))
        }
        val back = Button(this).apply {
            text = "Back"
            isAllCaps = false
            setTextColor(Palette.fg())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.scaledSp(20f))
            background = Ui.roundRect(Palette.card(), Ui.dp(this@GroupActivity,14).toFloat(), Palette.fg(), Ui.dp(this@GroupActivity,2))
            setPadding(Ui.dp(this@GroupActivity,22), Ui.dp(this@GroupActivity,16), Ui.dp(this@GroupActivity,22), Ui.dp(this@GroupActivity,16))
            setOnClickListener { finish() }
        }
        val title = TextView(this).apply {
            text = groupName()
            setTextColor(Palette.fg())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.scaledSp(26f))
            gravity = Gravity.CENTER
            setPadding(Ui.dp(this@GroupActivity,12),0,0,0)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        header.addView(back)
        header.addView(title)
        outer.addView(header)

        val scroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
            isFillViewport = true
        }
        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(Ui.dp(this@GroupActivity,12), Ui.dp(this@GroupActivity,8),
                Ui.dp(this@GroupActivity,12), Ui.dp(this@GroupActivity,16))
        }

        val people = contacts()
        if (people.isEmpty()) {
            list.addView(TextView(this).apply {
                text = if (isAll()) "No contacts found on this phone."
                       else "This group is empty.\nTap the button below to add people."
                setTextColor(Palette.fg())
                setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.scaledSp(22f))
                gravity = Gravity.CENTER
                setPadding(0, Ui.dp(this@GroupActivity,30), 0, Ui.dp(this@GroupActivity,30))
            })
        } else {
            for (c in people) {
                list.addView(contactRow(c))
                list.addView(Ui.spacer(this, 12))
            }
        }

        scroll.addView(list)
        outer.addView(scroll)

        // For custom groups: an Edit/Add button fixed at the bottom
        if (!isAll()) {
            outer.addView(editBar())
        }

        setContentView(outer)
    }

    private fun contactRow(c: PhoneContact): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = Ui.roundRect(Palette.card(), Ui.dp(this@GroupActivity,18).toFloat(), Palette.fg(), Ui.dp(this@GroupActivity,2))
            setPadding(Ui.dp(this@GroupActivity,14), Ui.dp(this@GroupActivity,14), Ui.dp(this@GroupActivity,14), Ui.dp(this@GroupActivity,14))
            isClickable = true
            setOnClickListener { Speaker.say(c.name); Dialogs.confirmCall(this@GroupActivity, c.name, c.number) }
            setOnLongClickListener { Speaker.announce(c.name); true }
        }

        // Photo (real contact photo, or initial)
        val photoSize = Ui.dp(this, 90)
        val bmp = ContactsRepo.loadBitmap(this, c.photoUri)
        if (bmp != null) {
            val img = ImageView(this).apply {
                setImageBitmap(Ui.circleBitmap(bmp))
                layoutParams = LinearLayout.LayoutParams(photoSize, photoSize)
            }
            row.addView(img)
        } else {
            val initial = TextView(this).apply {
                text = c.name.trim().take(1).uppercase()
                gravity = Gravity.CENTER
                setTextColor(Palette.bg())
                setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.scaledSp(34f))
                background = Ui.circle(Palette.fg())
                layoutParams = LinearLayout.LayoutParams(photoSize, photoSize)
            }
            row.addView(initial)
        }

        val name = TextView(this).apply {
            text = c.name
            setTextColor(Palette.fg())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.scaledSp(26f))
            setPadding(Ui.dp(this@GroupActivity,14), 0, Ui.dp(this@GroupActivity,8), 0)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        row.addView(name)

        val callBtn = Button(this).apply {
            text = "Call"
            isAllCaps = false
            setTextColor(Palette.onColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.scaledSp(22f))
            background = Ui.roundRect(Palette.call, Ui.dp(this@GroupActivity,14).toFloat())
            layoutParams = LinearLayout.LayoutParams(Ui.dp(this@GroupActivity,120), Ui.dp(this@GroupActivity,84))
            setOnClickListener { Dialogs.confirmCall(this@GroupActivity, c.name, c.number) }
        }
        row.addView(callBtn)

        return row
    }

    private fun editBar(): View {
        return Button(this).apply {
            text = "Add / Remove People"
            isAllCaps = false
            setTextColor(Palette.onColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.scaledSp(22f))
            background = Ui.roundRect(Palette.call, 0f)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this@GroupActivity,100)
            )
            setOnClickListener {
                val i = Intent(this@GroupActivity, ContactPickerActivity::class.java)
                i.putExtra("groupId", groupId)
                startActivity(i)
            }
        }
    }
}
