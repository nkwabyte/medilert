package com.nkwabyte.medilert

import android.app.Application
import com.google.firebase.FirebaseApp

class MedilertApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = this
        FirebaseApp.initializeApp(this)
    }

    companion object {
        lateinit var appContext: android.content.Context
    }
}
