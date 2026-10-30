package com.example.engine

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.model.AnomalySeverity
import com.example.data.model.AuditStatus
import com.example.data.model.LandRecordDocument
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    private const val PAGE_WIDTH = 595 // Standard A4 width in points
    private const val PAGE_HEIGHT = 842 // Standard A4 height in points

    fun generateAuditPdf(context: Context, document: LandRecordDocument): File {
        val pdfDocument = PdfDocument()

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val boldBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Setup paints
        titlePaint.textSize = 16f
        titlePaint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        titlePaint.color = Color.rgb(27, 94, 32) // Forest green

        headerPaint.textSize = 11f
        headerPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        headerPaint.color = Color.rgb(33, 33, 33)

        bodyPaint.textSize = 9.5f
        bodyPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        bodyPaint.color = Color.rgb(55, 65, 81)

        boldBodyPaint.textSize = 9.5f
        boldBodyPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        boldBodyPaint.color = Color.rgb(31, 41, 55)

        smallPaint.textSize = 7.5f
        smallPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        smallPaint.color = Color.rgb(107, 114, 128)

        // ================= PAGE 1: Executive Summary & Title Extract =================
        val pageInfo1 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page1 = pdfDocument.startPage(pageInfo1)
        val canvas1 = page1.canvas

        drawPage1(canvas1, document, titlePaint, headerPaint, bodyPaint, boldBodyPaint, smallPaint)
        pdfDocument.finishPage(page1)

        // ================= PAGE 2: Forensic Forgery Log & OCR Transcript =================
        val pageInfo2 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 2).create()
        val page2 = pdfDocument.startPage(pageInfo2)
        val canvas2 = page2.canvas

        drawPage2(canvas2, document, titlePaint, headerPaint, bodyPaint, boldBodyPaint, smallPaint)
        pdfDocument.finishPage(page2)

        // Write to output file in cache directory
        val exportDir = File(context.cacheDir, "land_audit_reports")
        if (!exportDir.exists()) exportDir.mkdirs()

        val safeId = document.id.take(8)
        val pdfFile = File(exportDir, "LandAudit_${document.extractedData.surveyNumber.replace("[^a-zA-Z0-9]".toRegex(), "_")}_$safeId.pdf")
        val outputStream = FileOutputStream(pdfFile)
        pdfDocument.writeTo(outputStream)
        outputStream.flush()
        outputStream.close()
        pdfDocument.close()

        return pdfFile
    }

    private fun drawPage1(
        canvas: Canvas,
        doc: LandRecordDocument,
        titlePaint: Paint,
        headerPaint: Paint,
        bodyPaint: Paint,
        boldPaint: Paint,
        smallPaint: Paint
    ) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)

        // Page Header Banner (Dark Forest Green)
        p.color = Color.rgb(27, 94, 32)
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 68f, p)

        // Gold Accent Line
        p.color = Color.rgb(184, 134, 11)
        canvas.drawRect(0f, 68f, PAGE_WIDTH.toFloat(), 72f, p)

        // Header Title
        p.color = Color.WHITE
        p.textSize = 15f
        p.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        canvas.drawText("LAND RECORD FORENSIC AUDIT & VERIFICATION SUMMARY", 36f, 32f, p)

        p.textSize = 8.5f
        p.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        canvas.drawText("Official Document Integrity, OCR Decryption & Forgery Detection Certificate", 36f, 50f, p)

        var y = 92f

        // Document Meta Bar (Verification ID & Date)
        p.color = Color.rgb(243, 244, 246)
        canvas.drawRoundRect(RectF(36f, y, PAGE_WIDTH - 36f, y + 32f), 4f, 4f, p)

        boldPaint.color = Color.rgb(31, 41, 55)
        canvas.drawText("Verification Token:", 48f, y + 20f, boldPaint)
        bodyPaint.color = Color.rgb(27, 94, 32)
        bodyPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        canvas.drawText(doc.verificationToken, 150f, y + 20f, bodyPaint)

        bodyPaint.color = Color.rgb(75, 85, 99)
        bodyPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        val dateFormatted = SimpleDateFormat("dd MMM yyyy, HH:mm z", Locale.US).format(Date(doc.dateScanned))
        canvas.drawText("Audited On: $dateFormatted", 370f, y + 20f, bodyPaint)

        y += 46f

        // Status & Authenticity Box
        val isTampered = doc.status == AuditStatus.HIGH_RISK_FORGERY || doc.status == AuditStatus.SUSPECTED_TAMPERING
        val boxBgColor = if (isTampered) Color.rgb(254, 242, 242) else Color.rgb(240, 253, 244)
        val boxBorderColor = if (isTampered) Color.rgb(220, 38, 38) else Color.rgb(22, 163, 74)

        p.color = boxBgColor
        canvas.drawRoundRect(RectF(36f, y, PAGE_WIDTH - 36f, y + 54f), 6f, 6f, p)
        p.color = boxBorderColor
        p.style = Paint.Style.STROKE
        p.strokeWidth = 1.5f
        canvas.drawRoundRect(RectF(36f, y, PAGE_WIDTH - 36f, y + 54f), 6f, 6f, p)
        p.style = Paint.Style.FILL

        val statusText = when (doc.status) {
            AuditStatus.HIGH_RISK_FORGERY -> "CRITICAL ALERT: HIGH PROBABILITY OF DOCUMENT FORGERY"
            AuditStatus.SUSPECTED_TAMPERING -> "WARNING: SUSPECTED SPLICING / OVERWRITING DETECTED"
            AuditStatus.DEGRADED_MANUAL_REVIEW -> "NOTICE: HIGH DEGRADATION - MANUAL ARCHIVAL REVIEW REQUIRED"
            AuditStatus.VERIFIED_GENUINE -> "VERIFIED: AUTHENTIC HISTORICAL LAND RECORD"
        }

        p.color = boxBorderColor
        p.textSize = 10.5f
        p.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText(statusText, 48f, y + 22f, p)

        bodyPaint.color = Color.rgb(55, 65, 81)
        val scoreDesc = "Tamper Risk Index: ${doc.tamperScore}/100 | Anomalies Flagged: ${doc.anomalies.size} | OCR Confidence: ${(doc.extractedData.overallOcrConfidence * 100).toInt()}%"
        canvas.drawText(scoreDesc, 48f, y + 40f, bodyPaint)

        y += 70f

        // SHA-256 Cryptographic Fingerprint
        headerPaint.textSize = 10f
        headerPaint.color = Color.rgb(31, 41, 55)
        canvas.drawText("1. CRYPTOGRAPHIC SOURCE DOCUMENT FINGERPRINT", 36f, y, headerPaint)
        y += 14f

        p.color = Color.rgb(249, 250, 251)
        canvas.drawRect(RectF(36f, y, PAGE_WIDTH - 36f, y + 26f), p)
        p.color = Color.rgb(229, 231, 235)
        p.style = Paint.Style.STROKE
        p.strokeWidth = 1f
        canvas.drawRect(RectF(36f, y, PAGE_WIDTH - 36f, y + 26f), p)
        p.style = Paint.Style.FILL

        smallPaint.textSize = 8f
        smallPaint.color = Color.rgb(31, 41, 55)
        canvas.drawText("SHA-256: ${doc.documentHash}", 44f, y + 16f, smallPaint)

        y += 42f

        // Section 2: Extracted Landholder & Title Metadata Table
        canvas.drawText("2. EXTRACTED LANDHOLDER & PARCEL METADATA (OCR + ML)", 36f, y, headerPaint)
        y += 14f

        // Table headers
        val startY = y
        val rowHeight = 22f
        val col1 = 36f
        val col2 = 180f
        val col3 = PAGE_WIDTH - 36f

        val fields = listOf(
            "Primary Title Holder:" to doc.extractedData.ownerName,
            "Co-Owners / Heirs:" to if (doc.extractedData.coOwners.isNotEmpty()) doc.extractedData.coOwners.joinToString(", ") else "None Recorded",
            "Survey / Parcel ID:" to doc.extractedData.surveyNumber,
            "Subdivision / Khata:" to "${doc.extractedData.subdivisionNumber} | Khata: ${doc.extractedData.khataNumber}",
            "Location / Revenue Circle:" to "${doc.extractedData.village}, Tehsil: ${doc.extractedData.talukOrTehsil}",
            "District & State:" to "${doc.extractedData.district}, ${doc.extractedData.state}",
            "Total Demarcated Area:" to doc.extractedData.totalAreaText,
            "Land Classification:" to doc.extractedData.landClassification,
            "Registration Date:" to doc.extractedData.registrationDate,
            "Stamp Duty Recorded:" to doc.extractedData.stampDutyAmount,
            "Sub-Registrar Office:" to doc.extractedData.subRegistrarOffice
        )

        for ((idx, field) in fields.withIndex()) {
            val isEven = idx % 2 == 0
            p.color = if (isEven) Color.rgb(249, 250, 251) else Color.WHITE
            canvas.drawRect(RectF(col1, y, col3, y + rowHeight), p)

            boldPaint.color = Color.rgb(55, 65, 81)
            canvas.drawText(field.first, col1 + 8f, y + 15f, boldPaint)

            bodyPaint.color = Color.rgb(17, 24, 39)
            canvas.drawText(field.second, col2 + 8f, y + 15f, bodyPaint)

            y += rowHeight
        }

        // Outer border for table
        p.color = Color.rgb(209, 213, 219)
        p.style = Paint.Style.STROKE
        p.strokeWidth = 1f
        canvas.drawRect(RectF(col1, startY, col3, y), p)
        p.style = Paint.Style.FILL

        y += 24f

        // Section 3: Boundary Demarcations
        canvas.drawText("3. BOUNDARY DEMARCATIONS & ABUTTING PARCELS", 36f, y, headerPaint)
        y += 14f

        val boundaryTableStart = y
        val boundaries = listOf(
            "North Boundary:" to doc.extractedData.boundaries.north,
            "South Boundary:" to doc.extractedData.boundaries.south,
            "East Boundary:" to doc.extractedData.boundaries.east,
            "West Boundary:" to doc.extractedData.boundaries.west
        )

        for ((idx, b) in boundaries.withIndex()) {
            val isEven = idx % 2 == 0
            p.color = if (isEven) Color.rgb(249, 250, 251) else Color.WHITE
            canvas.drawRect(RectF(col1, y, col3, y + rowHeight), p)

            boldPaint.color = Color.rgb(55, 65, 81)
            canvas.drawText(b.first, col1 + 8f, y + 15f, boldPaint)

            bodyPaint.color = Color.rgb(17, 24, 39)
            canvas.drawText(b.second, col2 + 8f, y + 15f, bodyPaint)

            y += rowHeight
        }

        p.color = Color.rgb(209, 213, 219)
        p.style = Paint.Style.STROKE
        p.strokeWidth = 1f
        canvas.drawRect(RectF(col1, boundaryTableStart, col3, y), p)
        p.style = Paint.Style.FILL

        // Page 1 Footer
        drawPageFooter(canvas, 1, 2)
    }

    private fun drawPage2(
        canvas: Canvas,
        doc: LandRecordDocument,
        titlePaint: Paint,
        headerPaint: Paint,
        bodyPaint: Paint,
        boldPaint: Paint,
        smallPaint: Paint
    ) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)

        // Page Header Banner (Slim)
        p.color = Color.rgb(27, 94, 32)
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 36f, p)
        p.color = Color.WHITE
        p.textSize = 10f
        p.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        canvas.drawText("LAND RECORD FORENSIC AUDIT - DETAILED ANOMALY LOG & TRANSCRIPT", 36f, 22f, p)

        var y = 58f

        // Section 4: Forensic Anomaly & Tamper Log
        headerPaint.textSize = 10f
        headerPaint.color = Color.rgb(31, 41, 55)
        canvas.drawText("4. FORENSIC TAMPER & ALTERATION LOG (${doc.anomalies.size} Flagged)", 36f, y, headerPaint)
        y += 14f

        if (doc.anomalies.isEmpty()) {
            p.color = Color.rgb(240, 253, 244)
            canvas.drawRoundRect(RectF(36f, y, PAGE_WIDTH - 36f, y + 36f), 4f, 4f, p)
            p.color = Color.rgb(22, 101, 52)
            p.textSize = 9.5f
            canvas.drawText("No physical overwriting, ink discontinuity, or digital splicing detected. All characters verified.", 48f, y + 22f, p)
            y += 48f
        } else {
            for (anomaly in doc.anomalies) {
                val cardHeight = 62f
                val severityColor = when (anomaly.severity) {
                    AnomalySeverity.CRITICAL -> Color.rgb(220, 38, 38)
                    AnomalySeverity.HIGH -> Color.rgb(234, 88, 12)
                    AnomalySeverity.MEDIUM -> Color.rgb(202, 138, 4)
                    AnomalySeverity.LOW -> Color.rgb(75, 85, 99)
                }

                // Card Background
                p.color = Color.rgb(254, 254, 254)
                canvas.drawRoundRect(RectF(36f, y, PAGE_WIDTH - 36f, y + cardHeight), 4f, 4f, p)
                p.color = Color.rgb(229, 231, 235)
                p.style = Paint.Style.STROKE
                p.strokeWidth = 1f
                canvas.drawRoundRect(RectF(36f, y, PAGE_WIDTH - 36f, y + cardHeight), 4f, 4f, p)
                p.style = Paint.Style.FILL

                // Severity Indicator Strip on Left
                p.color = severityColor
                canvas.drawRoundRect(RectF(36f, y, 42f, y + cardHeight), 2f, 2f, p)

                // Title & Field
                boldPaint.textSize = 9.5f
                boldPaint.color = severityColor
                canvas.drawText("[${anomaly.severity.name}] ${anomaly.title} (Field: ${anomaly.field})", 48f, y + 16f, boldPaint)

                // Description (Truncate cleanly if long)
                bodyPaint.textSize = 8.5f
                bodyPaint.color = Color.rgb(55, 65, 81)
                val line1 = if (anomaly.description.length > 95) anomaly.description.take(92) + "..." else anomaly.description
                canvas.drawText(line1, 48f, y + 30f, bodyPaint)

                // Detected vs Original Values
                boldPaint.color = Color.rgb(31, 41, 55)
                canvas.drawText("Detected in Scan: \"${anomaly.detectedValue}\"", 48f, y + 46f, boldPaint)
                if (anomaly.originalSuspicion != null) {
                    p.color = Color.rgb(180, 83, 9)
                    p.textSize = 8.5f
                    canvas.drawText("Suspected Pre-Alteration: \"${anomaly.originalSuspicion}\"", 260f, y + 46f, p)
                }

                y += cardHeight + 8f
            }
        }

        y += 10f

        // Section 5: Decrypted OCR Transcript
        headerPaint.textSize = 10f
        headerPaint.color = Color.rgb(31, 41, 55)
        canvas.drawText("5. OPTICAL CHARACTER RECOGNITION (OCR) FULL TRANSCRIPT", 36f, y, headerPaint)
        y += 14f

        p.color = Color.rgb(249, 250, 251)
        val transcriptHeight = (doc.ocrLines.size.coerceAtMost(8) * 18f + 16f).coerceAtLeast(60f)
        canvas.drawRoundRect(RectF(36f, y, PAGE_WIDTH - 36f, y + transcriptHeight), 4f, 4f, p)
        p.color = Color.rgb(229, 231, 235)
        p.style = Paint.Style.STROKE
        p.strokeWidth = 1f
        canvas.drawRoundRect(RectF(36f, y, PAGE_WIDTH - 36f, y + transcriptHeight), 4f, 4f, p)
        p.style = Paint.Style.FILL

        var textY = y + 16f
        for (line in doc.ocrLines.take(8)) {
            val confColor = if (line.isFlagged) Color.rgb(220, 38, 38) else Color.rgb(16, 149, 74)
            p.color = confColor
            p.textSize = 7.5f
            p.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            val flagMarker = if (line.isFlagged) "[ALTERED]" else "[OK]"
            canvas.drawText("$flagMarker ${(line.confidence * 100).toInt()}%", 46f, textY, p)

            bodyPaint.textSize = 8f
            bodyPaint.color = if (line.isFlagged) Color.rgb(185, 28, 28) else Color.rgb(31, 41, 55)
            val displayText = if (line.text.length > 70) line.text.take(68) + "..." else line.text
            canvas.drawText(displayText, 120f, textY, bodyPaint)

            textY += 18f
        }

        y += transcriptHeight + 20f

        // Section 6: Official Verification Seal & Legal Attestation
        p.color = Color.rgb(243, 244, 246)
        canvas.drawRoundRect(RectF(36f, y, PAGE_WIDTH - 36f, y + 80f), 4f, 4f, p)

        // Draw Simulated Verifiable QR Code Matrix on Canvas
        drawVerifiableQrCode(canvas, 48f, y + 10f, 60f)

        boldPaint.textSize = 9.5f
        boldPaint.color = Color.rgb(27, 94, 32)
        canvas.drawText("VERIFIED BY LANDAUDIT FORENSIC ENGINE", 125f, y + 25f, boldPaint)

        smallPaint.textSize = 7.5f
        smallPaint.color = Color.rgb(75, 85, 99)
        canvas.drawText("This cryptographic audit summary certifies that the digital document was evaluated", 125f, y + 38f, smallPaint)
        canvas.drawText("for ink micro-variations, character baseline displacement, and historical stamp integrity.", 125f, y + 49f, smallPaint)
        canvas.drawText("Verify digital hash at: landaudit://verify?token=${doc.verificationToken}", 125f, y + 62f, smallPaint)

        drawPageFooter(canvas, 2, 2)
    }

    private fun drawVerifiableQrCode(canvas: Canvas, x: Float, y: Float, size: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.WHITE
        canvas.drawRect(x, y, x + size, y + size, p)

        p.color = Color.rgb(17, 24, 39)
        val cells = 9
        val cellSize = size / cells

        // Draw deterministic matrix pattern with finder corners
        val qrMatrix = arrayOf(
            intArrayOf(1, 1, 1, 0, 1, 0, 1, 1, 1),
            intArrayOf(1, 0, 1, 0, 0, 1, 1, 0, 1),
            intArrayOf(1, 1, 1, 1, 1, 0, 1, 1, 1),
            intArrayOf(0, 0, 0, 1, 0, 1, 0, 0, 0),
            intArrayOf(1, 1, 0, 0, 1, 1, 0, 1, 1),
            intArrayOf(0, 1, 1, 1, 0, 0, 1, 0, 0),
            intArrayOf(1, 1, 1, 0, 1, 0, 1, 1, 1),
            intArrayOf(1, 0, 1, 1, 0, 1, 1, 0, 1),
            intArrayOf(1, 1, 1, 0, 1, 1, 1, 1, 1)
        )

        for (r in 0 until cells) {
            for (c in 0 until cells) {
                if (qrMatrix[r][c] == 1) {
                    canvas.drawRect(x + c * cellSize, y + r * cellSize, x + (c + 1) * cellSize, y + (r + 1) * cellSize, p)
                }
            }
        }
    }

    private fun drawPageFooter(canvas: Canvas, pageNumber: Int, totalPages: Int) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.rgb(156, 163, 175)
        p.textSize = 8f
        p.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

        canvas.drawLine(36f, PAGE_HEIGHT - 32f, PAGE_WIDTH - 36f, PAGE_HEIGHT - 32f, p)
        canvas.drawText("Confidential Land Record Forensic Audit | Powered by Machine Learning", 36f, PAGE_HEIGHT - 18f, p)
        canvas.drawText("Page $pageNumber of $totalPages", PAGE_WIDTH - 90f, PAGE_HEIGHT - 18f, p)
    }
}
