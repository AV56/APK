package com.bigcall.appa

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract

data class PhoneContact(
    val id: String,
    val name: String,
    val number: String,
    val photoUri: String?
)

/** Reads the phone's contacts fresh every time, so the list always matches the phone. */
object ContactsRepo {

    fun loadAll(context: Context): List<PhoneContact> {
        val list = ArrayList<PhoneContact>()
        val seen = HashSet<String>()
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        )
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE NOCASE ASC"
        )
        cursor?.use { c ->
            val idIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val photoIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
            while (c.moveToNext()) {
                val id = (if (idIdx >= 0) c.getString(idIdx) else null) ?: continue
                val name = (if (nameIdx >= 0) c.getString(nameIdx) else null)?.trim()
                val number = (if (numIdx >= 0) c.getString(numIdx) else null)?.trim()
                if (name.isNullOrEmpty() || number.isNullOrEmpty()) continue
                if (!seen.add(id)) continue   // one entry per contact
                val photo = if (photoIdx >= 0) c.getString(photoIdx) else null
                list.add(PhoneContact(id, name, number, photo))
            }
        }
        return list
    }

    fun byIds(context: Context, ids: List<String>): List<PhoneContact> {
        val all = loadAll(context).associateBy { it.id }
        return ids.mapNotNull { all[it] }
    }

    fun loadBitmap(context: Context, photoUri: String?): Bitmap? {
        if (photoUri.isNullOrBlank()) return null
        return try {
            context.contentResolver.openInputStream(Uri.parse(photoUri)).use {
                BitmapFactory.decodeStream(it)
            }
        } catch (e: Exception) { null }
    }
}
