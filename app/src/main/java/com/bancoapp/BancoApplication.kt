package com.bancoapp

import android.app.Application
import com.bancoapp.security.NativeCrypto
import com.google.firebase.FirebaseApp

class BancoApplication : Application() {
    
    companion object {
        init {
            System.loadLibrary("bancoapp")
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        NativeCrypto.initialize()
    }
}
