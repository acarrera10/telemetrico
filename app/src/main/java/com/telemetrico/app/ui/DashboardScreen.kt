package com.telemetrico.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telemetrico.app.R
import com.telemetrico.app.model.ConnectionState
import com.telemetrico.app.model.TelemetryState
import java.util.Locale
import kotlin.math.min
import kotlin.math.roundToInt

private const val BASE_WIDTH = 1600f
private const val BASE_HEIGHT = 900f
private const val OUTER_MARGIN = 12f

private val CanvasBg = Color(0xFF02070B)
private val PanelTop = Color(0xFF0C1821)
private val PanelBottom = Color(0xFF03080C)
private val PanelLine = Color(0xFF31414E)
private val Divider = Color(0xFF44505A)
private val TextMain = Color(0xFFF5F6F7)
private val TextSoft = Color(0xFFAFB8C1)
private val TextMuted = Color(0xFF7F8B97)
private val Cyan = Color(0xFF00E3E8)
private val Green = Color(0xFF00EB76)
private val Yellow = Color(0xFFFFD21A)
private val Red = Color(0xFFFF3039)
private val Orange = Color(0xFFFF8A18)
private val Blue = Color(0xFF4D8DFF)

private val DisplayFont = FontFamily(
    Font(R.font.nimbus_sans_narrow_regular, FontWeight.Normal),
    Font(R.font.nimbus_sans_narrow_bold, FontWeight.Bold),
    Font(R.font.nimbus_sans_narrow_bold, FontWeight.Black),
)

private val UiFont = FontFamily(
    Font(R.font.nimbus_sans_regular, FontWeight.Normal),
    Font(R.font.nimbus_sans_bold, FontWeight.Bold),
    Font(R.font.nimbus_sans_bold, FontWeight.Black),
)

private enum class RaceUiState {
    NORMAL,
    YELLOW_FLAG,
    SAFETY_CAR,
    VSC,
    PIT_LIMITER,
    PENALTY,
    BLUE_FLAG,
    INVALID_LAP,
}

private data class RaceVisualState(
    val state: RaceUiState,
    val label: String = "",
    val detail: String = "",
    val accent: Color = Red,
    val strong: Boolean = false,
)

private fun raceVisualState(t: TelemetryState): RaceVisualState = when {
    t.unservedStopGo > 0 -> RaceVisualState(RaceUiState.PENALTY, "STOP & GO", "PENALTY", Red, true)
    t.unservedDriveThrough > 0 -> RaceVisualState(RaceUiState.PENALTY, "DRIVE THROUGH", "PENALTY", Red, true)
    t.pitLimiterActive -> RaceVisualState(RaceUiState.PIT_LIMITER, "PIT LIMITER", "ACTIVE", Orange, true)
    t.safetyCarStatus == 2 -> RaceVisualState(RaceUiState.VSC, "VIRTUAL SAFETY CAR", "VSC", Yellow, true)
    t.safetyCarStatus == 1 -> RaceVisualState(RaceUiState.SAFETY_CAR, "SAFETY CAR", "SC", Yellow, true)
    t.fiaFlag == 3 -> RaceVisualState(RaceUiState.YELLOW_FLAG, "YELLOW FLAG", "CAUTION", Yellow, true)
    t.fiaFlag == 2 -> RaceVisualState(RaceUiState.BLUE_FLAG, "BLUE FLAG", "LET CAR PASS", Blue)
    t.currentLapInvalid -> RaceVisualState(RaceUiState.INVALID_LAP, "INVALID LAP", "LAP TIME", Red)
    t.penaltiesSeconds > 0 -> RaceVisualState(RaceUiState.PENALTY, "PENALTY +${t.penaltiesSeconds}s", "TIME PENALTY", Red, true)
    else -> RaceVisualState(RaceUiState.NORMAL)
}

@Composable
fun DashboardScreen(telemetry: TelemetryState, connection: ConnectionState) {
    val raceState = raceVisualState(telemetry)
    BoxWithConstraints(
        Modifier.fillMaxSize().background(CanvasBg),
        contentAlignment = Alignment.Center,
    ) {
        val scale = min((maxWidth.value - OUTER_MARGIN * 2f) / BASE_WIDTH, (maxHeight.value - OUTER_MARGIN * 2f) / BASE_HEIGHT).coerceAtLeast(0.01f)
        Box(Modifier.size((BASE_WIDTH * scale).dp, (BASE_HEIGHT * scale).dp)) {
            Box(
                Modifier
                    .size(BASE_WIDTH.dp, BASE_HEIGHT.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = TransformOrigin(0f, 0f)
                    }
                    .background(Brush.radialGradient(colors = listOf(Color(0x2B08212D), Color.Transparent), radius = 720f))
                    .background(CanvasBg)
            ) {
                Header(telemetry, raceState)
                PositionPanel(telemetry, raceState)
                DrivePanel(telemetry, raceState)
                FuelPanel(telemetry)
                ErsPanel(telemetry)
                TyresPanel(telemetry)
                BottomTiming(telemetry, raceState)
                StateChrome(raceState)
            }
        }
    }
}

@Composable
private fun BoxScope.StateChrome(state: RaceVisualState) {
    if (state.state == RaceUiState.NORMAL) return
    val alpha = if (state.strong) .92f else .70f
    Box(
        Modifier.offset(2.dp, 2.dp).size(1596.dp, 896.dp)
            .border(9.dp, state.accent.copy(alpha = .08f), RoundedCornerShape(20.dp))
    )
    Box(
        Modifier.offset(5.dp, 5.dp).size(1590.dp, 890.dp)
            .border(5.dp, state.accent.copy(alpha = .18f), RoundedCornerShape(18.dp))
    )
    Box(
        Modifier.offset(8.dp, 8.dp).size(1584.dp, 884.dp)
            .border(2.dp, state.accent.copy(alpha = alpha), RoundedCornerShape(16.dp))
    )
}

@Composable
private fun BoxScope.Header(t: TelemetryState, state: RaceVisualState) {
    val accent = if (state.state == RaceUiState.NORMAL) Red else state.accent
    Panel(
        Modifier.offset(0.dp, 0.dp).size(1600.dp, 116.dp),
        radius = 17f,
        borderColor = accent.copy(alpha = if (state.state == RaceUiState.NORMAL) .42f else .78f),
    ) {
        Box(
            Modifier.fillMaxSize().background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF071019), Color(0xFF050B11), Color(0xFF071019), Color(0xFF040A0F))
                )
            )
        )
        Box(Modifier.offset(0.dp, 0.dp).size(2.dp, 116.dp).background(accent))
        Box(Modifier.offset(0.dp, 114.dp).size(1600.dp, 2.dp).background(accent.copy(alpha = .58f)))

        teamLogoRes(t.teamName)?.let { logo ->
            Image(
                painter = painterResource(logo),
                contentDescription = "${t.teamName} logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.offset(39.dp, 32.dp).size(82.dp, 52.dp),
            )
        }
        Text(
            t.driverName.uppercase(Locale.getDefault()),
            color = TextMain,
            fontFamily = DisplayFont,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            fontStyle = FontStyle.Italic,
            modifier = Modifier.offset(150.dp, 29.dp).width(420.dp),
        )
        Text(
            t.teamName.uppercase(Locale.getDefault()),
            color = Color(0xFFA4ADB7),
            fontFamily = UiFont,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.offset(150.dp, 67.dp).width(420.dp),
        )

        if (state.state != RaceUiState.NORMAL) {
            StatusBadge(state)
        } else {
            repeat(3) { i ->
                Box(
                    Modifier.offset((835 + i * 41).dp, 0.dp).size(27.dp, 112.dp)
                        .background(Color(0xFF101923).copy(alpha = .52f))
                )
            }
        }

        Box(Modifier.offset(1010.dp, 0.dp).size(1.dp, 116.dp).background(Color(0xFF202C35)))
        Text(
            sessionLabel(t.sessionType),
            color = Color(0xFFBCC4CB),
            fontFamily = UiFont,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.offset(1050.dp, 30.dp).width(150.dp),
        )
        Text(
            if (t.trackName != "TRACK") t.trackName.uppercase(Locale.getDefault()) else "F1 25",
            color = TextMain,
            fontFamily = DisplayFont,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.offset(1050.dp, 58.dp).width(235.dp),
        )

        // Reserved visual slot for a future circuit trace.
        Box(
            Modifier.offset(1305.dp, 20.dp).size(260.dp, 76.dp)
                .border(1.dp, Color(0xFF263541), RoundedCornerShape(10.dp))
                .background(Color(0x1200E3E8), RoundedCornerShape(10.dp))
        )
        repeat(3) { i ->
            Box(
                Modifier.offset((1368 + i * 34).dp, 31.dp).size(18.dp, 54.dp)
                    .background(Color(0xFF15232D).copy(alpha = .65f), RoundedCornerShape(9.dp))
            )
        }
    }
}

@Composable
private fun BoxScope.StatusBadge(state: RaceVisualState) {
    val width = when (state.state) {
        RaceUiState.VSC -> 390
        RaceUiState.PENALTY -> 360
        else -> 320
    }
    val x = 610 + (390 - width) / 2
    Box(
        Modifier.offset((x - 8).dp, 19.dp).size((width + 16).dp, 79.dp)
            .border(8.dp, state.accent.copy(alpha = .08f), RoundedCornerShape(13.dp))
    )
    Box(
        Modifier.offset((x - 4).dp, 23.dp).size((width + 8).dp, 71.dp)
            .border(4.dp, state.accent.copy(alpha = .18f), RoundedCornerShape(11.dp))
    )
    Box(
        Modifier.offset(x.dp, 27.dp).size(width.dp, 63.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF071018), state.accent.copy(alpha = .12f), Color(0xFF071018))
                ),
                RoundedCornerShape(8.dp),
            )
            .border(if (state.strong) 2.dp else 1.dp, state.accent, RoundedCornerShape(8.dp))
    ) {
        Box(
            Modifier.offset(0.dp, 0.dp).size(7.dp, 63.dp)
                .background(state.accent, RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
        )
        Text(
            state.label,
            color = state.accent,
            fontFamily = DisplayFont,
            fontSize = if (state.label.length > 16) 27.sp else 32.sp,
            fontWeight = FontWeight.Black,
            fontStyle = FontStyle.Italic,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.offset(25.dp, 8.dp).width((width - 40).dp),
        )
        Text(
            state.detail,
            color = TextSoft,
            fontFamily = UiFont,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.offset(26.dp, 42.dp).width((width - 40).dp),
        )
    }
}

@Composable
private fun BoxScope.PositionPanel(t: TelemetryState, state: RaceVisualState) {
    val accent = if (state.state == RaceUiState.NORMAL) Red else state.accent
    Panel(Modifier.offset(0.dp, 126.dp).size(344.dp, 627.dp), radius = 17f, borderColor = if (state.strong) accent.copy(alpha = .45f) else PanelLine) {
        Label("POSITION", 47, 29)
        Text(if (t.position > 0) "P${t.position}" else "—", color = TextMain, fontFamily = DisplayFont, fontSize = 126.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic, modifier = Modifier.offset(43.dp, 67.dp).width(260.dp))
        Box(Modifier.offset(47.dp, 210.dp).size(267.dp, 1.dp).background(Divider))
        Box(Modifier.offset(47.dp, 209.dp).size(55.dp, 3.dp).background(accent))
        Label("LAP", 47, 244)
        Text(if (t.currentLap > 0 && t.totalLaps > 0) "${t.currentLap} / ${t.totalLaps}" else "—", color = TextMain, fontFamily = DisplayFont, fontSize = 57.sp, fontWeight = FontWeight.Bold, modifier = Modifier.offset(47.dp, 276.dp).width(260.dp))
        Box(Modifier.offset(47.dp, 361.dp).size(267.dp, 1.dp).background(Divider))
        Label("GAP AHEAD", 47, 396)
        Text(formatGap(t.gapAheadMs), color = TextMain, fontFamily = DisplayFont, fontSize = 43.sp, fontWeight = FontWeight.Bold, modifier = Modifier.offset(47.dp, 428.dp).width(250.dp))
        Box(Modifier.offset(47.dp, 495.dp).size(267.dp, 1.dp).background(Divider))
        Label("GAP LEADER", 47, 529)
        Text(formatGap(t.gapLeaderMs), color = TextMain, fontFamily = DisplayFont, fontSize = 43.sp, fontWeight = FontWeight.Bold, modifier = Modifier.offset(47.dp, 561.dp).width(250.dp))
    }
}

@Composable
private fun BoxScope.DrivePanel(t: TelemetryState, state: RaceVisualState) {
    val border = if (state.strong) state.accent.copy(alpha = .42f) else PanelLine
    Panel(Modifier.offset(356.dp, 126.dp).size(680.dp, 627.dp), radius = 17f, borderColor = border) {
        Label("RPM ×1000", 30, 13)
        RevLights(t)
        Pedal("BRAKE", t.brake, Red, x = 30)
        Pedal("THROTTLE", t.throttle, Green, x = 552)
        Text("GEAR", color = Color(0xFFC0C7CE), fontFamily = UiFont, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.offset(170.dp, 139.dp).width(346.dp))
        Text(formatGear(t.gear), color = TextMain, fontFamily = DisplayFont, fontSize = 195.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, modifier = Modifier.offset(170.dp, 167.dp).width(346.dp))
        Text(t.speedKph.toString(), color = TextMain, fontFamily = DisplayFont, fontSize = 103.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, modifier = Modifier.offset(170.dp, 343.dp).width(346.dp))
        Text("KM/H", color = Color(0xFFC8CFD5), fontFamily = UiFont, fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.offset(170.dp, 441.dp).width(346.dp))
        DrsModule(t)
    }
}

@Composable
private fun BoxScope.RevLights(t: TelemetryState) {
    val fallback = ((t.revLightsPercent.coerceIn(0, 100) / 100f) * 15f).roundToInt().coerceIn(0, 15)
    val effectiveBits = if (t.revLightsBits != 0 || t.revLightsPercent == 0) t.revLightsBits else if (fallback == 0) 0 else (1 shl fallback) - 1
    Row(Modifier.offset(31.dp, 40.dp).size(620.dp, 32.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(15) { i ->
            val active = effectiveBits and (1 shl i) != 0
            val zoneColor = when { i < 5 -> Green; i < 10 -> Yellow; else -> Red }
            Box(Modifier.size(32.dp).background(if (active) zoneColor else Color(0xFF11191F), CircleShape).border(1.dp, if (active) zoneColor.copy(alpha = .9f) else Color(0x3DFFFFFF), CircleShape))
            if (i < 14) Box(Modifier.width(9.95.dp))
        }
    }
    Row(Modifier.offset(31.dp, 79.dp).size(620.dp, 24.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(15) { i -> Text("${i + 1}", color = Color(0xFFD0D6DB), fontFamily = DisplayFont, fontSize = 17.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.width(41.33.dp)) }
    }
}

@Composable
private fun BoxScope.Pedal(label: String, value: Float, color: Color, x: Int) {
    val labelX = if (label == "THROTTLE") x - 12 else x
    val labelWidth = if (label == "THROTTLE") 128 else 104
    Text(
        label,
        color = Color(0xFFF2F3F4),
        fontFamily = UiFont,
        fontSize = if (label == "THROTTLE") 21.sp else 24.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        maxLines = 1,
        softWrap = false,
        modifier = Modifier.offset(labelX.dp, 151.dp).width(labelWidth.dp),
    )
    Box(Modifier.offset((x + 34).dp, 195.dp).size(45.dp, 284.dp).background(Color(0xFF091015), RoundedCornerShape(7.dp)).border(2.dp, if (label == "THROTTLE") Green.copy(alpha = .44f) else Color(0xFF293743), RoundedCornerShape(7.dp)).padding(4.dp)) {
        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().fillMaxHeight(value.coerceIn(0f, 1f)).background(color, RoundedCornerShape(3.dp)))
    }
    Text("${(value.coerceIn(0f, 1f) * 100f).roundToInt()}%", color = TextMain, fontFamily = DisplayFont, fontSize = 32.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.offset(x.dp, 487.dp).width(104.dp))
}

@Composable
private fun BoxScope.DrsModule(t: TelemetryState) {
    val active = t.drsActive
    val available = t.drsAllowed && !active
    val illuminated = active || available
    val border = if (illuminated) Cyan else PanelLine

    if (illuminated) {
        Box(
            Modifier.offset(118.dp, 538.dp).size(459.dp, 86.dp)
                .border(8.dp, Cyan.copy(alpha = .07f), RoundedCornerShape(13.dp))
        )
        Box(
            Modifier.offset(122.dp, 542.dp).size(451.dp, 78.dp)
                .border(4.dp, Cyan.copy(alpha = .16f), RoundedCornerShape(10.dp))
        )
    }

    Panel(
        Modifier.offset(126.dp, 546.dp).size(443.dp, 70.dp),
        radius = 7f,
        borderColor = border,
        borderWidth = if (active) 3f else if (available) 2f else 1f,
        background = if (illuminated) {
            Brush.horizontalGradient(listOf(Color(0xFF041014), Cyan.copy(alpha = .10f), Color(0xFF041014)))
        } else {
            Brush.horizontalGradient(listOf(Color(0xFF061018), Color(0xFF061018)))
        },
    ) {
        Box(Modifier.offset(24.dp, 0.dp).size(110.dp, 70.dp), contentAlignment = Alignment.CenterStart) {
            Text(
                "DRS",
                color = TextMain,
                fontFamily = DisplayFont,
                fontSize = 37.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                maxLines = 1,
            )
        }
        Box(Modifier.offset(125.dp, 0.dp).size(210.dp, 70.dp), contentAlignment = Alignment.Center) {
            Text(
                when {
                    active -> "ACTIVE"
                    available -> "AVAILABLE"
                    t.drsActivationDistanceM > 0 -> "${t.drsActivationDistanceM} M"
                    else -> "—"
                },
                color = if (illuminated) Cyan else TextMuted,
                fontFamily = DisplayFont,
                fontSize = if (available) 28.sp else 37.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
            )
        }
        Canvas(Modifier.offset(350.dp, 17.dp).size(76.dp, 38.dp)) {
            val c = if (illuminated) Cyan.copy(alpha = .78f) else TextMuted.copy(alpha = .25f)
            var x = 0f
            repeat(4) {
                drawLine(
                    c,
                    start = androidx.compose.ui.geometry.Offset(x + 8f, size.height),
                    end = androidx.compose.ui.geometry.Offset(x + 28f, 0f),
                    strokeWidth = 8f,
                )
                x += 18f
            }
        }
    }
}

@Composable
private fun BoxScope.FuelPanel(t: TelemetryState) {
    Panel(Modifier.offset(1047.dp, 126.dp).size(553.dp, 156.dp), radius = 17f) {
        Label("FUEL", 29, 29)
        FuelPumpIcon(Modifier.offset(29.dp, 66.dp).size(44.dp, 48.dp))
        Text("${t.fuelPercent}%", color = TextMain, fontFamily = DisplayFont, fontSize = 53.sp, fontWeight = FontWeight.Bold, modifier = Modifier.offset(95.dp, 58.dp).width(140.dp))
        HorizontalBar(value = t.fuelPercent / 100f, color = Cyan, x = 95, y = 108, width = 266, height = 22)
        Box(Modifier.offset(389.dp, 59.dp).size(1.dp, 74.dp).background(Color(0xFF28343E)))
        Text(if (t.estimatedFuelLapsAvailable > 0f) String.format(Locale.US, "%.1f", t.estimatedFuelLapsAvailable) else "—", color = TextMain, fontFamily = DisplayFont, fontSize = 46.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.offset(414.dp, 57.dp).width(100.dp))
        Text("LAPS", color = Color(0xFFA8B1BA), fontFamily = UiFont, fontSize = 23.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.offset(414.dp, 110.dp).width(100.dp))
    }
}

@Composable
private fun BoxScope.ErsPanel(t: TelemetryState) {
    Panel(Modifier.offset(1047.dp, 292.dp).size(553.dp, 139.dp), radius = 17f) {
        Label("ERS", 29, 27)
        Text("⚡", color = TextMain, fontSize = 42.sp, modifier = Modifier.offset(25.dp, 58.dp).size(42.dp, 52.dp), textAlign = TextAlign.Center)
        HorizontalBar(value = t.ersPercent / 100f, color = Yellow, x = 93, y = 77, width = 191, height = 22)
        Text("${t.ersPercent}%", color = TextMain, fontFamily = DisplayFont, fontSize = 34.sp, fontWeight = FontWeight.Bold, modifier = Modifier.offset(301.dp, 66.dp).width(85.dp))
        val mode = t.ersModeLabel
        if (mode != "NONE") {
            Box(Modifier.offset(392.dp, 63.dp).size(136.dp, 51.dp).border(3.dp, Yellow, RoundedCornerShape(7.dp)), contentAlignment = Alignment.Center) {
                Text(
                    mode,
                    color = Yellow,
                    fontFamily = UiFont,
                    fontSize = if (mode == "OVERTAKE") 18.sp else 23.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.width(126.dp),
                )
            }
        }
    }
}

@Composable
private fun BoxScope.TyresPanel(t: TelemetryState) {
    val compound = compoundColor(t.visualTyreCompound)
    Panel(Modifier.offset(1047.dp, 442.dp).size(553.dp, 311.dp), radius = 17f) {
        Label("TYRES", 29, 28)
        Box(Modifier.offset(29.dp, 75.dp).size(76.dp).border(9.dp, compound, CircleShape))
        Text(t.tyreCompoundLabel, color = TextMain, fontFamily = DisplayFont, fontSize = 34.sp, fontWeight = FontWeight.Bold, modifier = Modifier.offset(29.dp, 175.dp).width(180.dp))
        Text("${t.tyreAgeLaps} LAPS", color = Color(0xFFD0D5DA), fontFamily = UiFont, fontSize = 28.sp, modifier = Modifier.offset(29.dp, 219.dp).width(180.dp))
        Text(if (t.tyreLifeSpanLaps > 0) "(${t.tyreLifeSpanLaps} LAPS)" else "(— LAPS)", color = Color(0xFF9CA6B0), fontFamily = UiFont, fontSize = 21.sp, modifier = Modifier.offset(29.dp, 255.dp).width(180.dp))
        Box(Modifier.offset(218.dp, 43.dp).size(1.dp, 244.dp).background(Color(0xFF26323C)))
        TyreReadout("FL", t.innerTyreTemps.getOrElse(2) { 0 }, t.tyreWear.getOrElse(2) { 0f }, compound, 250, 65)
        TyreReadout("FR", t.innerTyreTemps.getOrElse(3) { 0 }, t.tyreWear.getOrElse(3) { 0f }, compound, 462, 65)
        TyreReadout("RL", t.innerTyreTemps.getOrElse(0) { 0 }, t.tyreWear.getOrElse(0) { 0f }, compound, 250, 203)
        TyreReadout("RR", t.innerTyreTemps.getOrElse(1) { 0 }, t.tyreWear.getOrElse(1) { 0f }, compound, 462, 203)
        CarOutline(Modifier.offset(323.dp, 43.dp).size(121.dp, 246.dp))
    }
}

@Composable
private fun BoxScope.TyreReadout(label: String, temp: Int, wear: Float, compound: Color, x: Int, y: Int) {
    Text(label, color = Color(0xFFB3BCC5), fontFamily = UiFont, fontSize = 22.sp, modifier = Modifier.offset(x.dp, y.dp).width(65.dp))
    Text(if (temp > 0) "$temp°" else "—", color = TextMain, fontFamily = DisplayFont, fontSize = 29.sp, fontWeight = FontWeight.Bold, modifier = Modifier.offset(x.dp, (y + 27).dp).width(72.dp))
    Text("${wear.roundToInt()}%", color = compound, fontFamily = UiFont, fontSize = 25.sp, fontWeight = FontWeight.Black, modifier = Modifier.offset(x.dp, (y + 68).dp).width(72.dp))
}

@Composable
private fun CarOutline(modifier: Modifier) {
    // Crash-isolation build: keep the approved TYRES geometry but avoid
    // decoding the raster car asset until we confirm it is safe on-device.
    Canvas(modifier) {
        val center = size.width / 2f
        val body = Color(0xFFCBD3DA)
        val secondary = Color(0xFF84909C)
        drawRoundRect(
            color = body,
            topLeft = androidx.compose.ui.geometry.Offset(center - size.width * .12f, size.height * .08f),
            size = androidx.compose.ui.geometry.Size(size.width * .24f, size.height * .78f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.width * .08f, size.width * .08f),
        )
        drawRect(
            color = secondary,
            topLeft = androidx.compose.ui.geometry.Offset(center - size.width * .34f, size.height * .28f),
            size = androidx.compose.ui.geometry.Size(size.width * .68f, size.height * .12f),
        )
        drawRect(
            color = secondary,
            topLeft = androidx.compose.ui.geometry.Offset(center - size.width * .28f, size.height * .64f),
            size = androidx.compose.ui.geometry.Size(size.width * .56f, size.height * .12f),
        )
        drawRect(
            color = body,
            topLeft = androidx.compose.ui.geometry.Offset(center - size.width * .22f, size.height * .03f),
            size = androidx.compose.ui.geometry.Size(size.width * .44f, size.height * .08f),
        )
        drawRect(
            color = body,
            topLeft = androidx.compose.ui.geometry.Offset(center - size.width * .18f, size.height * .86f),
            size = androidx.compose.ui.geometry.Size(size.width * .36f, size.height * .08f),
        )
    }
}

@Composable
private fun BoxScope.BottomTiming(t: TelemetryState, state: RaceVisualState) {
    Panel(Modifier.offset(0.dp, 766.dp).size(1013.dp, 134.dp), radius = 17f) {
        TimingCell("CURRENT LAP", formatLapTime(t.currentLapTimeMs), 0, 225, current = true)
        TimingCell("LAST LAP", formatLapTime(t.lastLapTimeMs), 225, 178)
        TimingCell("BEST LAP", formatLapTime(t.bestLapTimeMs), 403, 194)
        SectorCell("S1", t.sector1TimeMs, t.bestSector1Ms, 597, 139)
        SectorCell("S2", t.sector2TimeMs, t.bestSector2Ms, 736, 139)
        SectorCell("S3", t.currentSector3TimeMs, t.bestSector3Ms, 875, 138)
    }
    Panel(Modifier.offset(1024.dp, 766.dp).size(284.dp, 134.dp), radius = 17f) {
        Label("SPEED TRAP", 37, 28)
        Text(if (t.speedTrapFastestKph > 0f) "${t.speedTrapFastestKph.roundToInt()} KM/H" else "—", color = TextMain, fontFamily = DisplayFont, fontSize = 37.sp, fontWeight = FontWeight.Bold, modifier = Modifier.offset(37.dp, 72.dp).width(220.dp))
    }
    val penaltyAccent = when {
        state.state == RaceUiState.PENALTY -> state.accent
        t.penaltiesSeconds > 0 -> Red
        else -> PanelLine
    }
    Panel(Modifier.offset(1319.dp, 766.dp).size(281.dp, 134.dp), radius = 17f, borderColor = penaltyAccent, borderWidth = if (state.state == RaceUiState.PENALTY) 2f else 1f) {
        Label("PENALTIES", 23, 20)
        Text("+${t.penaltiesSeconds}s", color = if (t.penaltiesSeconds > 0) Red else TextMain, fontFamily = DisplayFont, fontSize = 27.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.offset(180.dp, 17.dp).width(75.dp))
        SmallPenalty("WARNINGS", t.totalWarnings.toString(), 56)
        SmallPenalty("CORNER CUTTING", t.cornerCuttingWarnings.toString(), 89)
    }
}

@Composable
private fun BoxScope.TimingCell(label: String, value: String, x: Int, width: Int, current: Boolean = false) {
    if (x > 0) Box(Modifier.offset(x.dp, 23.dp).size(1.dp, 93.dp).background(Color(0xFF293640)))
    Text(label, color = Color(0xFF9FA9B3), fontFamily = UiFont, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic, modifier = Modifier.offset((x + 22).dp, 23.dp).width((width - 35).dp))
    Text(value, color = TextMain, fontFamily = DisplayFont, fontSize = if (current) 47.sp else 39.sp, fontWeight = if (current) FontWeight.Bold else FontWeight.Normal, fontStyle = if (current) FontStyle.Italic else FontStyle.Normal, modifier = Modifier.offset((x + 22).dp, 58.dp).width((width - 30).dp))
}

@Composable
private fun BoxScope.SectorCell(label: String, value: Long, best: Long, x: Int, width: Int) {
    Box(Modifier.offset(x.dp, 23.dp).size(1.dp, 93.dp).background(Color(0xFF293640)))
    val delta = if (value > 0 && best > 0) value - best else 0L
    Text(label, color = Color(0xFF9FA9B3), fontFamily = UiFont, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic, modifier = Modifier.offset((x + 23).dp, 23.dp).width((width - 30).dp))
    Text(formatSectorTime(value), color = TextMain, fontFamily = DisplayFont, fontSize = 31.sp, fontWeight = FontWeight.Normal, modifier = Modifier.offset((x + 23).dp, 54.dp).width((width - 25).dp))
    if (delta != 0L) Text(String.format(Locale.US, "%+.3f", delta / 1000f), color = if (delta <= 0) Green else Red, fontFamily = UiFont, fontSize = 20.sp, fontWeight = FontWeight.Black, modifier = Modifier.offset((x + 23).dp, 94.dp).width((width - 25).dp))
}

@Composable
private fun BoxScope.SmallPenalty(label: String, value: String, y: Int) {
    Text(label, color = Color(0xFFA9B2BB), fontFamily = UiFont, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.offset(23.dp, y.dp).width(170.dp))
    Text(value, color = TextMain, fontFamily = DisplayFont, fontSize = 25.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.offset(205.dp, (y - 4).dp).width(50.dp))
}

@Composable
private fun FuelPumpIcon(modifier: Modifier) {
    Canvas(modifier) {
        val c = TextMain
        drawRoundRect(
            color = c,
            topLeft = androidx.compose.ui.geometry.Offset(size.width * .10f, size.height * .08f),
            size = androidx.compose.ui.geometry.Size(size.width * .56f, size.height * .72f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.width * .06f, size.width * .06f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = size.width * .09f),
        )
        drawLine(
            color = c,
            start = androidx.compose.ui.geometry.Offset(size.width * .18f, size.height * .28f),
            end = androidx.compose.ui.geometry.Offset(size.width * .58f, size.height * .28f),
            strokeWidth = size.width * .08f,
        )
        drawLine(
            color = c,
            start = androidx.compose.ui.geometry.Offset(size.width * .66f, size.height * .20f),
            end = androidx.compose.ui.geometry.Offset(size.width * .83f, size.height * .35f),
            strokeWidth = size.width * .08f,
        )
        drawLine(
            color = c,
            start = androidx.compose.ui.geometry.Offset(size.width * .83f, size.height * .35f),
            end = androidx.compose.ui.geometry.Offset(size.width * .83f, size.height * .67f),
            strokeWidth = size.width * .08f,
        )
        drawLine(
            color = c,
            start = androidx.compose.ui.geometry.Offset(size.width * .83f, size.height * .67f),
            end = androidx.compose.ui.geometry.Offset(size.width * .68f, size.height * .67f),
            strokeWidth = size.width * .08f,
        )
    }
}

@Composable
private fun BoxScope.HorizontalBar(value: Float, color: Color, x: Int, y: Int, width: Int, height: Int) {
    Box(Modifier.offset(x.dp, y.dp).size(width.dp, height.dp).background(Color(0xFF202C36), RoundedCornerShape(5.dp))) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(value.coerceIn(0f, 1f)).background(color, RoundedCornerShape(5.dp)))
    }
}

@Composable
private fun BoxScope.Label(text: String, x: Int, y: Int) {
    Text(text, color = TextSoft, fontFamily = UiFont, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic, modifier = Modifier.offset(x.dp, y.dp))
}

@Composable
private fun Panel(
    modifier: Modifier,
    radius: Float,
    borderColor: Color = PanelLine,
    borderWidth: Float = 1f,
    background: Brush = Brush.linearGradient(listOf(PanelTop.copy(alpha = .96f), PanelBottom.copy(alpha = .985f))),
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier.clip(RoundedCornerShape(radius.dp)).background(background).border(borderWidth.dp, borderColor, RoundedCornerShape(radius.dp)), content = content)
}

private fun formatGear(gear: Int) = when { gear < 0 -> "R"; gear == 0 -> "N"; else -> gear.toString() }
private fun formatGap(ms: Long) = if (ms <= 0) "—" else String.format(Locale.US, "+%.3f", ms / 1000f)
private fun formatLapTime(ms: Long): String { if (ms <= 0) return "—"; val m = ms / 60000; val s = (ms % 60000) / 1000; val mm = ms % 1000; return String.format(Locale.US, "%d:%02d.%03d", m, s, mm) }
private fun formatSectorTime(ms: Long) = if (ms <= 0) "—" else String.format(Locale.US, "%.3f", ms / 1000f)
private fun sessionLabel(type: Int) = when (type) { 10 -> "RACE"; 11 -> "RACE 2"; 12 -> "RACE 3"; 5 -> "Q1"; 6 -> "Q2"; 7 -> "Q3"; else -> "SESSION" }
private fun compoundColor(v: Int) = when (v) { 16 -> Red; 17 -> Yellow; 18 -> TextMain; 7 -> Green; 8 -> Blue; else -> TextMuted }

private fun teamLogoRes(teamName: String): Int? {
    val team = teamName.lowercase(Locale.getDefault())
    return when {
        "alpine" in team -> R.drawable.team_alpine
        "aston" in team -> R.drawable.team_aston_martin
        "audi" in team || "sauber" in team -> R.drawable.team_audi
        "cadillac" in team -> R.drawable.team_cadillac
        "ferrari" in team -> R.drawable.team_ferrari
        "haas" in team -> R.drawable.team_haas
        "mclaren" in team -> R.drawable.team_mclaren
        "mercedes" in team -> R.drawable.team_mercedes
        "racing bulls" in team || "rb" == team.trim() -> R.drawable.team_racing_bulls
        "red bull" in team -> R.drawable.team_red_bull
        "williams" in team -> R.drawable.team_williams
        else -> null
    }
}
