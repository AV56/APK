package com.bigcall.appa

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val VOICE_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppData.ensureLoaded(this)
        Speaker.init(this)
        requestPermsIfNeeded()
        buildUi()
    }

    override fun onResume() {
        super.onResume()
        AppData.ensureLoaded(this)
        buildUi()
    }

    private fun requestPermsIfNeeded() {
        val need = ArrayList<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) need.add(Manifest.permission.READ_CONTACTS)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED) need.add(Manifest.permission.CALL_PHONE)
        if (need.isNotEmpty()) ActivityCompat.requestPermissions(this, need.toTypedArray(), 1)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        buildUi()
    }

    // ----- UI -----

    private fun buildUi() {
        val outer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Palette.bg())
        }

        // Top bar: title + settings
        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(Ui.dp(this@MainActivity,20), Ui.dp(this@MainActivity,18),
                Ui.dp(this@MainActivity,20), Ui.dp(this@MainActivity,8))
        }
        val title = TextView(this).apply {
            text = "Appa Call"
            setTextColor(Palette.fg())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.scaledSp(30f))
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val settings = Button(this).apply {
            text = "Settings"
            isAllCaps = false
            setTextColor(Palette.fg())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.scaledSp(18f))
            background = Ui.roundRect(Palette.card(), Ui.dp(this@MainActivity,14).toFloat(), Palette.fg(), Ui.dp(this@MainActivity,2))
            setPadding(Ui.dp(this@MainActivity,18), Ui.dp(this@MainActivity,14), Ui.dp(this@MainActivity,18), Ui.dp(this@MainActivity,14))
            setOnClickListener { startActivity(Intent(this@MainActivity, SettingsActivity::class.java)) }
        }
        topBar.addView(title)
        topBar.addView(settings)
        outer.addView(topBar)

        // Scrollable content
        val scroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
            isFillViewport = true
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(Ui.dp(this@MainActivity,16), Ui.dp(this@MainActivity,8),
                Ui.dp(this@MainActivity,16), Ui.dp(this@MainActivity,16))
        }

        // Big voice search
        content.addView(bigBar("Search by Voice", Palette.call) { startVoice() })
        content.addView(Ui.spacer(this, 18))

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {
            content.addView(infoText("Please allow access to Contacts so the names appear here.\nTap the button below."))
            content.addView(bigBar("Allow Contacts", Palette.call) { requestPermsIfNeeded() })
        } else {
            // Default: All Contacts
            content.addView(groupCircle("All Contacts", initials("All Contacts"), Palette.call, Palette.onColor) {
                openGroup(AppData.ALL_ID)
            })

            // Custom groups
            for (g in AppData.getGroups()) {
                content.addView(Ui.spacer(this, 10))
                content.addView(groupCircle(g.name, initials(g.name), Palette.fg(), Palette.bg()) {
                    openGroup(g.id)
                })
            }

            // New group
            content.addView(Ui.spacer(this, 16))
            content.addView(groupCircle("New Group", "+", Palette.card(), Palette.fg()) {
                Dialogs.textInput(this, "Name of new group", "") { name ->
                    val g = AppData.addGroup(this, name)
                    Speaker.say(name + " created")
                    openPicker(g.id)
                }
            })
        }

        scroll.addView(content)
        outer.addView(scroll)

        // SOS fixed bar
        if (AppData.sosNumber.isNotBlank()) {
            outer.addView(sosBar())
        } else {
            outer.addView(sosSetupBar())
        }

        setContentView(outer)
    }

    private fun openGroup(id: String) {
        val i = Intent(this, GroupActivity::class.java)
        i.putExtra("groupId", id)
        startActivity(i)
    }

    private fun openPicker(id: String) {
        val i = Intent(this, ContactPickerActivity::class.java)
        i.putExtra("groupId", id)
        startActivity(i)
    }

    // ----- Reusable views -----

    private fun bigBar(label: String, color: Int, onClick: () -> Unit): View {
        return Button(this).apply {
            text = label
            isAllCaps = false
            setTextColor(Palette.onColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.scaledSp(26f))
            background = Ui.roundRect(color, Ui.dp(this@MainActivity,18).toFloat())
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this@MainActivity,96)
            )
            setOnClickListener { onClick() }
        }
    }

    private fun groupCircle(name: String, badge: String, fill: Int, textColor: Int, onClick: () -> Unit): View {
        val wrap = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(0, Ui.dp(this@MainActivity,10), 0, Ui.dp(this@MainActivity,10))
            isClickable = true
            setOnClickListener { Speaker.say(name); onClick() }
        }
        val size = Ui.dp(this, 170)
        val circle = TextView(this).apply {
            text = badge
            gravity = Gravity.CENTER
            setTextColor(textColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.scaledSp(54f))
            background = Ui.circle(fill, Palette.fg(), Ui.dp(this@MainActivity,3))
            layoutParams = LinearLayout.LayoutParams(size, size)
        }
        val label = TextView(this).apply {
            text = name
            gravity = Gravity.CENTER
            setTextColor(Palette.fg())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.scaledSp(26f))
            setPadding(0, Ui.dp(this@MainActivity,10), 0, 0)
        }
        wrap.addView(circle)
        wrap.addView(label)
        return wrap
    }

    private fun infoText(msg: String): View {
        return TextView(this).apply {
            text = msg
            setTextColor(Palette.fg())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.scaledSp(20f))
            setPadding(0, Ui.dp(this@MainActivity,12), 0, Ui.dp(this@MainActivity,16))
        }
    }

    private fun sosBar(): View {
        return Button(this).apply {
            text = "SOS  -  Call " + AppData.sosName
            isAllCaps = false
            setTextColor(Palette.onColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.scaledSp(26f))
            background = Ui.roundRect(Palette.sos, 0f)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this@MainActivity,110)
            )
            setOnClickListener {
                Dialogs.confirmCall(this@MainActivity, AppData.sosName.ifBlank { "Emergency" }, AppData.sosNumber)
            }
        }
    }

    private fun sosSetupBar(): View {
        return Button(this).apply {
            text = "Set up SOS button"
            isAllCaps = false
            setTextColor(Palette.fg())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.scaledSp(20f))
            background = Ui.roundRect(Palette.card(), 0f, Palette.sos, Ui.dp(this@MainActivity,3))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this@MainActivity,90)
            )
            setOnClickListener { startActivity(Intent(this@MainActivity, SettingsActivity::class.java)) }
        }
    }

    private fun initials(name: String): String {
        val parts = name.trim().split(" ").filter { it.isNotEmpty() }
        return when {
            parts.isEmpty() -> "?"
            parts.size == 1 -> parts[0].take(1).uppercase()
            else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
        }
    }

    // ----- Voice search -----

    private fun startVoice() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say a name")
        }
        try {
            Speaker.announce("Say a name")
            startActivityForResult(intent, VOICE_CODE)
        } catch (e: Exception) {
            Toast.makeText(this, "Voice search is not available on this phone", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VOICE_CODE && resultCode == Activity.RESULT_OK) {
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spoken = results?.firstOrNull()?.trim() ?: return
            val match = findContact(spoken)
            if (match != null) {
                Dialogs.confirmCall(this, match.name, match.number)
            } else {
                Speaker.announce("No contact found for $spoken")
                Toast.makeText(this, "No contact found for \"$spoken\"", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun findContact(query: String): PhoneContact? {
        val contacts = ContactsRepo.loadAll(this)
        if (contacts.isEmpty()) return null
        val q = query.lowercase().trim()
        contacts.firstOrNull { it.name.lowercase() == q }?.let { return it }
        contacts.firstOrNull { it.name.lowercase().startsWith(q) }?.let { return it }
        contacts.firstOrNull { it.name.lowercase().contains(q) }?.let { return it }
        // token overlap
        val qTokens = q.split(" ").filter { it.isNotEmpty() }.toSet()
        return contacts
            .map { it to it.name.lowercase().split(" ").toSet().intersect(qTokens).size }
            .filter { it.second > 0 }
            .maxByOrNull { it.second }?.first
    }
}
