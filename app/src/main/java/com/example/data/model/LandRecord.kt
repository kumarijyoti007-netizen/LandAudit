package com.example.data.model

import java.io.Serializable

data class NormalizedRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) : Serializable

enum class AnomalySeverity {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW
}

enum class AnomalyType {
    FONT_MISMATCH,
    STROKE_DENSITY_OVERWRITE,
    DIGITAL_SPLICING,
    ARITHMETIC_INCONSISTENCY,
    SEAL_WATERMARK_DEFECT,
    DATE_ANOMALY,
    SUSPICIOUS_ERASURE
}

enum class AuditStatus {
    VERIFIED_GENUINE,
    SUSPECTED_TAMPERING,
    HIGH_RISK_FORGERY,
    DEGRADED_MANUAL_REVIEW
}

enum class DocumentSourceType {
    SAMPLE_AGED_JAMABANDI,
    SAMPLE_TAMPERED_DEED,
    SAMPLE_HANDWRITTEN_PATTA,
    USER_SCANNED
}

data class LandBoundaries(
    val north: String = "Plot No. 41 (Ramesh Patil)",
    val south: String = "Main Village PWD Road (12m width)",
    val east: String = "Irrigation Canal / Plot 43",
    val west: String = "Plot No. 39-A (Gram Panchayat land)"
) : Serializable

data class ExtractedLandInfo(
    val ownerName: String,
    val coOwners: List<String> = emptyList(),
    val surveyNumber: String,
    val subdivisionNumber: String = "",
    val khataNumber: String = "",
    val village: String,
    val talukOrTehsil: String,
    val district: String,
    val state: String = "Revenue Department",
    val totalAreaText: String,
    val totalAreaAcre: Double = 0.0,
    val landClassification: String,
    val registrationDate: String,
    val stampDutyAmount: String,
    val subRegistrarOffice: String,
    val boundaries: LandBoundaries = LandBoundaries(),
    val encumbranceNotes: String = "No registered encumbrance found",
    val overallOcrConfidence: Float = 0.92f
) : Serializable

data class TamperAnomaly(
    val id: String,
    val field: String,
    val type: AnomalyType,
    val severity: AnomalySeverity,
    val title: String,
    val description: String,
    val detectedValue: String,
    val originalSuspicion: String? = null,
    val bounds: NormalizedRect,
    val confidence: Float
) : Serializable

data class OcrTextLine(
    val id: String,
    val text: String,
    val confidence: Float,
    val isFlagged: Boolean = false,
    val bounds: NormalizedRect
) : Serializable

data class LandRecordDocument(
    val id: String,
    val title: String,
    val documentType: String,
    val sourceType: DocumentSourceType,
    val imagePathOrRes: String,
    val dateScanned: Long = System.currentTimeMillis(),
    val status: AuditStatus,
    val tamperScore: Int, // 0 (pristine) to 100 (forged)
    val documentHash: String, // SHA-256
    val verificationToken: String,
    val extractedData: ExtractedLandInfo,
    val anomalies: List<TamperAnomaly> = emptyList(),
    val ocrLines: List<OcrTextLine> = emptyList(),
    val imageQualityScore: Float = 0.85f,
    val pdfExportPath: String? = null
) : Serializable
