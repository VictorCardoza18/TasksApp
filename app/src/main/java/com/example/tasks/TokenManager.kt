package com.example.tasks

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.core.content.edit

class TokenManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "auth_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun clearToken() {
        sharedPreferences.edit().remove("token").remove("username").apply()
    }

    fun saveUsername(username: String) {
        sharedPreferences.edit() { putString("username", username) }
    }

    fun getUsername(): String? {
        return sharedPreferences.getString("username", null)
    }

    fun getToken(): String? {
        return sharedPreferences.getString("jwt_token", null)
    }

    fun saveToken(token: String) {
        sharedPreferences.edit() {
            putString("jwt_token", token)
        }
    }

    fun deleteToken() {
        sharedPreferences.edit() {
            remove("jwt_token")
        }
    }

    companion object {
        private var instance: TokenManager? = null

        fun getInstance(context: Context): TokenManager {
            return instance ?: synchronized(this) {
                instance ?: TokenManager(context.applicationContext).also { instance = it }
            }
        }
    }
}