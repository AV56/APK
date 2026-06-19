package com.bigcall.appa

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/** Text-to-speech so names and prompts can be read aloud. */
object Speaker {
    private var tts: TextToSpeech? = null
    private var ready = false

    fun init(context: Context) {
        if (tts != null) return
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ready = true
                try { tts?.language = Locale.getDefault() } catch (e: Exception) {}
            }
        }
    }

    fun say(text: String) {
        if (ready && AppData.speakNames) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "appa")
        }
    }

    /** Force speak (used for confirmations regardless of the speakNames toggle). */
    fun announce(text: String) {
        if (ready) tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "appa")
    }
}
