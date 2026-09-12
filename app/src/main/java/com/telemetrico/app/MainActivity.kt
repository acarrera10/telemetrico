package com.telemetrico.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.telemetrico.app.model.ConnectionState
import com.telemetrico.app.model.TelemetryState
import com.telemetrico.app.network.UdpTelemetryReceiver
import com.telemetrico.app.theme.TelemetricoTheme
import com.telemetrico.app.ui.ConfigurationScreen
import com.telemetrico.app.ui.TelemetryTestScreen
import com.telemetrico.app.ui.WaitingScreen

class MainActivity : ComponentActivity() {
    private var telemetry by mutableStateOf(TelemetryState())
    private var connection by mutableStateOf(ConnectionState())
    private var receiver: UdpTelemetryReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableImmersiveMode()

        receiver = UdpTelemetryReceiver(
            port = 20777,
            onConnection = { state -> runOnUiThread { connection = state } },
            onTelemetry = { state -> runOnUiThread { telemetry = state } },
        ).also { it.start() }

        setContent {
            TelemetricoTheme {
                var configurationOpen by remember { mutableStateOf(false) }

                when {
                    configurationOpen -> ConfigurationScreen(
                        connection = connection,
                        onBack = { configurationOpen = false },
                    )
                    connection.telemetryActive -> TelemetryTestScreen(
                        telemetry = telemetry,
                        connection = connection,
                    )
                    else -> WaitingScreen(
                        connection = connection,
                        onOpenSettings = { configurationOpen = true },
                    )
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) enableImmersiveMode()
    }

    override fun onDestroy() {
        receiver?.stop()
        receiver = null
        super.onDestroy()
    }

    private fun enableImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
