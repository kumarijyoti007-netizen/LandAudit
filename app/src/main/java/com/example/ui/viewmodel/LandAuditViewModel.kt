package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.LandRecordEntity
import com.example.data.model.AuditStatus
import com.example.data.model.DocumentSourceType
import com.example.data.model.ExtractedLandInfo
import com.example.data.model.LandBoundaries
import com.example.data.model.LandRecordDocument
import com.example.data.model.NormalizedRect
import com.example.data.model.OcrTextLine
import com.example.data.model.TamperAnomaly
import com.example.engine.LandRecordAnalyzer
import com.example.engine.PdfReportGenerator
import com.example.engine.SampleDocuments
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

enum class AuditFilter {
    ALL,
    VERIFIED_ONLY,
    TAMPERED_ONLY
}

enum class DocumentViewMode {
    ORIGINAL,
    TAMPER_HIGHLIGHT,
    ENHANCED_CONTRAST
}

class LandAuditViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.landRecordDao()

    private val _userRecords = MutableStateFlow<List<LandRecordDocument>>(emptyList())
    private val _selectedRecord = MutableStateFlow<LandRecordDocument?>(null)
    val selectedRecord: StateFlow<LandRecordDocument?> = _selectedRecord.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _analysisStep = MutableStateFlow("")
    val analysisStep: StateFlow<String> = _analysisStep.asStateFlow()

    private val _analysisProgress = MutableStateFlow(0f)
    val analysisProgress: StateFlow<Float> = _analysisProgress.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(AuditFilter.ALL)
    val selectedFilter: StateFlow<AuditFilter> = _selectedFilter.asStateFlow()

    private val _viewMode = MutableStateFlow(DocumentViewMode.TAMPER_HIGHLIGHT)
    val viewMode: StateFlow<DocumentViewMode> = _viewMode.asStateFlow()

    private val _exportedPdf = MutableStateFlow<File?>(null)
    val exportedPdf: StateFlow<File?> = _exportedPdf.asStateFlow()

    // Combined list of presets and saved user records
    val allRecords: StateFlow<List<LandRecordDocument>> = combine(
        _userRecords,
        _searchQuery,
        _selectedFilter
    ) { userDocs, query, filter ->
        val presetDocs = SampleDocuments.getAllPresetSamples()
        val combined = presetDocs + userDocs
        combined.filter { doc ->
            val matchesQuery = query.isBlank() ||
                    doc.title.contains(query, ignoreCase = true) ||
                    doc.extractedData.ownerName.contains(query, ignoreCase = true) ||
                    doc.extractedData.surveyNumber.contains(query, ignoreCase = true) ||
                    doc.extractedData.village.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                AuditFilter.ALL -> true
                AuditFilter.VERIFIED_ONLY -> doc.status == AuditStatus.VERIFIED_GENUINE
                AuditFilter.TAMPERED_ONLY -> doc.status == AuditStatus.HIGH_RISK_FORGERY || doc.status == AuditStatus.SUSPECTED_TAMPERING
            }
            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SampleDocuments.getAllPresetSamples())

    init {
        loadPersistedRecords()
    }

    private fun loadPersistedRecords() {
        viewModelScope.launch {
            dao.getAllRecords().collect { entities ->
                val docs = entities.map { deserializeEntity(it) }
                _userRecords.value = docs
            }
        }
    }

    fun selectRecord(doc: LandRecordDocument) {
        _selectedRecord.value = doc
    }

    fun clearSelection() {
        _selectedRecord.value = null
    }

    fun setFilter(filter: AuditFilter) {
        _selectedFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setViewMode(mode: DocumentViewMode) {
        _viewMode.value = mode
    }

    fun clearExportedPdf() {
        _exportedPdf.value = null
    }

    fun analyzeScannedImage(context: Context, uri: Uri, onCompleted: (LandRecordDocument) -> Unit) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _analysisProgress.value = 0.05f
            _analysisStep.value = "Initializing Optical Inspection..."

            try {
                val document = LandRecordAnalyzer.analyzeDocument(context, uri) { step, progress ->
                    _analysisStep.value = step
                    _analysisProgress.value = progress
                }

                // Persist to Room Database
                val entity = serializeDocument(document)
                dao.insertRecord(entity)

                _selectedRecord.value = document
                _isAnalyzing.value = false
                onCompleted(document)
            } catch (e: Exception) {
                _isAnalyzing.value = false
                _analysisStep.value = "Analysis failed: ${e.localizedMessage}"
            }
        }
    }

    fun exportVerifiablePdf(context: Context, document: LandRecordDocument, onReady: (File) -> Unit) {
        viewModelScope.launch {
            val file = PdfReportGenerator.generateAuditPdf(context, document)
            _exportedPdf.value = file
            onReady(file)
        }
    }

    fun deleteRecord(id: String) {
        viewModelScope.launch {
            dao.deleteById(id)
            if (_selectedRecord.value?.id == id) {
                _selectedRecord.value = null
            }
        }
    }

    private fun serializeDocument(doc: LandRecordDocument): LandRecordEntity {
        val extractedObj = JSONObject().apply {
            put("ownerName", doc.extractedData.ownerName)
            put("coOwners", JSONArray(doc.extractedData.coOwners))
            put("surveyNumber", doc.extractedData.surveyNumber)
            put("subdivisionNumber", doc.extractedData.subdivisionNumber)
            put("khataNumber", doc.extractedData.khataNumber)
            put("village", doc.extractedData.village)
            put("talukOrTehsil", doc.extractedData.talukOrTehsil)
            put("district", doc.extractedData.district)
            put("state", doc.extractedData.state)
            put("totalAreaText", doc.extractedData.totalAreaText)
            put("totalAreaAcre", doc.extractedData.totalAreaAcre)
            put("landClassification", doc.extractedData.landClassification)
            put("registrationDate", doc.extractedData.registrationDate)
            put("stampDutyAmount", doc.extractedData.stampDutyAmount)
            put("subRegistrarOffice", doc.extractedData.subRegistrarOffice)
            put("north", doc.extractedData.boundaries.north)
            put("south", doc.extractedData.boundaries.south)
            put("east", doc.extractedData.boundaries.east)
            put("west", doc.extractedData.boundaries.west)
            put("encumbranceNotes", doc.extractedData.encumbranceNotes)
            put("overallOcrConfidence", doc.extractedData.overallOcrConfidence.toDouble())
        }

        val anomArray = JSONArray()
        for (anom in doc.anomalies) {
            val aObj = JSONObject().apply {
                put("id", anom.id)
                put("field", anom.field)
                put("type", anom.type.name)
                put("severity", anom.severity.name)
                put("title", anom.title)
                put("description", anom.description)
                put("detectedValue", anom.detectedValue)
                put("originalSuspicion", anom.originalSuspicion ?: "")
                put("left", anom.bounds.left.toDouble())
                put("top", anom.bounds.top.toDouble())
                put("right", anom.bounds.right.toDouble())
                put("bottom", anom.bounds.bottom.toDouble())
                put("confidence", anom.confidence.toDouble())
            }
            anomArray.put(aObj)
        }

        val ocrArray = JSONArray()
        for (line in doc.ocrLines) {
            val lObj = JSONObject().apply {
                put("id", line.id)
                put("text", line.text)
                put("confidence", line.confidence.toDouble())
                put("isFlagged", line.isFlagged)
                put("left", line.bounds.left.toDouble())
                put("top", line.bounds.top.toDouble())
                put("right", line.bounds.right.toDouble())
                put("bottom", line.bounds.bottom.toDouble())
            }
            ocrArray.put(lObj)
        }

        return LandRecordEntity(
            id = doc.id,
            title = doc.title,
            documentType = doc.documentType,
            sourceType = doc.sourceType.name,
            imagePath = doc.imagePathOrRes,
            dateScanned = doc.dateScanned,
            status = doc.status.name,
            tamperScore = doc.tamperScore,
            documentHash = doc.documentHash,
            verificationToken = doc.verificationToken,
            ownerName = doc.extractedData.ownerName,
            surveyNumber = doc.extractedData.surveyNumber,
            totalAreaText = doc.extractedData.totalAreaText,
            village = doc.extractedData.village,
            district = doc.extractedData.district,
            anomaliesCount = doc.anomalies.size,
            extractedJson = extractedObj.toString(),
            anomaliesJson = anomArray.toString(),
            ocrJson = ocrArray.toString(),
            pdfExportPath = doc.pdfExportPath
        )
    }

    private fun deserializeEntity(entity: LandRecordEntity): LandRecordDocument {
        val extObj = JSONObject(entity.extractedJson)
        val coOwners = mutableListOf<String>()
        val coOwnersArray = extObj.optJSONArray("coOwners")
        if (coOwnersArray != null) {
            for (i in 0 until coOwnersArray.length()) {
                coOwners.add(coOwnersArray.getString(i))
            }
        }

        val extracted = ExtractedLandInfo(
            ownerName = extObj.optString("ownerName", entity.ownerName),
            coOwners = coOwners,
            surveyNumber = extObj.optString("surveyNumber", entity.surveyNumber),
            subdivisionNumber = extObj.optString("subdivisionNumber", ""),
            khataNumber = extObj.optString("khataNumber", ""),
            village = extObj.optString("village", entity.village),
            talukOrTehsil = extObj.optString("talukOrTehsil", ""),
            district = extObj.optString("district", entity.district),
            state = extObj.optString("state", "Revenue Dept"),
            totalAreaText = extObj.optString("totalAreaText", entity.totalAreaText),
            totalAreaAcre = extObj.optDouble("totalAreaAcre", 0.0),
            landClassification = extObj.optString("landClassification", "Agricultural"),
            registrationDate = extObj.optString("registrationDate", "Recorded"),
            stampDutyAmount = extObj.optString("stampDutyAmount", "Verified"),
            subRegistrarOffice = extObj.optString("subRegistrarOffice", "Sub-Registrar"),
            boundaries = LandBoundaries(
                north = extObj.optString("north", "Border Plot"),
                south = extObj.optString("south", "Public Road"),
                east = extObj.optString("east", "Adjacent Survey"),
                west = extObj.optString("west", "Village Boundary")
            ),
            encumbranceNotes = extObj.optString("encumbranceNotes", "Nil"),
            overallOcrConfidence = extObj.optDouble("overallOcrConfidence", 0.92).toFloat()
        )

        val anomalies = mutableListOf<TamperAnomaly>()
        val anomArray = JSONArray(entity.anomaliesJson)
        for (i in 0 until anomArray.length()) {
            val a = anomArray.getJSONObject(i)
            anomalies.add(
                TamperAnomaly(
                    id = a.getString("id"),
                    field = a.getString("field"),
                    type = try { com.example.data.model.AnomalyType.valueOf(a.getString("type")) } catch (e: Exception) { com.example.data.model.AnomalyType.DIGITAL_SPLICING },
                    severity = try { com.example.data.model.AnomalySeverity.valueOf(a.getString("severity")) } catch (e: Exception) { com.example.data.model.AnomalySeverity.MEDIUM },
                    title = a.getString("title"),
                    description = a.getString("description"),
                    detectedValue = a.getString("detectedValue"),
                    originalSuspicion = a.optString("originalSuspicion").ifEmpty { null },
                    bounds = NormalizedRect(
                        a.getDouble("left").toFloat(),
                        a.getDouble("top").toFloat(),
                        a.getDouble("right").toFloat(),
                        a.getDouble("bottom").toFloat()
                    ),
                    confidence = a.getDouble("confidence").toFloat()
                )
            )
        }

        val lines = mutableListOf<OcrTextLine>()
        val ocrArray = JSONArray(entity.ocrJson)
        for (i in 0 until ocrArray.length()) {
            val l = ocrArray.getJSONObject(i)
            lines.add(
                OcrTextLine(
                    id = l.getString("id"),
                    text = l.getString("text"),
                    confidence = l.getDouble("confidence").toFloat(),
                    isFlagged = l.getBoolean("isFlagged"),
                    bounds = NormalizedRect(
                        l.getDouble("left").toFloat(),
                        l.getDouble("top").toFloat(),
                        l.getDouble("right").toFloat(),
                        l.getDouble("bottom").toFloat()
                    )
                )
            )
        }

        return LandRecordDocument(
            id = entity.id,
            title = entity.title,
            documentType = entity.documentType,
            sourceType = try { DocumentSourceType.valueOf(entity.sourceType) } catch (e: Exception) { DocumentSourceType.USER_SCANNED },
            imagePathOrRes = entity.imagePath,
            dateScanned = entity.dateScanned,
            status = try { AuditStatus.valueOf(entity.status) } catch (e: Exception) { AuditStatus.VERIFIED_GENUINE },
            tamperScore = entity.tamperScore,
            documentHash = entity.documentHash,
            verificationToken = entity.verificationToken,
            extractedData = extracted,
            anomalies = anomalies,
            ocrLines = lines,
            pdfExportPath = entity.pdfExportPath
        )
    }
}
