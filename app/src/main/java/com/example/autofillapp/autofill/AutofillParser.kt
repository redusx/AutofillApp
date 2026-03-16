package com.example.autofillapp.autofill

import android.app.assist.AssistStructure
import android.view.autofill.AutofillId

/** Represents a parsed form field with its autofill ID and detected field type. */
data class ParsedField(val autofillId: AutofillId, val fieldType: FieldType)

/** Supported field types that can be autofilled. */
enum class FieldType {
    EMAIL,
    PHONE,
    FULL_NAME,
    FIRST_NAME,
    LAST_NAME,
    ADDRESS,
    CITY,
    POSTAL_CODE,
    COUNTRY,
    UNKNOWN
}

/**
 * Parses an AssistStructure to find autofillable fields and determine their types. Inspects
 * autofillHints, hint text, idEntry, and inputType to classify fields.
 */
object AutofillParser {

    /** Walk the entire AssistStructure and collect all recognized fields. */
    fun parseStructure(structure: AssistStructure): List<ParsedField> {
        val fields = mutableListOf<ParsedField>()
        for (i in 0 until structure.windowNodeCount) {
            val windowNode = structure.getWindowNodeAt(i)
            parseNode(windowNode.rootViewNode, fields)
        }
        return fields.filter { it.fieldType != FieldType.UNKNOWN }
    }

    private fun parseNode(node: AssistStructure.ViewNode, fields: MutableList<ParsedField>) {
        // Only consider nodes that have an autofill ID and are editable
        val autofillId = node.autofillId
        if (autofillId != null && node.className?.contains("EditText") == true ||
                        autofillId != null && node.htmlInfo != null ||
                        autofillId != null &&
                                node.autofillType == android.view.View.AUTOFILL_TYPE_TEXT
        ) {
            val fieldType = detectFieldType(node)
            fields.add(ParsedField(autofillId, fieldType))
        }

        // Recurse into child nodes
        for (i in 0 until node.childCount) {
            parseNode(node.getChildAt(i), fields)
        }
    }

    private fun detectFieldType(node: AssistStructure.ViewNode): FieldType {
        // 1. Check autofillHints (most reliable)
        node.autofillHints?.forEach { hint ->
            val type = matchHint(hint)
            if (type != FieldType.UNKNOWN) return type
        }

        // 2. Check hint text
        node.hint?.let { hint ->
            val type = matchHint(hint)
            if (type != FieldType.UNKNOWN) return type
        }

        // 3. Check idEntry (resource ID name)
        node.idEntry?.let { idEntry ->
            val type = matchHint(idEntry)
            if (type != FieldType.UNKNOWN) return type
        }

        // 4. Check inputType
        val inputType = node.inputType
        if (inputType and android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS != 0) {
            return FieldType.EMAIL
        }
        if (inputType and android.text.InputType.TYPE_CLASS_PHONE != 0) {
            return FieldType.PHONE
        }
        if (inputType and android.text.InputType.TYPE_TEXT_VARIATION_PERSON_NAME != 0) {
            return FieldType.FULL_NAME
        }
        if (inputType and android.text.InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS != 0) {
            return FieldType.ADDRESS
        }

        return FieldType.UNKNOWN
    }

    private fun matchHint(hint: String): FieldType {
        val lower = hint.lowercase()
        return when {
            // Email
            lower.contains("email") ||
                    lower.contains("e-mail") ||
                    lower == android.view.View.AUTOFILL_HINT_EMAIL_ADDRESS -> FieldType.EMAIL

            // Phone
            lower.contains("phone") ||
                    lower.contains("tel") ||
                    lower == android.view.View.AUTOFILL_HINT_PHONE -> FieldType.PHONE

            // First name
            lower.contains("firstname") ||
                    lower.contains("first_name") ||
                    lower.contains("given") ||
                    lower.contains("first name") -> FieldType.FIRST_NAME

            // Last name
            lower.contains("lastname") ||
                    lower.contains("last_name") ||
                    lower.contains("family") ||
                    lower.contains("surname") ||
                    lower.contains("last name") -> FieldType.LAST_NAME

            // Full name
            lower.contains("name") ||
                    lower.contains("fullname") ||
                    lower == android.view.View.AUTOFILL_HINT_NAME -> FieldType.FULL_NAME

            // Postal code
            lower.contains("postal") ||
                    lower.contains("zip") ||
                    lower == android.view.View.AUTOFILL_HINT_POSTAL_CODE -> FieldType.POSTAL_CODE

            // City
            lower.contains("city") || lower.contains("locality") -> FieldType.CITY

            // Country
            lower.contains("country") || lower.contains("nation") -> FieldType.COUNTRY

            // Address (check after city/postal/country to avoid false matches)
            lower.contains("address") ||
                    lower.contains("street") ||
                    lower == android.view.View.AUTOFILL_HINT_POSTAL_ADDRESS -> FieldType.ADDRESS
            else -> FieldType.UNKNOWN
        }
    }
}
