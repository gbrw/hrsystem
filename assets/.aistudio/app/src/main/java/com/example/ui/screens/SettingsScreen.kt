package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.ui.viewmodel.HrViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: HrViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var companyName by remember(settings) { mutableStateOf(settings?.companyName ?: "") }
    var logoUri by remember(settings) { mutableStateOf(settings?.logoUri) }
    
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                logoUri = it.toString()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.CreateDocument("text/csv")) { uri: Uri? ->
        uri?.let { viewModel.exportDataToCsv(context, it) }
    }
    
    val importLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let { viewModel.importDataFromCsv(context, it) }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("الإعدادات") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("معلومات الشركة", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        label = { Text("اسم الشركة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(onClick = { launcher.launch(arrayOf("image/*")) }) {
                            Text("اختيار شعار")
                        }
                        if (logoUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(model = logoUri),
                                contentDescription = "الشعار",
                                modifier = Modifier.size(64.dp),
                                contentScale = ContentScale.Fit
                            )
                            TextButton(onClick = { logoUri = null }) {
                                Text("إزالة", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    Button(
                        onClick = { 
                            viewModel.updateSettings(companyName, logoUri)
                            android.widget.Toast.makeText(context, "تم الحفظ", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("حفظ التغييرات")
                    }
                }
            }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("إدارة البيانات", style = MaterialTheme.typography.titleMedium)
                    Button(onClick = { exportLauncher.launch("hr_backup.csv") }, modifier = Modifier.fillMaxWidth()) { Text("تصدير البيانات (النسخ الاحتياطي)") }
                    Button(onClick = { importLauncher.launch(arrayOf("text/csv", "application/vnd.ms-excel", "*/*")) }, modifier = Modifier.fillMaxWidth()) { Text("استيراد البيانات") }
                    Button(onClick = { android.widget.Toast.makeText(context, "هذه الميزة قيد التطوير (قريباً)", android.widget.Toast.LENGTH_SHORT).show() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("أرشفة سنوية") }
                }
            }
        }
    }
}

