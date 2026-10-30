package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.LandRecordDocument
import com.example.ui.screens.AuditDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PdfExportedDialog
import com.example.ui.theme.LandAuditTheme
import com.example.ui.viewmodel.LandAuditViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: LandAuditViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LandAuditTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LandAuditApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun LandAuditApp(viewModel: LandAuditViewModel) {
    val context = LocalContext.current
    val selectedRecord by viewModel.selectedRecord.collectAsState()
    val exportedPdf by viewModel.exportedPdf.collectAsState()

    // Screen Router
    if (selectedRecord == null) {
        HomeScreen(
            viewModel = viewModel,
            onNavigateToDetail = { doc ->
                viewModel.selectRecord(doc)
            },
            onQuickExportPdf = { doc ->
                viewModel.exportVerifiablePdf(context, doc) {
                    // Exported PDF state updated in ViewModel, opening dialog automatically
                }
            }
        )
    } else {
        AuditDetailScreen(
            document = selectedRecord!!,
            viewModel = viewModel,
            onBack = {
                viewModel.clearSelection()
            },
            onExportPdf = { doc ->
                viewModel.exportVerifiablePdf(context, doc) {
                    // Exported dialog will show
                }
            }
        )
    }

    // PDF Success / Share Dialog
    exportedPdf?.let { file ->
        val doc = selectedRecord ?: com.example.engine.SampleDocuments.sample1AgedJamabandi
        PdfExportedDialog(
            pdfFile = file,
            document = doc,
            onDismiss = {
                viewModel.clearExportedPdf()
            }
        )
    }
}
