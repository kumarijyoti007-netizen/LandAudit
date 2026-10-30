package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "land_records")
data class LandRecordEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val documentType: String,
    val sourceType: String,
    val imagePath: String,
    val dateScanned: Long,
    val status: String,
    val tamperScore: Int,
    val documentHash: String,
    val verificationToken: String,
    val ownerName: String,
    val surveyNumber: String,
    val totalAreaText: String,
    val village: String,
    val district: String,
    val anomaliesCount: Int,
    val extractedJson: String,
    val anomaliesJson: String,
    val ocrJson: String,
    val pdfExportPath: String? = null
)
