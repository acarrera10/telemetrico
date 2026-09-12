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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telemetrico.app.theme.*
import kotlin.math.roundToInt

@Composable
fun TelemetricoWordmark(modifier: Modifier = Modifier, compact: Boolean = false) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .width(if (compact) 7.dp else 9.dp)
                .height(if (compact) 30.dp else 40.dp)
                .background(F1WarmRed)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = "TELEMÉTRICO",
                color = F1HighVisWhite,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                letterSpacing = (-0.7).sp,
                fontSize = if (compact) 22.sp else 30.sp,
            )
            if (!compact) {
                Text(
                    text = "RACE DATA. GREATER CONTROL.",
                    color = F1Carbon50,
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                )
            }
        }
    }
}

@Composable
fun TechnicalPanel(
    modifier: Modifier = Modifier,
    accent: Color = F1Carbon70,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .border(1.dp, F1Carbon70.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
            .background(Color(0xFF0E0F15).copy(alpha = 0.94f), RoundedCornerShape(18.dp))
            .padding(18.dp)
    ) {
        Box(Modifier.width(54.dp).height(4.dp).background(accent))
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
fun StatusRow(icon: String, label: String, value: String, valueColor: Color = F1OffWhite) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(icon, fontSize = 18.sp, modifier = Modifier.width(36.dp), color = F1HighVisWhite)
        Text(label, color = F1OffWhite, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Box(Modifier.weight(1f).height(1.dp).background(F1Carbon70.copy(alpha = 0.45f)))
        Spacer(Modifier.width(16.dp))
        Text(value, color = valueColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun RevLights(bits: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        for (i in 0 until 15) {
            val active = bits and (1 shl i) != 0
            val activeColor = when {
                i < 5 -> Color(0xFF22E15D)
                i < 10 -> WarningYellow
                else -> Color(0xFFFF2D2D)
            }
            Box(
                Modifier
                    .size(14.dp)
                    .background(if (active) activeColor else F1Carbon90, RoundedCornerShape(50))
                    .border(1.dp, if (active) activeColor else F1Carbon70, RoundedCornerShape(50))
            )
        }
    }
}

@Composable
fun VerticalPedalBar(
    label: String,
    value: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = F1Carbon50, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(7.dp))
        Box(
            Modifier
                .width(28.dp)
                .weight(1f)
                .border(1.dp, F1Carbon70, RoundedCornerShape(5.dp))
                .padding(3.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(value.coerceIn(0f, 1f))
                    .background(color, RoundedCornerShape(3.dp))
            )
        }
        Spacer(Modifier.height(6.dp))
        Text("${(value * 100).roundToInt()}%", color = F1HighVisWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ThinTrackLine(modifier: Modifier = Modifier, color: Color = F1WarmRed, thickness: Dp = 2.dp) {
    Canvas(modifier) {
        drawLine(
            color = color,
            start = Offset(0f, size.height),
            end = Offset(size.width * 0.72f, size.height),
            strokeWidth = thickness.toPx(),
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.72f, size.height),
            end = Offset(size.width * 0.79f, 0f),
            strokeWidth = thickness.toPx(),
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.79f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = thickness.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

@Composable
fun BigMetric(label: String, value: String, unit: String? = null, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = F1Carbon50, fontSize = 12.sp, letterSpacing = 1.sp)
        Text(value, color = F1HighVisWhite, fontSize = 62.sp, lineHeight = 62.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        if (unit != null) Text(unit, color = F1OffWhite, fontSize = 14.sp, letterSpacing = 1.sp)
    }
}
