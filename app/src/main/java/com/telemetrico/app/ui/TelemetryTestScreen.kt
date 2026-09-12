package com.telemetrico.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.telemetrico.app.model.ConnectionState
import com.telemetrico.app.model.TelemetryState
import com.telemetrico.app.theme.*

@Composable
fun TelemetryTestScreen(telemetry: TelemetryState, connection: ConnectionState) {
    val drsBorder = if (telemetry.drsActive) DrsCyan else F1Carbon70
    Box(
        Modifier
            .fillMaxSize()
            .background(F1CarbonBlack)
            .then(
                if (telemetry.drsActive) Modifier.border(5.dp, DrsCyan.copy(alpha = 0.7f))
                else Modifier
            )
            .padding(20.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TelemetricoWordmark(compact = true)
                Spacer(Modifier.weight(1f))
                Text("F1 25 / 2025", color = F1Carbon50, fontSize = 12.sp)
                Spacer(Modifier.width(14.dp))
                Box(Modifier.size(9.dp).background(TelemetryGreen, RoundedCornerShape(50)))
                Spacer(Modifier.width(7.dp))
                Text("TELEMETRÍA CONECTADA", color = TelemetryGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            ThinTrackLine(Modifier.fillMaxWidth().height(8.dp), if (telemetry.drsActive) DrsCyan else F1WarmRed)
            Spacer(Modifier.height(10.dp))

            TechnicalPanel(Modifier.fillMaxWidth(), accent = if (telemetry.drsActive) DrsCyan else F1WarmRed) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("RPM", color = F1Carbon50, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(12.dp))
                    RevLights(telemetry.revLightsBits)
                    Spacer(Modifier.weight(1f))
                    Text("${telemetry.engineRpm} RPM", color = F1OffWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            Spacer(Modifier.height(12.dp))

            Row(Modifier.weight(1f).fillMaxWidth()) {
                TechnicalPanel(Modifier.width(125.dp).fillMaxHeight(), accent = Color(0xFFFF3A3A)) {
                    VerticalPedalBar("BRAKE", telemetry.brake, Color(0xFFFF3A3A), Modifier.fillMaxHeight())
                }
                Spacer(Modifier.width(12.dp))

                TechnicalPanel(Modifier.weight(1f).fillMaxHeight(), accent = drsBorder) {
                    Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            BigMetric("GEAR", formatGear(telemetry.gear))
                            Spacer(Modifier.height(8.dp))
                            BigMetric("SPEED", telemetry.speedKph.toString(), "KM/H")
                        }
                        Spacer(Modifier.width(18.dp))
                        Column(Modifier.width(190.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("DRS", color = F1Carbon50, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .background(if (telemetry.drsActive) DrsCyan else F1Carbon90, RoundedCornerShape(12.dp))
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    if (telemetry.drsActive) "ACTIVE" else "OFF",
                                    color = if (telemetry.drsActive) F1CarbonBlack else F1Carbon50,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 22.sp,
                                )
                            }
                            Spacer(Modifier.height(22.dp))
                            Text("REV LIGHTS ${telemetry.revLightsPercent}%", color = F1Carbon50, fontSize = 11.sp)
                            Spacer(Modifier.height(7.dp))
                            Text("CAR #${telemetry.playerCarIndex}", color = F1Carbon50, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(Modifier.width(12.dp))
                TechnicalPanel(Modifier.width(125.dp).fillMaxHeight(), accent = TelemetryGreen) {
                    VerticalPedalBar("THROTTLE", telemetry.throttle, TelemetryGreen, Modifier.fillMaxHeight())
                }
            }

            Spacer(Modifier.height(12.dp))
            TechnicalPanel(Modifier.fillMaxWidth(), accent = F1Carbon70) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TyreTemp("FL", telemetry.innerTyreTemps.getOrElse(2) { 0 })
                    TyreTemp("FR", telemetry.innerTyreTemps.getOrElse(3) { 0 })
                    Text("INNER TYRE TEMPS", color = F1Carbon50, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterVertically))
                    TyreTemp("RL", telemetry.innerTyreTemps.getOrElse(0) { 0 })
                    TyreTemp("RR", telemetry.innerTyreTemps.getOrElse(1) { 0 })
                }
            }
        }
    }
}

private fun formatGear(gear: Int): String = when (gear) {
    -1 -> "R"
    0 -> "N"
    else -> gear.toString()
}

@Composable
private fun TyreTemp(label: String, value: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = F1Carbon50, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(6.dp))
        Text("$value°", color = F1HighVisWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}
