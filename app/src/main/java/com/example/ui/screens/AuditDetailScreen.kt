package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnomalySeverity
import com.example.data.model.AuditStatus
import com.example.data.model.LandRecordDocument
import com.example.data.model.TamperAnomaly
import com.example.ui.components.DocumentViewerWithOverlay
import com.example.ui.components.TamperStatusBadge
import com.example.ui.viewmodel.DocumentViewMode
import com.example.ui.viewmodel.LandAuditViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditDetailScreen(
    document: LandRecordDocument,
    viewModel: LandAuditViewModel,
    onBack: () -> Unit,
    onExportPdf: (LandRecordDocument) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewMode by viewModel.viewMode.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedAnomaly by remember { mutableStateOf<TamperAnomaly?>(document.anomalies.firstOrNull()) }

    // Enforce back button handling per guidelines
    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = document.extractedData.surveyNumber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Token: ${document.verificationToken}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to list"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onExportPdf(document) },
                        modifier = Modifier.testTag("detail_export_pdf_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Export Verifiable PDF",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.testTag("detail_top_bar")
            )
        },
        bottomBar = {
            SurfaceBottomBar(
                onExportPdf = { onExportPdf(document) },
                onShareSummary = {
                    val summaryText = buildString {
                        append("LandAudit Verification Summary\n")
                        append("Survey No: ${document.extractedData.surveyNumber}\n")
                        append("Owner: ${document.extractedData.ownerName}\n")
                        append("Area: ${document.extractedData.totalAreaText}\n")
                        append("Location: ${document.extractedData.village}, ${document.extractedData.district}\n")
                        append("Status: ${document.status.name} (${document.tamperScore}% Risk)\n")
                        append("Verification Token: ${document.verificationToken}\n")
                        append("SHA-256: ${document.documentHash}\n")
                    }
                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_SUBJECT, "Land Record Audit - ${document.extractedData.surveyNumber}")
                        putExtra(android.content.Intent.EXTRA_TEXT, summaryText)
                    }
                    context.startActivity(android.content.Intent.createChooser(intent, "Share Land Audit Summary"))
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                end = 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Status Banner & Score
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (document.status == AuditStatus.HIGH_RISK_FORGERY || document.status == AuditStatus.SUSPECTED_TAMPERING)
                            Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TamperStatusBadge(
                                status = document.status,
                                tamperScore = document.tamperScore
                            )
                            Text(
                                text = "OCR Accuracy: ${(document.extractedData.overallOcrConfidence * 100).toInt()}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val explanation = when (document.status) {
                            AuditStatus.HIGH_RISK_FORGERY ->
                                "Severe alterations detected: handwritten area overwriting and divergent ink chemistry identified on document."
                            AuditStatus.SUSPECTED_TAMPERING ->
                                "Minor baseline or boundary discrepancy detected. Secondary revenue verification recommended."
                            AuditStatus.DEGRADED_MANUAL_REVIEW ->
                                "Aged physical document with significant fiber wear. Review highlighted areas manually."
                            AuditStatus.VERIFIED_GENUINE ->
                                "Genuine historical land record. Natural fiber aging without digital splicing or overwritten characters."
                        }

                        Text(
                            text = explanation,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 2. Interactive Document Viewer with Mode Switcher
            item {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Document Inspection Window",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Mode Selector Pills
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            ViewModeButton(
                                label = "Tamper Overlay",
                                isSelected = viewMode == DocumentViewMode.TAMPER_HIGHLIGHT,
                                onClick = { viewModel.setViewMode(DocumentViewMode.TAMPER_HIGHLIGHT) },
                                tag = "mode_tamper_btn"
                            )
                            ViewModeButton(
                                label = "Enhance Ink",
                                isSelected = viewMode == DocumentViewMode.ENHANCED_CONTRAST,
                                onClick = { viewModel.setViewMode(DocumentViewMode.ENHANCED_CONTRAST) },
                                tag = "mode_contrast_btn"
                            )
                            ViewModeButton(
                                label = "Original",
                                isSelected = viewMode == DocumentViewMode.ORIGINAL,
                                onClick = { viewModel.setViewMode(DocumentViewMode.ORIGINAL) },
                                tag = "mode_orig_btn"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    DocumentViewerWithOverlay(
                        document = document,
                        viewMode = viewMode,
                        selectedAnomaly = selectedAnomaly,
                        onAnomalySelected = {
                            selectedAnomaly = it
                            selectedTab = 1 // Switch to Tamper tab
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )

                    if (viewMode == DocumentViewMode.TAMPER_HIGHLIGHT && document.anomalies.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "💡 Tap on any highlighted box on document to inspect alteration notes.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 3. Inspection Tabs
            item {
                val tabs = listOf("Land Details", "Tamper Analysis (${document.anomalies.size})", "OCR Transcript", "Proof & Hash")

                SecondaryScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            },
                            modifier = Modifier.testTag("tab_$index")
                        )
                    }
                }
            }

            // 4. Tab Content
            when (selectedTab) {
                0 -> { // Extracted Land Details
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Decrypted Ownership & Parcel Metadata",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                DetailRow("Primary Title Holder", document.extractedData.ownerName, isBold = true)
                                if (document.extractedData.coOwners.isNotEmpty()) {
                                    DetailRow("Co-Owners / Heirs", document.extractedData.coOwners.joinToString(", "))
                                }
                                DetailRow("Survey / Parcel Number", document.extractedData.surveyNumber, isHighlight = true)
                                DetailRow("Subdivision / Khata", "${document.extractedData.subdivisionNumber} • Khata: ${document.extractedData.khataNumber}")
                                DetailRow("Demarcated Total Area", document.extractedData.totalAreaText, isBold = true)
                                DetailRow("Land Classification", document.extractedData.landClassification)
                                DetailRow("Revenue Village & Taluk", "${document.extractedData.village}, Tehsil: ${document.extractedData.talukOrTehsil}")
                                DetailRow("District & State", "${document.extractedData.district}, ${document.extractedData.state}")
                                DetailRow("Registration Date", document.extractedData.registrationDate)
                                DetailRow("Stamp Duty Amount", document.extractedData.stampDutyAmount)
                                DetailRow("Sub-Registrar Office", document.extractedData.subRegistrarOffice)
                            }
                        }
                    }

                    // Boundaries Card
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Demarcated Boundary Coordinates",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                DetailRow("North Boundary", document.extractedData.boundaries.north)
                                DetailRow("South Boundary", document.extractedData.boundaries.south)
                                DetailRow("East Boundary", document.extractedData.boundaries.east)
                                DetailRow("West Boundary", document.extractedData.boundaries.west)
                            }
                        }
                    }

                    // Encumbrances
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Encumbrances & Legal Notes",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = document.extractedData.encumbranceNotes,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                1 -> { // Forgery & Tamper Analysis
                    if (document.anomalies.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "No tampering, physical erasure, or digital splicing detected in this document.",
                                        fontSize = 13.sp,
                                        color = Color(0xFF1B5E20)
                                    )
                                }
                            }
                        }
                    } else {
                        items(document.anomalies) { anomaly ->
                            val isSelected = anomaly.id == selectedAnomaly?.id
                            AnomalyCard(
                                anomaly = anomaly,
                                isSelected = isSelected,
                                onClick = { selectedAnomaly = anomaly }
                            )
                        }
                    }
                }

                2 -> { // OCR Transcript
                    items(document.ocrLines) { line ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (line.isFlagged) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    if (line.isFlagged) Color(0xFFEF9A9A) else MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (line.isFlagged) Color(0xFFC62828) else Color(0xFF2E7D32),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (line.isFlagged) "ALTERED" else "${(line.confidence * 100).toInt()}%",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = line.text,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (line.isFlagged) Color(0xFFB71C1C) else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                3 -> { // Proof & Hash
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Cryptographic Verification Fingerprint",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Document SHA-256 Hash",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF3F4F6), RoundedCornerShape(6.dp))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = document.documentHash,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = Color(0xFF1F2937),
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("SHA-256 Hash", document.documentHash)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "SHA-256 Hash Copied", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy Hash",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                DetailRow("Verification Reference", document.verificationToken, isBold = true)
                                DetailRow("Inspection Timestamp", java.text.SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", java.util.Locale.US).format(java.util.Date(document.dateScanned)))
                                DetailRow("Document Type", document.documentType)
                                DetailRow("Auditing Engine", "LandAudit ML Optical Density & Typography Inspector v2.4")

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "This cryptographic hash is permanently anchored in the verifiable PDF summary. Any pixel modification to the scanned deed will invalidate the SHA-256 fingerprint.",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnomalyCard(
    anomaly: TamperAnomaly,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val severityColor = when (anomaly.severity) {
        AnomalySeverity.CRITICAL -> Color(0xFFC62828)
        AnomalySeverity.HIGH -> Color(0xFFE65100)
        AnomalySeverity.MEDIUM -> Color(0xFFF57F17)
        AnomalySeverity.LOW -> Color(0xFF1976D2)
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                if (isSelected) 2.dp else 1.dp,
                if (isSelected) severityColor else MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .testTag("anomaly_card_${anomaly.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .background(severityColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${anomaly.severity.name} RISK • ${anomaly.type.name.replace("_", " ")}",
                        color = severityColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Field: ${anomaly.field}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = anomaly.title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = anomaly.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Detected Value in Scan:", fontSize = 10.sp, color = Color.Gray)
                    Text(anomaly.detectedValue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = severityColor)
                }
                if (anomaly.originalSuspicion != null) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Suspected Pre-Alteration:", fontSize = 10.sp, color = Color.Gray)
                        Text(anomaly.originalSuspicion, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32))
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.42f)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = if (isBold || isHighlight) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.58f)
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
}

@Composable
fun ViewModeButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Box(
        modifier = Modifier
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(6.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(tag)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SurfaceBottomBar(
    onExportPdf: () -> Unit,
    onShareSummary: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onShareSummary,
                modifier = Modifier
                    .weight(0.38f)
                    .height(48.dp)
                    .testTag("share_summary_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share")
            }

            Button(
                onClick = onExportPdf,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .weight(0.62f)
                    .height(48.dp)
                    .testTag("export_verifiable_pdf_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Verifiable PDF", fontWeight = FontWeight.Bold)
            }
        }
    }
}
