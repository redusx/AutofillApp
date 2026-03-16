package com.example.autofillapp.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator

/**
 * Thin wrapper for Android Keystore master key management.
 * Provides utility methods to check key existence and create new keys.
 */
object KeyStoreManager {

    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val MASTER_KEY_ALIAS = "autofill_master_key"

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
    }

    /**
     * Check if the master key already exists in the Keystore.
     */
    fun hasMasterKey(): Boolean {
        return keyStore.containsAlias(MASTER_KEY_ALIAS)
    }

    /**
     * Ensure the master key exists. Creates it if it doesn't.
     */
    fun ensureMasterKey() {
        if (!hasMasterKey()) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                KEYSTORE_PROVIDER
            )
            val spec = KeyGenParameterSpec.Builder(
                MASTER_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()

            keyGenerator.init(spec)
            keyGenerator.generateKey()
        }
    }

    /**
     * Delete the master key from the Keystore (useful for account reset).
     */
    fun deleteMasterKey() {
        if (hasMasterKey()) {
            keyStore.deleteEntry(MASTER_KEY_ALIAS)
        }
    }
}
