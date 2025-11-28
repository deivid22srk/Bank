package com.bancoapp

import android.app.Application
import com.bancoapp.security.NativeCrypto
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

class BancoApplication : Application() {
    
    companion object {
        init {
            System.loadLibrary("bancoapp")
        }
        
        val supabase by lazy {
            createSupabaseClient(
                supabaseUrl = BuildConfig.SUPABASE_URL,
                supabaseKey = BuildConfig.SUPABASE_KEY
            ) {
                install(Postgrest)
                install(Realtime)
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        NativeCrypto.initialize()
    }
}
