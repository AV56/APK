package com.bigcall.appa

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/** Pick which phone contacts belong to a group. Selected ones are highlighted. */
class ContactPickerActivity : AppCompatActivity() {

    private var groupId: String = ""
    private val selected = HashSet<String>()
    private var allContacts: List<PhoneContact> = emptyList()
    private lateinit var listContainer: LinearLayout
    private var filter: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppData.ensureLoaded(this)
        Speaker.init(this)
        groupId = intent.getStringExtra("groupId") ?: ""
        AppData.findGroup(groupId)?.let { selected.addAll(it.contactIds) }
        allContacts = ContactsRepo.loadAll(this)
        buildUi()
    }

    private fun buildUi() {
        val outer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Palette.bg())
        }

        val title = TextView(this).apply {
            text = "Tap people to add"
            setTextColor(Palette.fg())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.scaledSp(24f))
            gravity = Gravity.CENTER
            setPadding(0, Ui.dp(this@ContactPickerActivity,16), 0, Ui.dp(this@ContactPickerActivity,10))
        }
        outer.addView(title)

        val search = EditText(this).apply {
            hint = "Type to search"
            setTextColor(Palette.fg())
            setHintTextColor(Palette.cardBorder())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.scaledSp(22f))
            setBackgroundColor(Palette.card())
            setPadding(Ui.dp(this@ContactPickerActivity,18), Ui.dp(this@ContactPickerActivity,18),
                Ui.dp(this@ContactPickerActivity,18), Ui.dp(this@ContactPickerActivity,18))
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.setMargins(Ui.dp(this@ContactPickerActivity,16), 0, Ui.dp(this@ContactPickerActivity,16), Ui.dp(this@ContactPickerActivity,10))
            layoutParams = lp
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) { filter = s?.toString()?.lowercase() ?: ""; refreshList() }
                override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            })
        }
        outer.addView(search)

        val scroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
            isFillViewport = true
        }
        listContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(Ui.dp(this@ContactPickerActivity,12), 0, Ui.dp(this@ContactPickerActivity,12), Ui.dp(this@ContactPickerActivity,12))
        }
        scroll.addView(listContainer)
        outer.addView(scroll)

        // Save bar
        outer.addView(Button(this).apply {
            text = "Save"
            isAllCaps = false
            setTextColor(Palette.onColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.scaledSp(26f))
            background = Ui.roundRect(Palette.call, 0f)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this@ContactPickerActivity,100))
            setOnClickListener {
                AppData.setGroupContacts(this@ContactPickerActivity, groupId, selected.toList())
                Toast.makeText(this@ContactPickerActivity, "Saved", Toast.LENGTH_SHORT).show()
                finish()
            }
        })

        setContentView(outer)
        refreshList()
    }

    private fun refreshList() {
        listContainer.removeAllViews()
        val shown = if (filter.isBlank()) allContacts
                    else allContacts.filter { it.name.lowercase().contains(filter) }
        for (c in shown) {
            listContainer.addView(pickRow(c))
            listContainer.addView(Ui.spacer(this, 10))
        }
    }

    private fun pickRow(c: PhoneContact): View {
        val isSel = selected.contains(c.id)
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = Ui.roundRect(
                if (isSel) Palette.call else Palette.card(),
                Ui.dp(this@ContactPickerActivity,16).toFloat(),
                Palette.fg(), Ui.dp(this@ContactPickerActivity,2)
            )
            setPadding(Ui.dp(this@ContactPickerActivity,14), Ui.dp(this@ContactPickerActivity,16),
                Ui.dp(this@ContactPickerActivity,14), Ui.dp(this@ContactPickerActivity,16))
            isClickable = true
            setOnClickListener {
                if (isSel) selected.remove(c.id) else selected.add(c.id)
                Speaker.say(c.name)
                refreshList()
            }
        }

        val photoSize = Ui.dp(this, 70)
        val bmp = ContactsRepo.loadBitmap(this, c.photoUri)
        if (bmp != null) {
            row.addView(ImageView(this).apply {
                setImageBitmap(Ui.circleBitmap(bmp))
                layoutParams = LinearLayout.LayoutParams(photoSize, photoSize)
            })
        } else {
            row.addView(TextView(this).apply {
                text = c.name.trim().take(1).uppercase()
                gravity = Gravity.CENTER
                setTextColor(Palette.bg())
                setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.scaledSp(26f))
                background = Ui.circle(Palette.fg())
                layoutParams = LinearLayout.LayoutParams(photoSize, photoSize)
            })
        }

        row.addView(TextView(this).apply {
            text = c.name
            setTextColor(if (isSel) Palette.onColor else Palette.fg())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.scaledSp(24f))
            setPadding(Ui.dp(this@ContactPickerActivity,14), 0, Ui.dp(this@ContactPickerActivity,8), 0)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        })

        row.addView(TextView(this).apply {
            text = if (isSel) "Added" else "Add"
            setTextColor(if (isSel) Palette.onColor else Palette.fg())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.scaledSp(20f))
        })

        return row
    }
}
