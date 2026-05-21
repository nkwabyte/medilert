package com.nkwabyte.medilert

import android.app.Application
import com.google.firebase.FirebaseApp

class MedilertApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
