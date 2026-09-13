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
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(telemetry: TelemetryState, connection: ConnectionState) {
    val accent = if (telemetry.drsActive) DrsCyan else F1WarmRed
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(F1CarbonBlack)
            .then(if (telemetry.drsActive) Modifier.border(5.dp, DrsCyan.copy(alpha = 0.72f)) else Modifier)
    ) {
        val compact = maxWidth < 950.dp || maxHeight < 620.dp
        val outer = if (compact) 12.dp else 18.dp
        val gap = if (compact) 8.dp else 11.dp
        val panelPadding = if (compact) 10.dp else 13.dp

        Column(Modifier.fillMaxSize().padding(outer)) {
            DashboardHeader(telemetry, compact)
            Spacer(Modifier.height(6.dp))
            ThinTrackLine(Modifier.fillMaxWidth().height(7.dp), accent)
            Spacer(Modifier.height(gap))

            TechnicalPanel(
                Modifier.fillMaxWidth(),
                accent = accent,
                contentPadding = panelPadding,
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("RPM", color = F1Carbon50, fontSize = if (compact) 9.sp else 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(10.dp))
                    RevLights(telemetry.revLightsBits, telemetry.revLightsPercent, compact = compact)
                    Spacer(Modifier.weight(1f))
                    Text(
                        "${telemetry.engineRpm} RPM",
                        color = F1OffWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (compact) 13.sp else 16.sp,
                    )
                }
            }

            Spacer(Modifier.height(gap))

            Row(Modifier.fillMaxWidth().weight(1f)) {
                LeftRaceColumn(telemetry, compact, panelPadding, Modifier.weight(0.92f).fillMaxHeight())
                Spacer(Modifier.width(gap))
                CenterDriveColumn(telemetry, compact, panelPadding, Modifier.weight(1.6f).fillMaxHeight())
                Spacer(Modifier.width(gap))
                RightStrategyColumn(telemetry, compact, panelPadding, Modifier.weight(1.05f).fillMaxHeight())
            }

            Spacer(Modifier.height(gap))
            DashboardAlertStrip(telemetry, compact)
        }
    }
}

@Composable
private fun DashboardHeader(t: TelemetryState, compact: Boolean) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TelemetricoWordmark(compact = true)
        Spacer(Modifier.width(if (compact) 12.dp else 18.dp))
        Column {
            Text(
                t.driverName.uppercase(Locale.getDefault()),
                color = F1HighVisWhite,
                fontSize = if (compact) 10.sp else 12.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                listOfNotNull(
                    t.teamName.takeIf { it.isNotBlank() },
                    t.trackName.takeIf { it.isNotBlank() && it != "TRACK" },
                ).joinToString("  ·  "),
                color = F1Carbon50,
                fontSize = if (compact) 8.sp else 9.sp,
            )
        }
        Spacer(Modifier.weight(1f))
        if (t.position > 0) {
            Text("P${t.position}", color = F1HighVisWhite, fontSize = if (compact) 18.sp else 22.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.width(14.dp))
        }
        Text(
            if (t.totalLaps > 0 && t.currentLap > 0) "LAP ${t.currentLap}/${t.totalLaps}" else "F1 25 / 2025",
            color = F1OffWhite,
            fontSize = if (compact) 9.sp else 11.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.width(14.dp))
        Box(Modifier.size(8.dp).background(TelemetryGreen, RoundedCornerShape(50)))
        Spacer(Modifier.width(6.dp))
        Text("LIVE", color = TelemetryGreen, fontSize = if (compact) 9.sp else 10.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun LeftRaceColumn(t: TelemetryState, compact: Boolean, padding: androidx.compose.ui.unit.Dp, modifier: Modifier) {
    Column(modifier) {
        TechnicalPanel(Modifier.fillMaxWidth().weight(0.72f), accent = F1WarmRed, contentPadding = padding) {
            Text("RACE", color = F1HighVisWhite, fontSize = if (compact) 12.sp else 14.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                SmallMetric("POSITION", if (t.position > 0) "P${t.position}" else "—", Modifier.weight(1f))
                SmallMetric("LAP", if (t.currentLap > 0) "${t.currentLap}/${t.totalLaps.takeIf { it > 0 } ?: "—"}" else "—", Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            SmallMetric("GAP AHEAD", formatGap(t.gapAheadMs))
            Spacer(Modifier.height(8.dp))
            SmallMetric("GAP LEADER", formatGap(t.gapLeaderMs))
            Spacer(Modifier.height(8.dp))
            SmallMetric("SPEED TRAP", if (t.speedTrapFastestKph > 0f) String.format(Locale.US, "%.1f KM/H", t.speedTrapFastestKph) else "—")
        }
        Spacer(Modifier.height(8.dp))
        TechnicalPanel(Modifier.fillMaxWidth().weight(1f), accent = F1Carbon70, contentPadding = padding) {
            Text("TIMING", color = F1HighVisWhite, fontSize = if (compact) 12.sp else 14.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            TimingRow("CURRENT", formatLapTime(t.currentLapTimeMs), F1HighVisWhite)
            TimingRow("LAST", formatLapTime(t.lastLapTimeMs), F1OffWhite)
            TimingRow("BEST", formatLapTime(t.bestLapTimeMs), TelemetryGreen)
            Spacer(Modifier.height(8.dp))
            if (t.penaltiesSeconds > 0) SmallMetric("PENALTIES", "+${t.penaltiesSeconds}s", valueColor = WarningYellow)
            if (t.totalWarnings > 0) SmallMetric("WARNINGS", t.totalWarnings.toString())
            if (t.cornerCuttingWarnings > 0) SmallMetric("CORNER CUTS", t.cornerCuttingWarnings.toString())
        }
    }
}

@Composable
private fun CenterDriveColumn(t: TelemetryState, compact: Boolean, padding: androidx.compose.ui.unit.Dp, modifier: Modifier) {
    TechnicalPanel(modifier, accent = if (t.drsActive) DrsCyan else F1WarmRed, contentPadding = padding) {
        Row(Modifier.fillMaxWidth().weight(1f), verticalAlignment = Alignment.CenterVertically) {
            VerticalPedalBar("BRAKE", t.brake, Color(0xFFFF3A3A), Modifier.width(if (compact) 46.dp else 54.dp).fillMaxHeight())
            Spacer(Modifier.width(if (compact) 8.dp else 12.dp))
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                BigMetric("GEAR", formatGear(t.gear), compact = compact)
                Spacer(Modifier.height(if (compact) 4.dp else 7.dp))
                BigMetric("SPEED", t.speedKph.toString(), "KM/H", compact = compact)
                Spacer(Modifier.height(if (compact) 8.dp else 12.dp))
                DrsState(t, compact)
            }
            Spacer(Modifier.width(if (compact) 8.dp else 12.dp))
            VerticalPedalBar("THROTTLE", t.throttle, TelemetryGreen, Modifier.width(if (compact) 46.dp else 54.dp).fillMaxHeight())
        }

        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(F1Carbon70.copy(alpha = 0.45f)))
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            SectorMetric("S1", t.sector1TimeMs, t.bestSector1Ms, Modifier.weight(1f))
            SectorMetric("S2", t.sector2TimeMs, t.bestSector2Ms, Modifier.weight(1f))
            SectorMetric("S3", t.currentSector3TimeMs, t.bestSector3Ms, Modifier.weight(1f))
        }
    }
}

@Composable
private fun RightStrategyColumn(t: TelemetryState, compact: Boolean, padding: androidx.compose.ui.unit.Dp, modifier: Modifier) {
    Column(modifier) {
        TechnicalPanel(Modifier.fillMaxWidth().weight(0.82f), accent = TelemetryGreen, contentPadding = padding) {
            Text("ENERGY", color = F1HighVisWhite, fontSize = if (compact) 12.sp else 14.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            HorizontalValueBar("FUEL", t.fuelPercent, TelemetryGreen)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                SmallMetric("REMAINING", if (t.fuelRemainingLaps != 0f) String.format(Locale.US, "%.1f LAPS", t.fuelRemainingLaps) else "—", Modifier.weight(1f))
                SmallMetric("TANK", if (t.fuelInTank > 0f) String.format(Locale.US, "%.1f KG", t.fuelInTank) else "—", Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            HorizontalValueBar("ERS", t.ersPercent, DrsCyan)
            Spacer(Modifier.height(7.dp))
            SmallMetric("MODE", t.ersModeLabel)
        }
        Spacer(Modifier.height(8.dp))
        TechnicalPanel(Modifier.fillMaxWidth().weight(1.18f), accent = compoundColor(t.visualTyreCompound), contentPadding = padding) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("TYRES", color = F1HighVisWhite, fontSize = if (compact) 12.sp else 14.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.weight(1f))
                Text(t.tyreCompoundLabel, color = compoundColor(t.visualTyreCompound), fontSize = if (compact) 12.sp else 14.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(7.dp))
            Row(Modifier.fillMaxWidth()) {
                SmallMetric("AGE", "${t.tyreAgeLaps} LAPS", Modifier.weight(1f))
                SmallMetric("LIFE", if (t.tyreLifeSpanLaps > 0) "${t.tyreLifeSpanLaps} LAPS" else "—", Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                TyreCorner("FL", t.innerTyreTemps.getOrElse(2) { 0 }, t.tyreWear.getOrElse(2) { 0f }, Modifier.weight(1f))
                TyreCorner("FR", t.innerTyreTemps.getOrElse(3) { 0 }, t.tyreWear.getOrElse(3) { 0f }, Modifier.weight(1f))
            }
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth()) {
                TyreCorner("RL", t.innerTyreTemps.getOrElse(0) { 0 }, t.tyreWear.getOrElse(0) { 0f }, Modifier.weight(1f))
                TyreCorner("RR", t.innerTyreTemps.getOrElse(1) { 0 }, t.tyreWear.getOrElse(1) { 0f }, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DashboardAlertStrip(t: TelemetryState, compact: Boolean) {
    val alerts = buildList {
        t.safetyCarLabel?.let { add(it) }
        t.fiaFlagLabel?.let { add(it) }
        if (t.pitLimiterActive) add("PIT LIMITER")
        if (t.currentLapInvalid) add("INVALID LAP")
        if (t.unservedDriveThrough > 0) add("DRIVE THROUGH")
        if (t.unservedStopGo > 0) add("STOP & GO")
        if (t.drsAllowed && !t.drsActive) add("DRS AVAILABLE")
    }
    val main = alerts.firstOrNull() ?: "SYSTEM NORMAL"
    val color = when {
        main.contains("YELLOW") || main.contains("SAFETY") || main.contains("DRIVE") || main.contains("STOP") -> WarningYellow
        main.contains("BLUE") || main.contains("DRS") -> DrsCyan
        main == "SYSTEM NORMAL" || main.contains("GREEN") -> TelemetryGreen
        else -> F1WarmRed
    }
    Row(
        Modifier
            .fillMaxWidth()
            .border(1.dp, color.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            .background(Color(0xFF0E0F15), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = if (compact) 7.dp else 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(8.dp).background(color, RoundedCornerShape(50)))
        Spacer(Modifier.width(8.dp))
        Text(main, color = color, fontSize = if (compact) 10.sp else 11.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.weight(1f))
        if (t.penaltiesSeconds > 0) Text("+${t.penaltiesSeconds}s", color = WarningYellow, fontSize = 11.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun DrsState(t: TelemetryState, compact: Boolean) {
    val (label, bg, fg) = when {
        t.drsActive -> Triple("DRS ACTIVE", DrsCyan, F1CarbonBlack)
        t.drsAllowed -> Triple("DRS AVAILABLE", DrsCyan.copy(alpha = 0.13f), DrsCyan)
        t.drsActivationDistanceM > 0 -> Triple("DRS ${t.drsActivationDistanceM} M", F1Carbon90, F1OffWhite)
        else -> Triple("DRS", F1Carbon90, F1Carbon50)
    }
    Box(
        Modifier
            .widthIn(min = if (compact) 120.dp else 155.dp)
            .background(bg, RoundedCornerShape(11.dp))
            .padding(horizontal = 14.dp, vertical = if (compact) 8.dp else 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = fg, fontWeight = FontWeight.Black, fontSize = if (compact) 11.sp else 13.sp)
    }
}

@Composable
private fun TimingRow(label: String, value: String, color: Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = F1Carbon50, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(value, color = color, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SectorMetric(label: String, currentMs: Long, bestMs: Long, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = F1Carbon50, fontSize = 9.sp, fontWeight = FontWeight.Black)
        Text(formatSectorTime(currentMs), color = F1HighVisWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        if (bestMs > 0) Text("BEST ${formatSectorTime(bestMs)}", color = TelemetryGreen, fontSize = 8.sp)
    }
}

@Composable
private fun TyreCorner(label: String, temp: Int, wear: Float, modifier: Modifier = Modifier) {
    Column(modifier.padding(end = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = F1Carbon50, fontSize = 9.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.width(5.dp))
            Text(if (temp > 0) "$temp°" else "—", color = F1HighVisWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("${wear.roundToInt()}%", color = tyreWearColor(wear), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(3.dp))
        Box(Modifier.fillMaxWidth().height(4.dp).background(F1Carbon90, RoundedCornerShape(50))) {
            Box(
                Modifier.fillMaxHeight().fillMaxWidth((wear.coerceIn(0f, 100f) / 100f)).background(tyreWearColor(wear), RoundedCornerShape(50))
            )
        }
    }
}

private fun formatGear(gear: Int): String = when (gear) {
    -1 -> "R"
    0 -> "N"
    else -> gear.toString()
}

private fun formatLapTime(ms: Long): String {
    if (ms <= 0) return "—:—.---"
    val minutes = ms / 60_000
    val seconds = (ms % 60_000) / 1000
    val millis = ms % 1000
    return String.format(Locale.US, "%d:%02d.%03d", minutes, seconds, millis)
}

private fun formatSectorTime(ms: Long): String {
    if (ms <= 0) return "—"
    val seconds = ms / 1000
    val millis = ms % 1000
    return if (seconds >= 60) formatLapTime(ms) else String.format(Locale.US, "%d.%03d", seconds, millis)
}

private fun formatGap(ms: Long): String = if (ms <= 0) "—" else String.format(Locale.US, "+%.3f", ms / 1000.0)

private fun compoundColor(visualCompound: Int): Color = when (visualCompound) {
    16 -> Color(0xFFFF2D2D)
    17 -> WarningYellow
    18 -> F1OffWhite
    7 -> Color(0xFF3BE36B)
    8 -> Color(0xFF3A7BFF)
    else -> F1Carbon50
}

private fun tyreWearColor(wear: Float): Color = when {
    wear >= 70f -> F1WarmRed
    wear >= 45f -> WarningYellow
    else -> TelemetryGreen
}
