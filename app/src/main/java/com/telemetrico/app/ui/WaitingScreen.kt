package com.telemetrico.app.ui

import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telemetrico.app.R
import com.telemetrico.app.model.ConnectionPhase
import com.telemetrico.app.model.ConnectionState
import com.telemetrico.app.theme.*

@Composable
fun WaitingScreen(
    connection: ConnectionState,
    onOpenSettings: () -> Unit,
) {
    WaitingSoundtrack()

    Row(
        Modifier
            .fillMaxSize()
            .background(F1CarbonBlack)
            .padding(horizontal = 26.dp, vertical = 20.dp)
    ) {
        Column(Modifier.weight(0.88f).fillMaxHeight()) {
            TelemetricoWordmark()
            Spacer(Modifier.weight(0.36f))
            Image(
                painter = painterResource(R.drawable.f1_logo_personal),
                contentDescription = "F1",
                contentScale = ContentScale.Fit,
                modifier = Modifier.width(285.dp).height(96.dp),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "ESPERANDO",
                color = F1HighVisWhite,
                fontWeight = FontWeight.Black,
                fontSize = 44.sp,
                lineHeight = 44.sp,
            )
            Text(
                "TELEMETRÍA",
                color = F1WarmRed,
                fontWeight = FontWeight.Black,
                fontSize = 44.sp,
                lineHeight = 46.sp,
            )
            Spacer(Modifier.height(12.dp))
            Text("No se detectan datos de F1 25 todavía.", color = F1OffWhite, fontSize = 18.sp)
            Spacer(Modifier.height(14.dp))
            Box(Modifier.width(100.dp).height(4.dp).background(F1WarmRed))
            Spacer(Modifier.height(14.dp))
            Text(
                "Iniciá una sesión en PS5 y la app pasará automáticamente al dashboard cuando empiecen a llegar datos.",
                color = F1Carbon50,
                fontSize = 15.sp,
                lineHeight = 21.sp,
                modifier = Modifier.widthIn(max = 490.dp),
            )
            Spacer(Modifier.weight(1f))
            Text("MÁS DATOS  ·  MEJORES VUELTAS", color = F1Carbon50, fontSize = 10.sp, letterSpacing = 2.sp)
        }

        Spacer(Modifier.width(24.dp))

        Column(Modifier.weight(1.25f).fillMaxHeight()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Row(
                    modifier = Modifier
                        .background(Color(0xFF0D0E14), RoundedCornerShape(14.dp))
                        .clickable(onClick = onOpenSettings)
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("⚙", fontSize = 24.sp, color = F1HighVisWhite)
                    Spacer(Modifier.width(9.dp))
                    Text("CONFIGURACIÓN", color = F1HighVisWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            Spacer(Modifier.height(16.dp))
            TechnicalPanel(Modifier.fillMaxWidth().weight(1f), accent = F1WarmRed) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("ESTADO DE CONEXIÓN", color = F1HighVisWhite, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(Modifier.weight(1f))
                    val pulseColor = when (connection.phase) {
                        ConnectionPhase.RECEIVING -> TelemetryGreen
                        ConnectionPhase.ERROR, ConnectionPhase.UNSUPPORTED_FORMAT -> F1WarmRed
                        else -> WarningYellow
                    }
                    Box(Modifier.size(10.dp).background(pulseColor, RoundedCornerShape(50)))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        when (connection.phase) {
                            ConnectionPhase.RECEIVING -> "RECIBIENDO"
                            ConnectionPhase.ERROR -> "ERROR"
                            ConnectionPhase.UNSUPPORTED_FORMAT -> "FORMATO INCORRECTO"
                            else -> "ESCUCHANDO..."
                        },
                        color = F1OffWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(10.dp))
                StatusRow("◉", "Wi‑Fi", if (connection.tabletIp != null) "Conectado" else "Sin IP", if (connection.tabletIp != null) TelemetryGreen else WarningYellow)
                StatusRow("⌁", "UDP", when (connection.phase) {
                    ConnectionPhase.RECEIVING -> "Recibiendo"
                    ConnectionPhase.ERROR -> "Error"
                    else -> "Escuchando"
                }, if (connection.phase == ConnectionPhase.ERROR) F1WarmRed else F1OffWhite)
                StatusRow("●", "Telemetría", if (connection.telemetryActive) "Activa" else "Esperando", if (connection.telemetryActive) TelemetryGreen else WarningYellow)
                StatusRow("#", "Formato", connection.packetFormat?.toString() ?: "F1 25 / 2025")
                StatusRow("□", "Puerto", connection.port.toString())
                StatusRow("▤", "IP de esta tablet", connection.tabletIp ?: "No detectada")
                connection.message?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it, color = if (connection.phase == ConnectionPhase.ERROR || connection.phase == ConnectionPhase.UNSUPPORTED_FORMAT) F1WarmRed else F1Carbon50, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(14.dp))
            TechnicalPanel(Modifier.fillMaxWidth(), accent = F1Carbon70) {
                Text("PARA EMPEZAR", color = F1HighVisWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Step("1", "Misma red Wi‑Fi")
                    Step("2", "UDP activado")
                    Step("3", "IP + puerto 20777")
                }
            }
        }
    }
}

@Composable
private fun WaitingSoundtrack() {
    val context = LocalContext.current
    DisposableEffect(context) {
        val player = runCatching {
            MediaPlayer.create(context, R.raw.waiting_soundtrack)?.apply {
                isLooping = true
                setVolume(0.38f, 0.38f)
                start()
            }
        }.getOrNull()

        onDispose {
            runCatching { player?.stop() }
            player?.release()
        }
    }
}

@Composable
private fun Step(number: String, text: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(150.dp)) {
        Box(Modifier.size(30.dp).background(F1WarmRed, RoundedCornerShape(50)), contentAlignment = Alignment.Center) {
            Text(number, color = F1CarbonBlack, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(6.dp))
        Text(text, color = F1OffWhite, fontSize = 12.sp)
    }
}
