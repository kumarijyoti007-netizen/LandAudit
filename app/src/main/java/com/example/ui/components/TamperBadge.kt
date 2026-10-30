package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuditStatus

@Composable
fun TamperStatusBadge(
    status: AuditStatus,
    tamperScore: Int,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, borderColor, icon, label) = when (status) {
        AuditStatus.HIGH_RISK_FORGERY -> Pentad(
            Color(0xFFFFEBEE),
            Color(0xFFC62828),
            Color(0xFFEF9A9A),
            Icons.Default.Warning,
            "CRITICAL: FORGERY DETECTED"
        )
        AuditStatus.SUSPECTED_TAMPERING -> Pentad(
            Color(0xFFFFF3E0),
            Color(0xFFE65100),
            Color(0xFFFFCC80),
            Icons.Default.Warning,
            "SUSPECTED ALTERATION"
        )
        AuditStatus.DEGRADED_MANUAL_REVIEW -> Pentad(
            Color(0xFFF5F5F5),
            Color(0xFF616161),
            Color(0xFFE0E0E0),
            Icons.Default.Warning,
            "DEGRADED / MANUAL REVIEW"
        )
        AuditStatus.VERIFIED_GENUINE -> Pentad(
            Color(0xFFE8F5E9),
            Color(0xFF1B5E20),
            Color(0xFFA5D6A7),
            Icons.Default.CheckCircle,
            "VERIFIED AUTHENTIC"
        )
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(20.dp))
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "$label (${tamperScore}% Risk)",
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private data class Pentad<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
