package com.example.autofillapp.accessibility

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.autofillapp.overlay.OverlayManager

/**
 * AccessibilityService that detects text field focus in any app and shows a
 * floating autofill button near the focused field.
 *
 * Works alongside the existing AutofillService — this provides an overlay-based
 * alternative for apps/fields where the standard autofill dropdown doesn't appear.
 */
class MyAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "A11Y_AUTOFILL"

        /** Currently focused node — used by AutofillPopupActivity to perform ACTION_SET_TEXT. */
        var currentFocusedNode: AccessibilityNodeInfo? = null
            private set
    }

    private var overlayManager: OverlayManager? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        overlayManager = OverlayManager(this)
        Log.d(TAG, "AccessibilityService connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_FOCUSED,
            AccessibilityEvent.TYPE_VIEW_CLICKED -> handleFocusEvent(event)

            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                // Ignore window state changes caused by our own app (e.g. showing the overlay)
                if (event.packageName?.toString() == packageName) {
                    return
                }
                // App or screen changed — remove overlay
                Log.d(TAG, "Window state changed, removing overlay (package: ${event.packageName})")
                hideOverlay()
            }
        }
    }

    private fun handleFocusEvent(event: AccessibilityEvent) {
        val source = event.source ?: return

        if (isTextField(source)) {
            Log.d(TAG, "TextField detected: ${source.className} | hint=${source.hintText}")

            // Store reference for later ACTION_SET_TEXT
            currentFocusedNode = source

            // Get field bounds
            val rect = Rect()
            source.getBoundsInScreen(rect)

            Log.d(TAG, "Field bounds: $rect")
            overlayManager?.showOverlay(rect)
        } else {
            // Focused something that isn't a text field — hide
            hideOverlay()
        }
    }

    private fun isTextField(node: AccessibilityNodeInfo): Boolean {
        val className = node.className?.toString() ?: return false
        return className.contains("EditText", ignoreCase = true) ||
                className.contains("AutoCompleteTextView", ignoreCase = true) ||
                (node.isEditable && className.contains("TextView", ignoreCase = true))
    }

    private fun hideOverlay() {
        currentFocusedNode = null
        overlayManager?.removeOverlay()
    }

    override fun onInterrupt() {
        Log.d(TAG, "AccessibilityService interrupted")
        hideOverlay()
    }

    override fun onDestroy() {
        super.onDestroy()
        hideOverlay()
        overlayManager = null
        Log.d(TAG, "AccessibilityService destroyed")
    }

    /**
     * Called from AutofillPopupActivity to insert text into the focused field.
     */
    fun fillFocusedField(value: String) {
        val node = currentFocusedNode
        if (node == null) {
            Log.w(TAG, "No focused node to fill")
            return
        }
        val args = Bundle().apply {
            putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                value
            )
        }
        val success = node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        Log.d(TAG, "Text inserted: \"$value\" success=$success")
        hideOverlay()
    }
}
