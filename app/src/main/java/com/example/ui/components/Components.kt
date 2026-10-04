package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.util.PersianDateUtil
import com.example.ui.theme.*
import kotlin.math.*

@Composable
fun PersianRtlProvider(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    borderColor: Color = SignalCyan.copy(alpha = 0.25f),
    backgroundColor: Color = SpaceNavySurface.copy(alpha = 0.85f),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .border(1.dp, borderColor, RoundedCornerShape(cornerRadius))
            .shadow(6.dp, RoundedCornerShape(cornerRadius)),
        color = backgroundColor,
        shape = RoundedCornerShape(cornerRadius)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun MetricBadge(
    label: String,
    value: String,
    unit: String = "",
    color: Color = SignalCyan,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = PersianDateUtil.toPersianDigits(value),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            if (unit.isNotEmpty()) {
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = unit,
                    fontSize = 11.sp,
                    color = color.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun SignalLevelBar(
    title: String,
    percentage: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedPercent by animateFloatAsState(targetValue = percentage.coerceIn(0, 100) / 100f, label = "level")

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${PersianDateUtil.toPersianDigits(percentage.toString())}٪",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(SpaceNavyDark)
                .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(7.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedPercent)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(7.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(color.copy(alpha = 0.6f), color)
                        )
                    )
            )
        }
    }
}

@Composable
fun InteractiveCompassCanvas(
    currentAzimuth: Float,
    targetAzimuth: Float,
    isTargetAligned: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(240.dp)
            .clip(CircleShape)
            .background(SpaceNavyDark)
            .border(2.dp, if (isTargetAligned) SignalGreen else SignalCyan.copy(alpha = 0.4f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2

            // Outer dial ring
            drawCircle(
                color = SpaceNavySurfaceVariant,
                radius = radius,
                center = center,
                style = Stroke(width = 4f)
            )

            // Compass ticks (every 30 degrees)
            for (i in 0 until 12) {
                val tickAngle = Math.toRadians((i * 30.0) - currentAzimuth)
                val isCardinal = i % 3 == 0
                val tickLength = if (isCardinal) 18f else 10f
                val start = Offset(
                    center.x + (radius - tickLength) * sin(tickAngle).toFloat(),
                    center.y - (radius - tickLength) * cos(tickAngle).toFloat()
                )
                val end = Offset(
                    center.x + radius * sin(tickAngle).toFloat(),
                    center.y - radius * cos(tickAngle).toFloat()
                )
                drawLine(
                    color = if (isCardinal) SignalCyan else Color.Gray.copy(alpha = 0.5f),
                    start = start,
                    end = end,
                    strokeWidth = if (isCardinal) 4f else 2f
                )
            }

            // Target Satellite Azimuth Indicator Line (Cyan/Green neon)
            val targetAngleRad = Math.toRadians(targetAzimuth.toDouble() - currentAzimuth)
            val targetTip = Offset(
                center.x + (radius - 12f) * sin(targetAngleRad).toFloat(),
                center.y - (radius - 12f) * cos(targetAngleRad).toFloat()
            )
            drawLine(
                color = if (isTargetAligned) SignalGreen else SignalCyan,
                start = center,
                end = targetTip,
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )
            drawCircle(
                color = if (isTargetAligned) SignalGreen else SignalCyan,
                radius = 12f,
                center = targetTip
            )

            // Current phone orientation pointer (Forward arrow)
            val forwardTip = Offset(center.x, center.y - (radius - 20f))
            drawLine(
                color = GoldLock,
                start = center,
                end = forwardTip,
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )

            // Center hub
            drawCircle(color = SpaceNavy, radius = 22f, center = center)
            drawCircle(
                color = if (isTargetAligned) SignalGreen else GoldLock,
                radius = 8f,
                center = center
            )
        }

        // Center alignment label
        if (isTargetAligned) {
            Text(
                text = "✓ هم‌راستا",
                color = SignalGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 40.dp)
            )
        }
    }
}

@Composable
fun DigitalBubbleLevel(
    pitchDeg: Float,
    rollDeg: Float,
    targetElevationDeg: Float,
    modifier: Modifier = Modifier
) {
    val isLevel = abs(pitchDeg - targetElevationDeg) < 2.0f && abs(rollDeg) < 2.0f

    Box(
        modifier = modifier
            .size(160.dp)
            .clip(CircleShape)
            .background(SpaceNavyDark)
            .border(2.dp, if (isLevel) SignalGreen else Color.Gray.copy(alpha = 0.4f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val maxOffset = (size.minDimension / 2) - 24f

            // Crosshair
            drawLine(
                color = Color.DarkGray,
                start = Offset(center.x, 16f),
                end = Offset(center.x, size.height - 16f),
                strokeWidth = 2f
            )
            drawLine(
                color = Color.DarkGray,
                start = Offset(16f, center.y),
                end = Offset(size.width - 16f, center.y),
                strokeWidth = 2f
            )

            // Target ring
            drawCircle(
                color = if (isLevel) SignalGreen.copy(alpha = 0.5f) else SignalCyan.copy(alpha = 0.3f),
                radius = 24f,
                center = center,
                style = Stroke(width = 3f)
            )

            // Bubble position according to roll & pitch delta
            val pitchDelta = (pitchDeg - targetElevationDeg).coerceIn(-25f, 25f)
            val rollDelta = rollDeg.coerceIn(-25f, 25f)
            val bubbleX = center.x + (rollDelta / 25f) * maxOffset
            val bubbleY = center.y + (pitchDelta / 25f) * maxOffset

            drawCircle(
                color = if (isLevel) SignalGreen else GoldLock,
                radius = 16f,
                center = Offset(bubbleX, bubbleY)
            )
        }
    }
}
