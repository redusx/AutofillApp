package com.example.autofillapp.autofill

import com.example.autofillapp.data.UserProfile

/** Maps detected field types to the corresponding UserProfile property values. */
object AutofillMapper {

    /**
     * Return the value from the profile that corresponds to the given field type. Returns null if
     * the field value is blank.
     */
    fun getValueForField(fieldType: FieldType, profile: UserProfile): String? {
        val value =
                when (fieldType) {
                    FieldType.EMAIL -> profile.email
                    FieldType.PHONE -> profile.phone
                    FieldType.FULL_NAME -> profile.fullName
                    FieldType.FIRST_NAME -> profile.firstName
                    FieldType.LAST_NAME -> profile.lastName
                    FieldType.ADDRESS -> profile.address
                    FieldType.CITY -> profile.city
                    FieldType.POSTAL_CODE -> profile.postalCode
                    FieldType.COUNTRY -> profile.country
                    FieldType.UNKNOWN -> null
                }
        return value?.takeIf { it.isNotBlank() }
    }

    /** Return a user-facing label for the field type (shown in the autofill suggestion popup). */
    fun getDisplayLabel(fieldType: FieldType): String {
        return when (fieldType) {
            FieldType.EMAIL -> "Email"
            FieldType.PHONE -> "Phone"
            FieldType.FULL_NAME -> "Full Name"
            FieldType.FIRST_NAME -> "First Name"
            FieldType.LAST_NAME -> "Last Name"
            FieldType.ADDRESS -> "Address"
            FieldType.CITY -> "City"
            FieldType.POSTAL_CODE -> "Postal Code"
            FieldType.COUNTRY -> "Country"
            FieldType.UNKNOWN -> ""
        }
    }
}
