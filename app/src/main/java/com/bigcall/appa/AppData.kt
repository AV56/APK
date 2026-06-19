package com.bigcall.appa

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Group(
    val id: String,
    var name: String,
    val contactIds: MutableList<String>
)

/** All app data: settings + custom groups. Stored in SharedPreferences as JSON. */
object AppData {
    private const val PREFS = "appa_call_prefs"
    const val ALL_ID = "ALL"

    // Settings
    var theme: Int = 0          // 0 = Yellow on Black, 1 = White on Black, 2 = Black on White
    var textScale: Float = 1.4f // multiplier for all text sizes
    var sosName: String = ""
    var sosNumber: String = ""
    var confirmCall: Boolean = true
    var speakNames: Boolean = true

    private val groups = ArrayList<Group>()
    private var loaded = false

    fun ensureLoaded(context: Context) {
        if (!loaded) { load(context); loaded = true }
    }

    fun getGroups(): List<Group> = groups

    fun addGroup(context: Context, name: String): Group {
        val g = Group(System.currentTimeMillis().toString(), name, ArrayList())
        groups.add(g); save(context); return g
    }

    fun removeGroup(context: Context, id: String) {
        groups.removeAll { it.id == id }; save(context)
    }

    fun findGroup(id: String): Group? = groups.firstOrNull { it.id == id }

    fun setGroupContacts(context: Context, id: String, ids: List<String>) {
        findGroup(id)?.let { it.contactIds.clear(); it.contactIds.addAll(ids); save(context) }
    }

    fun renameGroup(context: Context, id: String, name: String) {
        findGroup(id)?.let { it.name = name; save(context) }
    }

    private fun load(context: Context) {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        theme = p.getInt("theme", 0)
        textScale = p.getFloat("textScale", 1.4f)
        sosName = p.getString("sosName", "") ?: ""
        sosNumber = p.getString("sosNumber", "") ?: ""
        confirmCall = p.getBoolean("confirmCall", true)
        speakNames = p.getBoolean("speakNames", true)
        groups.clear()
        val raw = p.getString("groups", null) ?: return
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val ids = ArrayList<String>()
                val ca = o.getJSONArray("contacts")
                for (j in 0 until ca.length()) ids.add(ca.getString(j))
                groups.add(Group(o.getString("id"), o.getString("name"), ids))
            }
        } catch (e: Exception) { /* ignore corrupt data */ }
    }

    fun save(context: Context) {
        val arr = JSONArray()
        for (g in groups) {
            val o = JSONObject()
            o.put("id", g.id)
            o.put("name", g.name)
            val ca = JSONArray()
            for (c in g.contactIds) ca.put(c)
            o.put("contacts", ca)
            arr.put(o)
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putInt("theme", theme)
            .putFloat("textScale", textScale)
            .putString("sosName", sosName)
            .putString("sosNumber", sosNumber)
            .putBoolean("confirmCall", confirmCall)
            .putBoolean("speakNames", speakNames)
            .putString("groups", arr.toString())
            .apply()
    }
}
