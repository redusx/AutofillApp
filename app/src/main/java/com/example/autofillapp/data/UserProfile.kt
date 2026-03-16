package com.example.autofillapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a user profile.
 * All string fields are stored encrypted in the database.
 * Encryption/decryption is handled at the repository layer.
 */
@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val city: String = "",
    val postalCode: String = "",
    val country: String = ""
)
