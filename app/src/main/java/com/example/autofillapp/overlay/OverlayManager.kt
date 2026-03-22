package com.example.autofillapp.overlay

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.ImageButton

/**
 * Manages a single floating overlay button (FAB) that appears near focused text fields.
 * Uses WindowManager with TYPE_APPLICATION_OVERLAY.
 */
class OverlayManager(private val context: Context) {

    companion object {
        private const val TAG = "A11Y_AUTOFILL"
        private const val BUTTON_SIZE_DP = 48
        private const val MARGIN_DP = 4
    }

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayButton: ImageButton? = null

    /**
     * Show the floating button near the bottom-right corner of [fieldBounds].
     * Removes any existing overlay first.
     */
    fun showOverlay(fieldBounds: Rect) {
        if (!canDrawOverlay()) {
            Log.w(TAG, "Overlay permission not granted")
            return
        }

        // Remove old button if present
        removeOverlay()

        val density = context.resources.displayMetrics.density
        val buttonSizePx = (BUTTON_SIZE_DP * density).toInt()
        val marginPx = (MARGIN_DP * density).toInt()

        val button = ImageButton(context).apply {
            setImageResource(android.R.drawable.ic_input_add)
            setBackgroundResource(android.R.drawable.btn_default)
            contentDescription = "Autofill"
            alpha = 0.9f
            setOnClickListener { onButtonClicked() }
        }

        val params = WindowManager.LayoutParams(
            buttonSizePx,
            buttonSizePx,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            // Position near bottom-right of the text field
            x = fieldBounds.right - buttonSizePx - marginPx
            y = fieldBounds.bottom + marginPx
        }

        try {
            windowManager.addView(button, params)
            overlayButton = button
            Log.d(TAG, "Overlay shown at (${params.x}, ${params.y})")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add overlay", e)
        }
    }

    /** Remove the floating button from the window. */
    fun removeOverlay() {
        overlayButton?.let { view ->
            try {
                windowManager.removeView(view)
                Log.d(TAG, "Overlay removed")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to remove overlay", e)
            }
        }
        overlayButton = null
    }

    private fun onButtonClicked() {
        Log.d(TAG, "Overlay button clicked — launching popup")
        removeOverlay()

        val intent = Intent(context, AutofillPopupActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(intent)
    }

    private fun canDrawOverlay(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }
}
