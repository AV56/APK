package com.bigcall.appa

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat

object CallHelper {
    /** Places the call directly if permission is granted, otherwise opens the dialer pre-filled. */
    fun call(activity: Activity, number: String) {
        val hasPerm = ContextCompat.checkSelfPermission(
            activity, Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
        val action = if (hasPerm) Intent.ACTION_CALL else Intent.ACTION_DIAL
        val intent = Intent(action, Uri.parse("tel:" + Uri.encode(number)))
        try { activity.startActivity(intent) } catch (e: Exception) {}
    }
}
