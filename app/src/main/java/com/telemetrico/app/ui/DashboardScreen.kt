package com.telemetrico.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telemetrico.app.model.ConnectionState
import com.telemetrico.app.model.TelemetryState
import com.telemetrico.app.theme.*
import java.util.Locale
import kotlin.math.roundToInt

private val Panel = Color(0xFF0B0D12)
private val Panel2 = Color(0xFF101319)
private val Line = Color(0xFF343944)
private val SoftRed = Color(0xFFFF3B30)

@Composable
fun DashboardScreen(telemetry: TelemetryState, connection: ConnectionState) {
    BoxWithConstraints(Modifier.fillMaxSize().background(F1CarbonBlack)) {
        val compact = maxWidth < 1050.dp || maxHeight < 650.dp
        val gap = if (compact) 7.dp else 10.dp
        val outer = if (compact) 10.dp else 14.dp

        Column(Modifier.fillMaxSize().padding(outer)) {
            Header(telemetry, compact)
            AlertBanner(telemetry, compact)
            Spacer(Modifier.height(gap))

            Row(Modifier.fillMaxWidth().weight(1f)) {
                PositionPanel(telemetry, compact, Modifier.weight(0.92f).fillMaxHeight())
                Spacer(Modifier.width(gap))
                DrivePanel(telemetry, compact, Modifier.weight(1.72f).fillMaxHeight())
                Spacer(Modifier.width(gap))
                StrategyColumn(telemetry, compact, Modifier.weight(1.28f).fillMaxHeight())
            }

            Spacer(Modifier.height(gap))
            BottomStrip(telemetry, compact)
        }
    }
}

@Composable
private fun Header(t: TelemetryState, compact: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(if (compact) 58.dp else 70.dp)
            .background(Panel, RoundedCornerShape(14.dp))
            .border(1.dp, Line, RoundedCornerShape(14.dp))
            .padding(horizontal = if (compact) 14.dp else 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(5.dp).height(34.dp).background(F1WarmRed))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(t.driverName.uppercase(Locale.getDefault()), color = F1HighVisWhite, fontSize = if (compact) 16.sp else 20.sp, fontWeight = FontWeight.Black)
            Text(t.teamName.uppercase(Locale.getDefault()), color = F1Carbon50, fontSize = if (compact) 10.sp else 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text(if (t.trackName != "TRACK") t.trackName else "F1 25", color = F1OffWhite, fontSize = if (compact) 10.sp else 12.sp, fontWeight = FontWeight.Black)
            Text(sessionLabel(t.sessionType), color = F1Carbon50, fontSize = if (compact) 8.sp else 10.sp)
        }
        Spacer(Modifier.width(if (compact) 16.dp else 24.dp))
        Box(Modifier.width(1.dp).height(34.dp).background(Line))
        Spacer(Modifier.width(if (compact) 14.dp else 18.dp))
        Text(t.weatherSymbol, color = WarningYellow, fontSize = if (compact) 20.sp else 24.sp)
        Spacer(Modifier.width(8.dp))
        Column {
            Text("${t.airTemperatureC}°C", color = F1HighVisWhite, fontSize = if (compact) 14.sp else 17.sp, fontWeight = FontWeight.Black)
            Text("TRACK ${t.trackTemperatureC}°C", color = F1Carbon50, fontSize = if (compact) 8.sp else 10.sp)
        }
    }
}

@Composable
private fun AlertBanner(t: TelemetryState, compact: Boolean) {
    val alert = when {
        t.unservedDriveThrough > 0 -> "DRIVE THROUGH" to SoftRed
        t.unservedStopGo > 0 -> "STOP & GO" to SoftRed
        t.pitLimiterActive -> "PIT LIMITER" to SoftRed
        t.safetyCarStatus == 2 -> "VSC · VIRTUAL SAFETY CAR" to WarningYellow
        t.safetyCarStatus == 1 -> "SAFETY CAR" to WarningYellow
        t.fiaFlag == 3 -> "YELLOW FLAG" to WarningYellow
        t.fiaFlag == 2 -> "BLUE FLAG" to DrsCyan
        t.currentLapInvalid -> "INVALID LAP" to SoftRed
        else -> null
    }
    if (alert != null) {
        Spacer(Modifier.height(if (compact) 5.dp else 7.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(if (compact) 30.dp else 38.dp)
                .background(alert.second, RoundedCornerShape(7.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(alert.first, color = F1CarbonBlack, fontSize = if (compact) 13.sp else 17.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun PositionPanel(t: TelemetryState, compact: Boolean, modifier: Modifier) {
    PanelBox(modifier) {
        MetricLabel("POSITION")
        Text(if (t.position > 0) "P${t.position}" else "—", color = F1HighVisWhite, fontSize = if (compact) 52.sp else 70.sp, lineHeight = if (compact) 54.sp else 72.sp, fontWeight = FontWeight.Black)
        DividerRed()
        MetricLabel("LAP")
        Text(if (t.currentLap > 0 && t.totalLaps > 0) "${t.currentLap} / ${t.totalLaps}" else "—", color = F1HighVisWhite, fontSize = if (compact) 23.sp else 29.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(11.dp))
        MetricLabel("GAP AHEAD")
        BigSmall(formatGap(t.gapAheadMs), compact)
        Spacer(Modifier.height(10.dp))
        MetricLabel("GAP LEADER")
        BigSmall(formatGap(t.gapLeaderMs), compact)
    }
}

@Composable
private fun DrivePanel(t: TelemetryState, compact: Boolean, modifier: Modifier) {
    PanelBox(modifier) {
        RevStrip(t, compact)
        Spacer(Modifier.height(if (compact) 8.dp else 11.dp))
        Row(Modifier.fillMaxWidth().weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Pedal("BRAKE", t.brake, SoftRed, compact, Modifier.width(if (compact) 48.dp else 58.dp).fillMaxHeight())
            Spacer(Modifier.width(if (compact) 8.dp else 13.dp))
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                MetricLabel("GEAR")
                Text(formatGear(t.gear), color = F1HighVisWhite, fontSize = if (compact) 74.sp else 98.sp, lineHeight = if (compact) 74.sp else 98.sp, fontWeight = FontWeight.Black)
                Text(t.speedKph.toString(), color = F1HighVisWhite, fontSize = if (compact) 46.sp else 62.sp, lineHeight = if (compact) 48.sp else 64.sp, fontWeight = FontWeight.Black)
                Text("KM/H", color = F1OffWhite, fontSize = if (compact) 11.sp else 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(if (compact) 7.dp else 10.dp))
                DrsModule(t, compact)
            }
            Spacer(Modifier.width(if (compact) 8.dp else 13.dp))
            Pedal("THROTTLE", t.throttle, TelemetryGreen, compact, Modifier.width(if (compact) 48.dp else 58.dp).fillMaxHeight())
        }
    }
}

@Composable
private fun RevStrip(t: TelemetryState, compact: Boolean) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.width(if (compact) 54.dp else 66.dp)) {
            MetricLabel("RPM ×1000")
            Text(String.format(Locale.US, "%.1f", t.engineRpm / 1000f), color = F1OffWhite, fontSize = if (compact) 12.sp else 14.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.width(7.dp))
        val fallback = ((t.revLightsPercent.coerceIn(0, 100) / 100f) * 15f).roundToInt().coerceIn(0, 15)
        val effectiveBits = if (t.revLightsBits != 0 || t.revLightsPercent == 0) t.revLightsBits else if (fallback == 0) 0 else (1 shl fallback) - 1
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
            repeat(15) { i ->
                val active = effectiveBits and (1 shl i) != 0
                val color = when { i < 5 -> TelemetryGreen; i < 10 -> WarningYellow; else -> SoftRed }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(if (compact) 13.dp else 16.dp).background(if (active) color else Color(0xFF20242C), RoundedCornerShape(50)).border(1.dp, if (active) color else F1Carbon70, RoundedCornerShape(50)))
                    if (!compact) Text("${i + 1}", color = F1Carbon50, fontSize = 7.sp)
                }
            }
        }
    }
}

@Composable
private fun Pedal(label: String, value: Float, color: Color, compact: Boolean, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = F1OffWhite, fontSize = if (compact) 9.sp else 11.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(5.dp))
        Box(Modifier.width(if (compact) 20.dp else 24.dp).weight(1f).border(1.dp, Line, RoundedCornerShape(4.dp)).padding(2.dp), contentAlignment = Alignment.BottomCenter) {
            Box(Modifier.fillMaxWidth().fillMaxHeight(value.coerceIn(0f, 1f)).background(color, RoundedCornerShape(3.dp)))
        }
        Spacer(Modifier.height(4.dp))
        Text("${(value.coerceIn(0f,1f) * 100).roundToInt()}%", color = F1HighVisWhite, fontSize = if (compact) 10.sp else 12.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun DrsModule(t: TelemetryState, compact: Boolean) {
    val active = t.drsActive
    val available = t.drsAllowed && !active
    val border = if (active || available) DrsCyan else Line
    val bg = if (active) DrsCyan.copy(alpha = .15f) else Panel2
    Row(Modifier.widthIn(min = if (compact) 145.dp else 190.dp).background(bg, RoundedCornerShape(6.dp)).border(if (active) 2.dp else 1.dp, border, RoundedCornerShape(6.dp)).padding(horizontal = 12.dp, vertical = if (compact) 7.dp else 9.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Text("DRS", color = F1HighVisWhite, fontSize = if (compact) 12.sp else 14.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.width(14.dp))
        Text(when { active -> "ACTIVE"; available -> "AVAILABLE"; t.drsActivationDistanceM > 0 -> "${t.drsActivationDistanceM} M"; else -> "—" }, color = if (active || available) DrsCyan else F1Carbon50, fontSize = if (compact) 14.sp else 18.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun StrategyColumn(t: TelemetryState, compact: Boolean, modifier: Modifier) {
    Column(modifier) {
        EnergyPanel(t, compact, Modifier.fillMaxWidth().weight(.72f))
        Spacer(Modifier.height(7.dp))
        TyrePanel(t, compact, Modifier.fillMaxWidth().weight(1.28f))
    }
}

@Composable
private fun EnergyPanel(t: TelemetryState, compact: Boolean, modifier: Modifier) {
    PanelBox(modifier) {
        MetricLabel("FUEL")
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Text("${t.fuelPercent}%", color = F1HighVisWhite, fontSize = if (compact) 22.sp else 28.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.weight(1f))
            Text(if (t.estimatedFuelLapsAvailable > 0f) String.format(Locale.US, "%.1f", t.estimatedFuelLapsAvailable) else "—", color = F1HighVisWhite, fontSize = if (compact) 21.sp else 27.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.width(4.dp)); Text("LAPS", color = F1Carbon50, fontSize = 8.sp)
        }
        ValueBar(t.fuelPercent, TelemetryGreen)
        Row(Modifier.fillMaxWidth()) {
            Text(if (t.fuelInTank > 0f) String.format(Locale.US, "%.1f KG", t.fuelInTank) else "—", color = F1Carbon50, fontSize = 9.sp)
            Spacer(Modifier.weight(1f))
            Text(String.format(Locale.US, "MFD %+.1f", t.fuelRemainingLaps), color = if (t.fuelRemainingLaps >= 0) TelemetryGreen else SoftRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(if (compact) 8.dp else 11.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            MetricLabel("ERS")
            Spacer(Modifier.weight(1f))
            Text("${t.ersPercent}%", color = F1HighVisWhite, fontSize = if (compact) 14.sp else 17.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.width(8.dp))
            if (t.ersModeLabel != "NONE") Badge(t.ersModeLabel, WarningYellow)
        }
        ValueBar(t.ersPercent, WarningYellow)
    }
}

@Composable
private fun TyrePanel(t: TelemetryState, compact: Boolean, modifier: Modifier) {
    val compound = compoundColor(t.visualTyreCompound)
    PanelBox(modifier) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column { MetricLabel("TYRES"); Text(t.tyreCompoundLabel, color = compound, fontSize = if (compact) 15.sp else 18.sp, fontWeight = FontWeight.Black) }
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Text("${t.tyreAgeLaps} LAPS", color = F1HighVisWhite, fontSize = if (compact) 12.sp else 14.sp, fontWeight = FontWeight.Black)
                Text(if (t.tyreLifeSpanLaps > 0) "LIFE ${t.tyreLifeSpanLaps}" else "LIFE —", color = F1Carbon50, fontSize = 8.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth().weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.SpaceAround) {
                TyreReadout("FL", t.innerTyreTemps.getOrElse(2){0}, t.tyreWear.getOrElse(2){0f}, compound, compact)
                TyreReadout("RL", t.innerTyreTemps.getOrElse(0){0}, t.tyreWear.getOrElse(0){0f}, compound, compact)
            }
            TyreCarGraphic(Modifier.width(if (compact) 58.dp else 72.dp).fillMaxHeight(.82f), compound)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.SpaceAround, horizontalAlignment = Alignment.End) {
                TyreReadout("FR", t.innerTyreTemps.getOrElse(3){0}, t.tyreWear.getOrElse(3){0f}, compound, compact, true)
                TyreReadout("RR", t.innerTyreTemps.getOrElse(1){0}, t.tyreWear.getOrElse(1){0f}, compound, compact, true)
            }
        }
    }
}

@Composable
private fun TyreReadout(label: String, temp: Int, wear: Float, color: Color, compact: Boolean, right: Boolean = false) {
    Column(horizontalAlignment = if (right) Alignment.End else Alignment.Start) {
        Text("$label  ${if (temp > 0) "$temp°" else "—"}", color = F1HighVisWhite, fontSize = if (compact) 10.sp else 12.sp, fontWeight = FontWeight.Black)
        Text("${wear.roundToInt()}%", color = color, fontSize = if (compact) 10.sp else 12.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun TyreCarGraphic(modifier: Modifier, color: Color) {
    Canvas(modifier) {
        val cx = size.width / 2f
        val bodyW = size.width * .38f
        drawRoundRect(Color(0xFF69717F), topLeft = Offset(cx - bodyW/2, size.height*.08f), size = Size(bodyW, size.height*.84f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(9f,9f), style = Stroke(width = 2f))
        val wheelW = size.width*.18f; val wheelH = size.height*.20f
        listOf(.14f to .22f, .14f to .64f, .68f to .22f, .68f to .64f).forEach { (x,y) ->
            drawRoundRect(color.copy(alpha=.9f), topLeft=Offset(size.width*x, size.height*y), size=Size(wheelW,wheelH), cornerRadius=androidx.compose.ui.geometry.CornerRadius(4f,4f))
        }
        drawLine(Color(0xFF69717F), Offset(cx, size.height*.02f), Offset(cx,size.height*.98f), strokeWidth=2f)
    }
}

@Composable
private fun BottomStrip(t: TelemetryState, compact: Boolean) {
    Row(Modifier.fillMaxWidth().height(if (compact) 76.dp else 92.dp)) {
        PanelBox(Modifier.weight(2.55f).fillMaxHeight(), padding = if (compact) 9.dp else 12.dp) {
            Row(Modifier.fillMaxSize()) {
                TimeCell("CURRENT LAP", formatLapTime(t.currentLapTimeMs), Modifier.weight(1.2f), compact)
                TimeCell("LAST LAP", formatLapTime(t.lastLapTimeMs), Modifier.weight(1f), compact)
                TimeCell("BEST LAP", formatLapTime(t.bestLapTimeMs), Modifier.weight(1f), compact)
                SectorCell("S1", t.sector1TimeMs, t.bestSector1Ms, Modifier.weight(.82f), compact)
                SectorCell("S2", t.sector2TimeMs, t.bestSector2Ms, Modifier.weight(.82f), compact)
                SectorCell("S3", t.currentSector3TimeMs, t.bestSector3Ms, Modifier.weight(.82f), compact)
            }
        }
        Spacer(Modifier.width(7.dp))
        PanelBox(Modifier.weight(.72f).fillMaxHeight(), padding = if (compact) 9.dp else 12.dp) {
            MetricLabel("SPEED TRAP")
            Spacer(Modifier.weight(1f))
            Text(if (t.speedTrapFastestKph > 0f) "${t.speedTrapFastestKph.roundToInt()} KM/H" else "—", color = F1HighVisWhite, fontSize = if (compact) 17.sp else 22.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.width(7.dp))
        PanelBox(Modifier.weight(.72f).fillMaxHeight(), padding = if (compact) 8.dp else 11.dp) {
            Row(Modifier.fillMaxWidth()) { MetricLabel("PENALTIES"); Spacer(Modifier.weight(1f)); Text("+${t.penaltiesSeconds}s", color = if (t.penaltiesSeconds>0) WarningYellow else F1HighVisWhite, fontSize = if (compact) 12.sp else 15.sp, fontWeight = FontWeight.Black) }
            Spacer(Modifier.height(3.dp)); SmallRow("WARNINGS", t.totalWarnings.toString()); SmallRow("CORNER CUT", t.cornerCuttingWarnings.toString())
        }
    }
}

@Composable
private fun SectorCell(label: String, current: Long, best: Long, modifier: Modifier, compact: Boolean) {
    val delta = if (current > 0 && best > 0) current - best else 0L
    Column(modifier.padding(horizontal = 6.dp)) {
        MetricLabel(label)
        Text(formatSectorTime(current), color = F1HighVisWhite, fontSize = if (compact) 12.sp else 15.sp, fontWeight = FontWeight.Black)
        if (delta != 0L) Text(String.format(Locale.US, "%+.3f", delta / 1000f), color = if (delta <= 0) TelemetryGreen else SoftRed, fontSize = if (compact) 8.sp else 10.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun TimeCell(label: String, value: String, modifier: Modifier, compact: Boolean) {
    Column(modifier.padding(horizontal = 6.dp)) { MetricLabel(label); Spacer(Modifier.height(4.dp)); Text(value, color = F1HighVisWhite, fontSize = if (compact) 15.sp else 20.sp, fontWeight = FontWeight.Black) }
}

@Composable
private fun PanelBox(modifier: Modifier, padding: androidx.compose.ui.unit.Dp = 12.dp, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.background(Panel, RoundedCornerShape(12.dp)).border(1.dp, Line, RoundedCornerShape(12.dp)).padding(padding), content = content)
}

@Composable private fun MetricLabel(text: String) = Text(text, color = F1Carbon50, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = .6.sp)
@Composable private fun DividerRed() { Spacer(Modifier.height(4.dp)); Box(Modifier.width(35.dp).height(3.dp).background(F1WarmRed)); Spacer(Modifier.height(5.dp)) }
@Composable private fun BigSmall(value: String, compact: Boolean) = Text(value, color = F1HighVisWhite, fontSize = if (compact) 17.sp else 21.sp, fontWeight = FontWeight.Black)
@Composable private fun SmallRow(label:String,value:String){ Row(Modifier.fillMaxWidth()){ Text(label,color=F1Carbon50,fontSize=8.sp); Spacer(Modifier.weight(1f)); Text(value,color=F1OffWhite,fontSize=9.sp,fontWeight=FontWeight.Black) } }
@Composable private fun Badge(text:String,color:Color){ Box(Modifier.border(1.dp,color,RoundedCornerShape(4.dp)).padding(horizontal=7.dp,vertical=3.dp)){Text(text,color=color,fontSize=8.sp,fontWeight=FontWeight.Black)} }

@Composable
private fun ValueBar(value: Int, color: Color) {
    Spacer(Modifier.height(4.dp))
    Box(Modifier.fillMaxWidth().height(7.dp).background(Color(0xFF252A32), RoundedCornerShape(50))) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(value.coerceIn(0,100)/100f).background(color, RoundedCornerShape(50)))
    }
    Spacer(Modifier.height(4.dp))
}

private fun formatGear(gear:Int)=when{gear<0->"R";gear==0->"N";else->gear.toString()}
private fun formatGap(ms:Long)=if(ms<=0) "—" else String.format(Locale.US,"+%.3f",ms/1000f)
private fun formatLapTime(ms:Long):String{ if(ms<=0) return "—"; val m=ms/60000; val s=(ms%60000)/1000; val mm=ms%1000; return String.format(Locale.US,"%d:%02d.%03d",m,s,mm) }
private fun formatSectorTime(ms:Long)=if(ms<=0) "—" else String.format(Locale.US,"%.3f",ms/1000f)
private fun sessionLabel(type:Int)=when(type){10->"RACE";11->"RACE 2";12->"RACE 3";5->"Q1";6->"Q2";7->"Q3";else->"SESSION"}
private fun compoundColor(v:Int)=when(v){16->SoftRed;17->WarningYellow;18->F1HighVisWhite;7->TelemetryGreen;8->Color(0xFF4D8DFF);else->F1Carbon50}
