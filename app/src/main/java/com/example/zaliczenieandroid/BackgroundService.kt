package com.example.zaliczenieandroid

import android.app.IntentService
import android.content.Intent

class BackgroundService : IntentService("BackgroundService") {

    override fun onHandleIntent(intent: Intent?) {
        // Pobierz wiadomość z Intentu
        val message = intent?.getStringExtra("MESSAGE") ?: "Halo halo! Aplikacja cię wzywa"

        // Wysyłanie broadcastu z wiadomością
        val broadcastIntent = Intent("com.example.app.SHOW_ALERT")
        broadcastIntent.putExtra("MESSAGE", message)
        sendBroadcast(broadcastIntent)
    }
}