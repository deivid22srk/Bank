package com.bancoapp.security

import android.util.Log
import com.bancoapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object NetworkSecurity {
    
    private const val TAG = "NetworkSecurity"
    
    external fun isDeviceSecure(): Boolean
    
    external fun validateConnection(url: String): Boolean
    
    external fun obfuscateEndpoint(endpoint: String): String
    
    fun createSecureClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request()
                val url = request.url.toString()
                
                if (!url.startsWith("https://")) {
                    Log.w(TAG, "Blocked non-HTTPS connection: $url")
                    throw SecurityException("Only HTTPS connections allowed")
                }
                
                val newRequest = request.newBuilder()
                    .addHeader("X-Security-Token", NativeCrypto.getSecurityToken())
                    .addHeader("X-App-Version", BuildConfig.VERSION_NAME)
                    .build()
                
                chain.proceed(newRequest)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    fun checkDeviceSecurity(): Boolean {
        return try {
            isDeviceSecure()
        } catch (e: Exception) {
            Log.e(TAG, "Device security check failed", e)
            true
        }
    }
}
