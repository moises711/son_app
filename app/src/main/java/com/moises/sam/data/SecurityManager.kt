package com.moises.sam.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Clase encargada de manejar la seguridad de la aplicación.
 * Utiliza EncryptedSharedPreferences para almacenar datos sensibles de forma segura.
 */
class SecurityManager private constructor(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedSharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        ENCRYPTED_PREFS_FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * Guarda un valor String encriptado
     */
    fun saveSecureString(key: String, value: String) {
        encryptedSharedPreferences.edit().putString(key, value).apply()
    }

    /**
     * Recupera un valor String encriptado
     */
    fun getSecureString(key: String, defaultValue: String = ""): String {
        return encryptedSharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    /**
     * Guarda un valor booleano encriptado
     */
    fun saveSecureBoolean(key: String, value: Boolean) {
        encryptedSharedPreferences.edit().putBoolean(key, value).apply()
    }

    /**
     * Recupera un valor booleano encriptado
     */
    fun getSecureBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return encryptedSharedPreferences.getBoolean(key, defaultValue)
    }

    /**
     * Elimina un valor encriptado
     */
    fun removeSecureValue(key: String) {
        encryptedSharedPreferences.edit().remove(key).apply()
    }

    /**
     * Limpia todos los datos encriptados
     */
    fun clearAllSecureData() {
        encryptedSharedPreferences.edit().clear().apply()
    }

    companion object {
        private const val ENCRYPTED_PREFS_FILE_NAME = "secured_app_prefs"
        
        @Volatile
        private var instance: SecurityManager? = null
        
        fun getInstance(context: Context): SecurityManager {
            return instance ?: synchronized(this) {
                instance ?: SecurityManager(context.applicationContext).also { instance = it }
            }
        }
    }
}