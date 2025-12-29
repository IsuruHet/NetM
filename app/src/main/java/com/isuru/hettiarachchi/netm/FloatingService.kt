package com.isuru.hettiarachchi.netm

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.graphics.PixelFormat
import android.net.TrafficStats
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.isuru.hettiarachchi.netm.ui.FloatingBubbleUI

class FloatingService : LifecycleService(), SavedStateRegistryOwner {

    private lateinit var wm: WindowManager
    private lateinit var composeView: ComposeView
    private var lastRx: Long = TrafficStats.getTotalRxBytes()
    private var lastTx: Long = TrafficStats.getTotalTxBytes()
    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null

    // SavedStateRegistry implementation
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        savedStateRegistryController.performRestore(null)
        super.onCreate()
        startForegroundService()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        addFloatingBubble()
        startSpeedUpdates()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startForegroundService() {
        val channelId = "net_speed"
        val channel = NotificationChannel(
            channelId,
            "Network Speed Monitor",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Network Speed Running")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(1, notification)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addFloatingBubble() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 200

        composeView = ComposeView(this)
        composeView.setViewTreeLifecycleOwner(this)
        composeView.setViewTreeSavedStateRegistryOwner(this)

        composeView.setContent {
            FloatingBubbleUI(
                onClose = { stopSelf() },
                download = downloadSpeed,
                upload = uploadSpeed
            )
        }

        // Dragging
        composeView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var touchX = 0f
            private var touchY = 0f

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        touchX = event.rawX
                        touchY = event.rawY
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - touchX).toInt()
                        params.y = initialY + (event.rawY - touchY).toInt()
                        wm.updateViewLayout(composeView, params)
                        return true
                    }
                }
                return false
            }
        })

        wm.addView(composeView, params)
    }

    private var downloadSpeed by mutableStateOf("0 KB/s")
    private var uploadSpeed by mutableStateOf("0 KB/s")

    private fun startSpeedUpdates() {
        runnable = object : Runnable {
            override fun run() {
                val currentRx = TrafficStats.getTotalRxBytes()
                val currentTx = TrafficStats.getTotalTxBytes()

                val download = currentRx - lastRx
                val upload = currentTx - lastTx

                lastRx = currentRx
                lastTx = currentTx

                downloadSpeed = format(download)
                uploadSpeed = format(upload)

                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable!!)
    }

    private fun format(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B/s"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB/s"
            else -> "${bytes / (1024 * 1024)} MB/s"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        runnable?.let { handler.removeCallbacks(it) }
        if (::composeView.isInitialized) {
            wm.removeView(composeView)
        }
    }
}