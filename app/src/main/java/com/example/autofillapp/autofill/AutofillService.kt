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
 */
class MyAutofillService : AutofillService() {

    companion object {
        private const val TAG = "AutofillService"
    }

    override fun onFillRequest(
            request: FillRequest,
            cancellationSignal: CancellationSignal,
            callback: FillCallback
    ) {
        Log.d(TAG, "onFillRequest called")

        val structure = request.fillContexts.lastOrNull()?.structure
        if (structure == null) {
            Log.w(TAG, "No AssistStructure available")
            callback.onSuccess(null)
            return
        }

        // Parse the form fields
        val parsedFields = AutofillParser.parseStructure(structure)
        if (parsedFields.isEmpty()) {
            Log.d(TAG, "No autofillable fields found")
            callback.onSuccess(null)
            return
        }

        // Get user profile (on binder thread, using runBlocking)
        val crypto = CryptoManager()
        val dao = AppDatabase.getInstance(applicationContext).userProfileDao()
        val repository = ProfileRepository(dao, crypto)

        val profile = runBlocking { repository.getProfileSync() }

        if (profile == null) {
            Log.d(TAG, "No profile stored, skipping autofill")
            callback.onSuccess(null)
            return
        }

        // Build dataset
        val datasetBuilder = Dataset.Builder()
        var hasAnyValue = false

        for (field in parsedFields) {
            val value = AutofillMapper.getValueForField(field.fieldType, profile)
            if (value != null) {
                val presentation = RemoteViews(packageName, R.layout.autofill_suggestion)
                presentation.setTextViewText(R.id.autofill_suggestion_text, value)

                datasetBuilder.setValue(
                        field.autofillId,
                        AutofillValue.forText(value),
                        presentation
                )
                hasAnyValue = true
            }
        }

        if (!hasAnyValue) {
            Log.d(TAG, "No matching values for detected fields")
            callback.onSuccess(null)
            return
        }

        val response = FillResponse.Builder().addDataset(datasetBuilder.build()).build()

        Log.d(TAG, "Returning FillResponse with ${parsedFields.size} fields")
        callback.onSuccess(response)
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        // No-op for MVP — we don't capture data from other apps
        Log.d(TAG, "onSaveRequest called (no-op)")
        callback.onSuccess()
    }
}
