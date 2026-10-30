package com.example.engine

import com.example.R
import com.example.data.model.AnomalySeverity
import com.example.data.model.AnomalyType
import com.example.data.model.AuditStatus
import com.example.data.model.DocumentSourceType
import com.example.data.model.ExtractedLandInfo
import com.example.data.model.LandBoundaries
import com.example.data.model.LandRecordDocument
import com.example.data.model.NormalizedRect
import com.example.data.model.OcrTextLine
import com.example.data.model.TamperAnomaly

object SampleDocuments {

    val sample1AgedJamabandi = LandRecordDocument(
        id = "doc-jamabandi-1968",
        title = "Aged 1968 Jamabandi Nakkal (7/12 Extract)",
        documentType = "Jamabandi Revenue Extract (Satbara)",
        sourceType = DocumentSourceType.SAMPLE_AGED_JAMABANDI,
        imagePathOrRes = "res:${R.drawable.sample_aged_jamabandi_1793368309448}",
        dateScanned = System.currentTimeMillis() - 86400000L * 2,
        status = AuditStatus.VERIFIED_GENUINE,
        tamperScore = 6, // Low risk, authentic aged document
        documentHash = "a7f93c04b8e1903e84bf92a061483c65df5e2365823126be15e19747d8ff2bc1",
        verificationToken = "MH-PUN-1968-712-V889",
        imageQualityScore = 0.88f,
        extractedData = ExtractedLandInfo(
            ownerName = "Shivram Bapurao Patil",
            coOwners = listOf("Ramchandra Bapurao Patil", "Parvati Bapurao Patil"),
            surveyNumber = "Gut No. 112 (Survey 45/301)",
            subdivisionNumber = "Division 2",
            khataNumber = "301",
            village = "Dhamangaon",
            talukOrTehsil = "Haveli (Taloja Division)",
            district = "Pune",
            state = "Maharashtra Revenue Dept.",
            totalAreaText = "7/12 Standard Units (3 Hectares 42 Ares)",
            totalAreaAcre = 8.45,
            landClassification = "Agricultural Jirayat (Dry Crop Class-1)",
            registrationDate = "19th July 1968",
            stampDutyAmount = "50 Paise (Court Fee Revenue Stamp 1968 Series)",
            subRegistrarOffice = "Talathi Office, Haveli Taloja Division",
            boundaries = LandBoundaries(
                north = "Survey 111 (Kashinath Shinde)",
                south = "Public Village Cart Road & Nala",
                east = "Survey 113 (Gram Panchayat Grazing Field)",
                west = "Canal Boundary (Khadakwasla Basin)"
            ),
            encumbranceNotes = "Clean Title. Land revenue assessment rate fixed at Rs. 6.50 annually. No private bank lien recorded.",
            overallOcrConfidence = 0.94f
        ),
        anomalies = listOf(
            TamperAnomaly(
                id = "anom-0",
                field = "Paper Degradation & Watermarks",
                type = AnomalyType.SEAL_WATERMARK_DEFECT,
                severity = AnomalySeverity.LOW,
                title = "Natural Fiber Oxidation & Tear",
                description = "Edges display authentic physical tearing and iron-gall ink feathering typical of pre-1970 cotton rag paper. No digital splicing detected.",
                detectedValue = "Authentic aging patina",
                originalSuspicion = null,
                bounds = NormalizedRect(0.05f, 0.05f, 0.95f, 0.20f),
                confidence = 0.96f
            )
        ),
        ocrLines = listOf(
            OcrTextLine("l1", "जमाबंदी नक्कल - १९६८-६९ (Jamabandi Nakkal)", 0.98f, false, NormalizedRect(0.20f, 0.10f, 0.85f, 0.16f)),
            OcrTextLine("l2", "गाव : धामणगाव | तालुका : हवेली | जिल्हा : पुणे", 0.96f, false, NormalizedRect(0.18f, 0.16f, 0.88f, 0.21f)),
            OcrTextLine("l3", "अनुक्रमांक : ४५ | खाते क्र : ३०१", 0.95f, false, NormalizedRect(0.35f, 0.26f, 0.65f, 0.35f)),
            OcrTextLine("l4", "गट क्र / सर्वे नं : ११२ (Survey No. 112)", 0.97f, false, NormalizedRect(0.35f, 0.35f, 0.68f, 0.40f)),
            OcrTextLine("l5", "क्षेत्र : ७/१२ (Area 7/12 Extract)", 0.92f, false, NormalizedRect(0.35f, 0.40f, 0.60f, 0.45f)),
            OcrTextLine("l6", "भोगवटादार : शिवराम बापूराव पाटील (Shivram B. Patil)", 0.96f, false, NormalizedRect(0.35f, 0.46f, 0.85f, 0.52f)),
            OcrTextLine("l7", "आकार : ६ रु. ५० पै. (Assessment Rs 6.50)", 0.94f, false, NormalizedRect(0.35f, 0.52f, 0.70f, 0.58f)),
            OcrTextLine("l8", "REVENUE STAMP 19 JUL 1968 PUNE DISTRICT", 0.93f, false, NormalizedRect(0.16f, 0.23f, 0.36f, 0.46f)),
            OcrTextLine("l9", "तळोजा विभाग (Taloja Division) Assistant Talathi M.V. Joshi 19.7.68", 0.95f, false, NormalizedRect(0.65f, 0.58f, 0.90f, 0.72f))
        )
    )

    val sample2TamperedDeed = LandRecordDocument(
        id = "doc-tampered-deed-1956",
        title = "Tampered Conveyance Deed (Altered Acreage)",
        documentType = "Deed of Conveyance & Land Sale",
        sourceType = DocumentSourceType.SAMPLE_TAMPERED_DEED,
        imagePathOrRes = "res:${R.drawable.sample_tampered_deed_1793368323400}",
        dateScanned = System.currentTimeMillis() - 86400000L,
        status = AuditStatus.HIGH_RISK_FORGERY,
        tamperScore = 91, // Critical forgery detected!
        documentHash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
        verificationToken = "NRB-DAG-1956-FORGERY-ALERT",
        imageQualityScore = 0.93f,
        extractedData = ExtractedLandInfo(
            ownerName = "David K. Otieno (Purchaser)",
            coOwners = listOf("James Mwangi (Vendor)", "Mary Njeri (Vendor)"),
            surveyNumber = "Parcel Ref VM-040120-19F",
            subdivisionNumber = "Dagoretti Section 1",
            khataNumber = "LR-1956-0812",
            village = "Dagoretti",
            talukOrTehsil = "Dagoretti Division",
            district = "Nairobi District",
            state = "Ministry of Lands Registry",
            totalAreaText = "THIRTY-EIGHT (38) ACRES, 3 RODS, 12 PERCHES",
            totalAreaAcre = 38.825,
            landClassification = "Freehold Agricultural & Suburban Parcel",
            registrationDate = "12th August 1956",
            stampDutyAmount = "Ad-valorem Stamp Duty Paid (12 AUG 1956 Stamp)",
            subRegistrarOffice = "Land Registry Nairobi District",
            boundaries = LandBoundaries(
                north = "Dagoretti Main Reserve Boundary",
                south = "St. West Kalyor Horticultural Plot",
                east = "Boundary of Intired 1st Aena #1",
                west = "Public Road Right-of-Way"
            ),
            encumbranceNotes = "CRITICAL ALERT: Physical pen alteration on deed face. Clause specifies crossing out 'FIFTY (50) ACRES' with divergent ink. Requires verification against primary Registry Book.",
            overallOcrConfidence = 0.95f
        ),
        anomalies = listOf(
            TamperAnomaly(
                id = "anom-1",
                field = "Total Acreage Clause",
                type = AnomalyType.STROKE_DENSITY_OVERWRITE,
                severity = AnomalySeverity.CRITICAL,
                title = "Handwritten Area Overwriting & Alteration",
                description = "Primary acreage statement has 'FIFTY (50) ACRES' struck through with heavy blue ink crosses, and 'THIRTY-EIGHT (38) ACRES' inserted above. Micro-spectrophotometry highlights that the blue ink of the inserted amendment has modern synthetic dye peaks absent in the 1956 typed ribbon ink.",
                detectedValue = "38 Acres, 3 Rods, 12 Perches",
                originalSuspicion = "50 Acres (Typographical or Intentional Land Theft)",
                bounds = NormalizedRect(0.15f, 0.36f, 0.92f, 0.43f),
                confidence = 0.98f
            ),
            TamperAnomaly(
                id = "anom-2",
                field = "Margin Initials Verification",
                type = AnomalyType.FONT_MISMATCH,
                severity = AnomalySeverity.HIGH,
                title = "Suspicious Margin Endorsement Initials ('J.M.', 'M.N.')",
                description = "Initials 'J.M.' and 'M.N.' adjacent to the struck out acreage show tremulous stroke velocity and hesitation marks. Stroke pressure variation indicates tracing rather than natural freehand execution.",
                detectedValue = "Initials J.M. & M.N.",
                originalSuspicion = "Simulated forgery without vendor presence",
                bounds = NormalizedRect(0.88f, 0.36f, 0.97f, 0.43f),
                confidence = 0.89f
            ),
            TamperAnomaly(
                id = "anom-3",
                field = "Stamp Duty Registry Seal",
                type = AnomalyType.SEAL_WATERMARK_DEFECT,
                severity = AnomalySeverity.MEDIUM,
                title = "Stamp Duty Date & Registry Ink Disparity",
                description = "The red serrated registry seal 'LAND REGISTRY NAIROBI 12 AUG 1956' shows ink bleed patterns inconsistent with 1956 aniline red stamp pad formulation.",
                detectedValue = "12 AUG 1956 Stamp Duty Paid",
                originalSuspicion = "Post-dated stamp placement",
                bounds = NormalizedRect(0.62f, 0.45f, 0.92f, 0.65f),
                confidence = 0.82f
            )
        ),
        ocrLines = listOf(
            OcrTextLine("t1", "THIS INDENTURE - DEED OF CONVEYANCE AND LAND SALE", 0.99f, false, NormalizedRect(0.14f, 0.09f, 0.75f, 0.16f)),
            OcrTextLine("t2", "Dated in the register date of 12th August 1956", 0.98f, false, NormalizedRect(0.14f, 0.15f, 0.66f, 0.19f)),
            OcrTextLine("t3", "Vendors: James Mwangi & Mary Njeri", 0.97f, false, NormalizedRect(0.24f, 0.21f, 0.72f, 0.28f)),
            OcrTextLine("t4", "Purchaser: David K. Otieno", 0.97f, false, NormalizedRect(0.25f, 0.30f, 0.58f, 0.35f)),
            OcrTextLine("t5", "THIRTY-EIGHT (38) ACRES, 3 RODS, 12 PERCHES J.M.", 0.94f, true, NormalizedRect(0.20f, 0.36f, 0.95f, 0.40f)),
            OcrTextLine("t6", "consisting of approximately [STRIKE: FIFTY (50) ACRES] M.N.", 0.91f, true, NormalizedRect(0.15f, 0.39f, 0.95f, 0.43f)),
            OcrTextLine("t7", "Situated at Dagoretti, Nairobi District, VM-040120-19F", 0.95f, false, NormalizedRect(0.14f, 0.43f, 0.88f, 0.58f)),
            OcrTextLine("t8", "LAND REGISTRY NAIROBI - STAMP DUTY PAID 12 AUG 1956", 0.96f, true, NormalizedRect(0.63f, 0.47f, 0.92f, 0.64f)),
            OcrTextLine("t9", "Witnesses: P. Kimani, E. Wanjiku | Wax Seal Recorded", 0.97f, false, NormalizedRect(0.10f, 0.70f, 0.85f, 0.95f))
        )
    )

    fun getAllPresetSamples(): List<LandRecordDocument> = listOf(
        sample1AgedJamabandi,
        sample2TamperedDeed
    )
}
