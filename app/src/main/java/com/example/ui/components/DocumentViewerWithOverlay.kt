package com.example.ui.components

import android.net.Uri
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.AnomalySeverity
import com.example.data.model.LandRecordDocument
import com.example.data.model.TamperAnomaly
import com.example.ui.viewmodel.DocumentViewMode

@Composable
fun DocumentViewerWithOverlay(
    document: LandRecordDocument,
    viewMode: DocumentViewMode,
    selectedAnomaly: TamperAnomaly?,
    onAnomalySelected: (TamperAnomaly) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Pulsing animation for highlighted tamper bounding boxes
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Contrast filter for faded ink enhancement
    val colorFilter = when (viewMode) {
        DocumentViewMode.ENHANCED_CONTRAST -> {
            // High contrast binarization-style color matrix to reveal faded iron-gall ink
            val matrix = ColorMatrix(floatArrayOf(
                2.2f, 0f, 0f, 0f, -80f,
                0f, 2.2f, 0f, 0f, -80f,
                0f, 0f, 2.2f, 0f, -80f,
                0f, 0f, 0f, 1.0f, 0f
            ))
            ColorFilter.colorMatrix(matrix)
        }
        else -> null
    }

    // Resolve image model: either resource ID or Uri
    val imageModel = if (document.imagePathOrRes.startsWith("res:")) {
        val resId = document.imagePathOrRes.removePrefix("res:").toIntOrNull()
        resId ?: document.imagePathOrRes
    } else {
        Uri.parse(document.imagePathOrRes)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF212529))
            .border(1.5.dp, Color(0xFF374151), RoundedCornerShape(12.dp))
    ) {
        val containerWidth = maxWidth
        val containerHeight = maxHeight

        // Base Scanned Document Image
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageModel)
                .crossfade(true)
                .build(),
            contentDescription = "Scanned land record document",
            contentScale = ContentScale.Fit,
            colorFilter = colorFilter,
            modifier = Modifier
                .fillMaxSize()
                .align(androidx.compose.ui.Alignment.Center)
        )

        // Overlay layer for detected tamper zones
        if (viewMode == DocumentViewMode.TAMPER_HIGHLIGHT && document.anomalies.isNotEmpty()) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(document.anomalies) {
                        detectTapGestures { offset ->
                            val normX = offset.x / size.width
                            val normY = offset.y / size.height
                            // Find tapped anomaly
                            val tapped = document.anomalies.firstOrNull { a ->
                                normX in a.bounds.left..a.bounds.right &&
                                        normY in a.bounds.top..a.bounds.bottom
                            }
                            if (tapped != null) {
                                onAnomalySelected(tapped)
                            }
                        }
                    }
            ) {
                val canvasW = size.width
                val canvasH = size.height

                for (anomaly in document.anomalies) {
                    val isCurrent = anomaly.id == selectedAnomaly?.id
                    val baseColor = when (anomaly.severity) {
                        AnomalySeverity.CRITICAL -> Color(0xFFEF4444)
                        AnomalySeverity.HIGH -> Color(0xFFF97316)
                        AnomalySeverity.MEDIUM -> Color(0xFFEAB308)
                        AnomalySeverity.LOW -> Color(0xFF60A5FA)
                    }

                    val left = anomaly.bounds.left * canvasW
                    val top = anomaly.bounds.top * canvasH
                    val right = anomaly.bounds.right * canvasW
                    val bottom = anomaly.bounds.bottom * canvasH
                    val width = right - left
                    val height = bottom - top

                    // Semi-transparent highlight fill
                    drawRect(
                        color = baseColor.copy(alpha = if (isCurrent) 0.40f else pulseAlpha * 0.25f),
                        topLeft = androidx.compose.ui.geometry.Offset(left, top),
                        size = androidx.compose.ui.geometry.Size(width, height)
                    )

                    // Glowing perimeter border
                    drawRect(
                        color = baseColor.copy(alpha = if (isCurrent) 1.0f else pulseAlpha),
                        topLeft = androidx.compose.ui.geometry.Offset(left, top),
                        size = androidx.compose.ui.geometry.Size(width, height),
                        style = Stroke(width = if (isCurrent) 6f else 3.5f)
                    )
                }
            }

            // Floating indicator badges for anomalies
            for (anomaly in document.anomalies) {
                val isCurrent = anomaly.id == selectedAnomaly?.id
                val badgeColor = when (anomaly.severity) {
                    AnomalySeverity.CRITICAL -> Color(0xFFDC2626)
                    AnomalySeverity.HIGH -> Color(0xFFEA580C)
                    AnomalySeverity.MEDIUM -> Color(0xFFCA8A04)
                    AnomalySeverity.LOW -> Color(0xFF2563EB)
                }

                val offsetX = containerWidth * anomaly.bounds.left
                val offsetY = (containerHeight * anomaly.bounds.top) - 18.dp

                Box(
                    modifier = Modifier
                        .offset(x = offsetX, y = offsetY.coerceAtLeast(4.dp))
                        .background(badgeColor, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .clickable { onAnomalySelected(anomaly) }
                ) {
                    Text(
                        text = if (isCurrent) "► ${anomaly.field}" else anomaly.field,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
