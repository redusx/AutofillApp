package com.example.autofillapp.autofill

import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.Dataset
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.service.autofill.SaveCallback
import android.service.autofill.SaveRequest
import android.util.Log
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import com.example.autofillapp.R
import com.example.autofillapp.data.AppDatabase
import com.example.autofillapp.data.ProfileRepository
import com.example.autofillapp.security.CryptoManager
import kotlinx.coroutines.runBlocking

/**
 * Android Autofill Service that fills form fields in other apps using the locally stored and
 * encrypted user profile data.
 *
 * Dependencies are resolved via AppDatabase.getInstance (no DI framework needed for MVP).
 * CryptoManager handles decryption of stored profile data transparently through ProfileRepository.
 */
class MyCustomAutofillService : AutofillService() {

    companion object {
        private const val TAG = "MyCustomAutofillService"
    }

    // Lazy-initialized dependencies (created once per service lifecycle)
    private val cryptoManager by lazy { CryptoManager() }
    private val repository by lazy {
        val dao = AppDatabase.getInstance(applicationContext).userProfileDao()
        ProfileRepository(dao, cryptoManager)
    }

    override fun onFillRequest(
            request: FillRequest,
            cancellationSignal: CancellationSignal,
            callback: FillCallback
    ) {
        Log.d(TAG, "onFillRequest called")

        // 1. Get the latest AssistStructure from the fill contexts
        val structure = request.fillContexts.lastOrNull()?.structure
        if (structure == null) {
            Log.w(TAG, "No AssistStructure available")
            callback.onSuccess(null)
            return
        }

        // 2. Parse ViewNodes — inspect autofillHints, hint, idEntry, inputType
        val parsedFields = AutofillParser.parseStructure(structure)
        if (parsedFields.isEmpty()) {
            Log.d(TAG, "No autofillable fields found in the structure")
            callback.onSuccess(null)
            return
        }
        Log.d(
                TAG,
                "Parsed ${parsedFields.size} autofillable fields: ${parsedFields.map { it.fieldType }}"
        )

        // 3. Get decrypted user profile from encrypted Room DB via repository
        val profile = runBlocking { repository.getProfileSync() }

        if (profile == null) {
            Log.d(TAG, "No profile stored yet, skipping autofill")
            callback.onSuccess(null)
            return
        }

        // 4. Build a Dataset for each matched field
        val responseBuilder = FillResponse.Builder()
        // Provide a default presentation (required by some Android versions/OEMs to display the inline UI)
        val defaultPresentation = RemoteViews(packageName, R.layout.autofill_suggestion).apply {
            setTextViewText(R.id.autofill_suggestion_text, "AutofillApp")
        }
        val datasetBuilder = Dataset.Builder(defaultPresentation)
        var hasAnyValue = false

        for (field in parsedFields) {
            val value = AutofillMapper.getValueForField(field.fieldType, profile)
            if (value != null) {
                // Build the RemoteViews presentation for this field
                val label = AutofillMapper.getDisplayLabel(field.fieldType)
                val presentation =
                        RemoteViews(packageName, R.layout.autofill_item).apply {
                            setTextViewText(R.id.autofill_item_value, value)
                            setTextViewText(
                                    R.id.autofill_item_label,
                                    "$label • ${profile.fullName}"
                            )
                        }

                @Suppress("DEPRECATION")
                datasetBuilder.setValue(
                        field.autofillId,
                        AutofillValue.forText(value),
                        presentation
                )
                hasAnyValue = true
                Log.d(TAG, "Matched field ${field.fieldType} -> \"$value\"")
            }
        }

        if (!hasAnyValue) {
            Log.d(TAG, "No matching values found for detected fields")
            callback.onSuccess(null)
            return
        }

        responseBuilder.addDataset(datasetBuilder.build())
        val response = responseBuilder.build()

        Log.d(TAG, "Returning FillResponse with dataset")
        callback.onSuccess(response)
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        // No-op for MVP — we don't capture data from other apps
        Log.d(TAG, "onSaveRequest called (no-op)")
        callback.onSuccess()
    }
}
