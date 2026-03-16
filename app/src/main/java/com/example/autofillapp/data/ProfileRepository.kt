package com.example.autofillapp.data

import com.example.autofillapp.security.CryptoManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository that handles encryption/decryption of profile fields.
 * All data is encrypted before being written to Room, and decrypted when read.
 */
class ProfileRepository(
    private val dao: UserProfileDao,
    private val crypto: CryptoManager
) {

    /**
     * Observe the current profile (decrypted).
     */
    fun getProfile(): Flow<UserProfile?> {
        return dao.getProfile().map { profile ->
            profile?.let { decryptProfile(it) }
        }
    }

    /**
     * Get the current profile synchronously (decrypted).
     * Used by the AutofillService which runs on a binder thread.
     */
    suspend fun getProfileSync(): UserProfile? {
        return dao.getProfileSync()?.let { decryptProfile(it) }
    }

    /**
     * Save or update the profile. Fields are encrypted before storage.
     */
    suspend fun saveProfile(profile: UserProfile) {
        val encrypted = encryptProfile(profile)
        if (profile.id == 0L) {
            dao.insertProfile(encrypted)
        } else {
            dao.updateProfile(encrypted)
        }
    }

    /**
     * Delete all profile data.
     */
    suspend fun deleteProfile() {
        dao.deleteAll()
    }

    private fun encryptProfile(profile: UserProfile): UserProfile {
        return profile.copy(
            fullName = crypto.encrypt(profile.fullName),
            firstName = crypto.encrypt(profile.firstName),
            lastName = crypto.encrypt(profile.lastName),
            email = crypto.encrypt(profile.email),
            phone = crypto.encrypt(profile.phone),
            address = crypto.encrypt(profile.address),
            city = crypto.encrypt(profile.city),
            postalCode = crypto.encrypt(profile.postalCode),
            country = crypto.encrypt(profile.country)
        )
    }

    private fun decryptProfile(profile: UserProfile): UserProfile {
        return try {
            profile.copy(
                fullName = crypto.decrypt(profile.fullName),
                firstName = crypto.decrypt(profile.firstName),
                lastName = crypto.decrypt(profile.lastName),
                email = crypto.decrypt(profile.email),
                phone = crypto.decrypt(profile.phone),
                address = crypto.decrypt(profile.address),
                city = crypto.decrypt(profile.city),
                postalCode = crypto.decrypt(profile.postalCode),
                country = crypto.decrypt(profile.country)
            )
        } catch (e: Exception) {
            // If decryption fails (e.g. corrupted data), return empty profile
            UserProfile(id = profile.id)
        }
    }
}
