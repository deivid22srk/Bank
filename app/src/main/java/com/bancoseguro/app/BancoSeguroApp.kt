package com.bancoseguro.app

import android.app.Application

class BancoSeguroApp : Application() {
    companion object {
        lateinit var instance: BancoSeguroApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        System.loadLibrary("bancoseguro_native")
    }
}
