package com.example

import com.example.data.model.AnomalySeverity
import com.example.data.model.AuditStatus
import com.example.engine.SampleDocuments
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun sample1_jamabandi_hasValidMetadataAndAuthenticStatus() {
        val sample = SampleDocuments.sample1AgedJamabandi
        assertEquals("doc-jamabandi-1968", sample.id)
        assertEquals("Shivram Bapurao Patil", sample.extractedData.ownerName)
        assertEquals("Gut No. 112 (Survey 45/301)", sample.extractedData.surveyNumber)
        assertEquals("Dhamangaon", sample.extractedData.village)
        assertEquals("Pune", sample.extractedData.district)
        assertEquals(AuditStatus.VERIFIED_GENUINE, sample.status)
        assertTrue("Tamper score should be low for authentic record", sample.tamperScore < 15)
        assertEquals(64, sample.documentHash.length) // SHA-256 length
        assertTrue("Verification token should be present", sample.verificationToken.isNotEmpty())
    }

    @Test
    fun sample2_tamperedDeed_flagsCriticalForgeryAnomalies() {
        val sample = SampleDocuments.sample2TamperedDeed
        assertEquals("doc-tampered-deed-1956", sample.id)
        assertEquals("David K. Otieno (Purchaser)", sample.extractedData.ownerName)
        assertEquals(AuditStatus.HIGH_RISK_FORGERY, sample.status)
        assertTrue("Tamper score should be high for forged record", sample.tamperScore > 80)

        // Verify that critical stroke overwrite is detected
        val criticalAnomaly = sample.anomalies.firstOrNull { it.severity == AnomalySeverity.CRITICAL }
        assertNotNull("Should detect critical severity anomaly", criticalAnomaly)
        assertTrue("Should detect acreage alteration", criticalAnomaly!!.detectedValue.contains("38"))
        assertEquals("Total Acreage Clause", criticalAnomaly.field)

        // Verify OCR lines flagged
        val flaggedLines = sample.ocrLines.filter { it.isFlagged }
        assertTrue("Should have flagged OCR lines", flaggedLines.isNotEmpty())
    }

    @Test
    fun allPresetSamples_containNonEmptyBoundaries() {
        val samples = SampleDocuments.getAllPresetSamples()
        for (sample in samples) {
            val b = sample.extractedData.boundaries
            assertTrue(b.north.isNotEmpty())
            assertTrue(b.south.isNotEmpty())
            assertTrue(b.east.isNotEmpty())
            assertTrue(b.west.isNotEmpty())
        }
    }
}
