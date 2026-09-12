package com.telemetrico.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telemetrico.app.model.ConnectionPhase
import com.telemetrico.app.model.ConnectionState
import com.telemetrico.app.theme.*

@Composable
fun ConfigurationScreen(connection: ConnectionState, onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(F1CarbonBlack)
            .padding(horizontal = 22.dp, vertical = 16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .background(Color(0xFF0D0E14), RoundedCornerShape(12.dp))
                    .clickable(onClick = onBack)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) { Text("‹", color = F1HighVisWhite, fontSize = 32.sp) }
            Spacer(Modifier.width(18.dp))
            TelemetricoWordmark(compact = true)
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Text("CONFIGURACIÓN", color = F1HighVisWhite, fontSize = 28.sp, fontWeight = FontWeight.Black)
                Text("CONEXIÓN PS5 + F1 25", color = F1Carbon50, fontSize = 14.sp, letterSpacing = 1.sp)
            }
            Spacer(Modifier.width(12.dp))
            Text("⚙", color = F1HighVisWhite, fontSize = 34.sp)
        }
        Spacer(Modifier.height(12.dp))
        ThinTrackLine(Modifier.fillMaxWidth().height(10.dp), F1WarmRed)
        Spacer(Modifier.height(10.dp))

        Row(Modifier.weight(1f)) {
            TechnicalPanel(Modifier.weight(1.15f).fillMaxHeight(), accent = F1WarmRed) {
                Text("CÓMO CONECTARSE", color = F1HighVisWhite, fontSize = 23.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(14.dp))
                ConfigStep("1", "Conectá la tablet Lenovo y la PS5 a la misma red Wi‑Fi")
                ConfigStep("2", "Abrí F1 25 en la PS5")
                ConfigStep("3", "En Ajustes > Telemetría activá UDP")
                ConfigStep("4", "Ingresá la IP de esta tablet y el puerto 20777")
                Spacer(Modifier.weight(1f))
                Box(Modifier.fillMaxWidth().background(F1Carbon90, RoundedCornerShape(12.dp)).padding(12.dp)) {
                    Text("ⓘ  La app detectará automáticamente la sesión cuando salgas a pista.", color = F1OffWhite, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(0.85f).fillMaxHeight()) {
                TechnicalPanel(Modifier.fillMaxWidth().weight(1f), accent = F1WarmRed) {
                    Text("PARÁMETROS RECOMENDADOS", color = F1HighVisWhite, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(10.dp))
                    ParamRow("Formato UDP", "F1 25")
                    ParamRow("IP destino", connection.tabletIp ?: "Ver IP de la tablet")
                    ParamRow("Puerto", connection.port.toString())
                    ParamRow("Your Telemetry", "Restricted")
                    ParamRow("Mostrar ID online", "Off")
                }
                Spacer(Modifier.height(12.dp))
                TechnicalPanel(Modifier.fillMaxWidth(), accent = F1Carbon70) {
                    Text("ESTADO ACTUAL", color = F1HighVisWhite, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(8.dp))
                    StatusRow("◉", "Wi‑Fi", if (connection.tabletIp != null) "OK" else "—", if (connection.tabletIp != null) TelemetryGreen else WarningYellow)
                    StatusRow("⌁", "Datos de carrera", if (connection.phase == ConnectionPhase.RECEIVING) "OK" else "Esperando", if (connection.phase == ConnectionPhase.RECEIVING) TelemetryGreen else F1Carbon50)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D0E14), RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("CONSEJOS RÁPIDOS", color = F1HighVisWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(Modifier.width(22.dp))
            Text("1  Misma red Wi‑Fi", color = F1Carbon50, fontSize = 12.sp)
            Spacer(Modifier.width(30.dp))
            Text("2  Confirmá que la IP coincida", color = F1Carbon50, fontSize = 12.sp)
            Spacer(Modifier.width(30.dp))
            Text("3  La telemetría aparece al entrar a pista", color = F1Carbon50, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ConfigStep(number: String, text: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(42.dp).background(F1WarmRed, RoundedCornerShape(9.dp)), contentAlignment = Alignment.Center) {
            Text(number, color = F1CarbonBlack, fontWeight = FontWeight.Black, fontSize = 22.sp)
        }
        Spacer(Modifier.width(14.dp))
        Text(text, color = F1OffWhite, fontSize = 15.sp, lineHeight = 19.sp)
    }
}

@Composable
private fun ParamRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = F1OffWhite, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(value, color = F1HighVisWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.background(F1Carbon90, RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 5.dp))
    }
}
