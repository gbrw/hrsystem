package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.HrViewModel
import com.example.data.Shift

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftsScreen(viewModel: HrViewModel) {
    val shifts by viewModel.shifts.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var shiftToEdit by remember { mutableStateOf<Shift?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("إدارة الشفتات") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { shiftToEdit = null; showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "إضافة شفت")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(shifts) { shift ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "اسم الشفت: ${shift.name}", style = MaterialTheme.typography.titleMedium)
                            Text(text = "البداية: ${shift.startHour}:${shift.startMinute.toString().padStart(2, '0')} ${shift.startAmPm}", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "النهاية: ${shift.endHour}:${shift.endMinute.toString().padStart(2, '0')} ${shift.endAmPm}", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "استراحة: ${shift.breakMinutes} دقيقة", style = MaterialTheme.typography.bodySmall)
                        }
                        Row {
                            IconButton(onClick = { shiftToEdit = shift; showDialog = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { viewModel.deleteShift(shift) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AddShiftDialog(
                initialShift = shiftToEdit,
                onDismiss = { showDialog = false },
                onAdd = { name, sh, sm, sa, eh, em, ea, bm ->
                    if (shiftToEdit != null) {
                        viewModel.updateShift(shiftToEdit!!.copy(name = name, startHour = sh, startMinute = sm, startAmPm = sa, endHour = eh, endMinute = em, endAmPm = ea, breakMinutes = bm))
                    } else {
                        viewModel.addShift(name, sh, sm, sa, eh, em, ea, bm)
                    }
                    showDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddShiftDialog(
    initialShift: Shift? = null,
    onDismiss: () -> Unit,
    onAdd: (String, Int, Int, String, Int, Int, String, Int) -> Unit
) {
    var name by remember { mutableStateOf(initialShift?.name ?: "") }
    var breakMins by remember { mutableStateOf(initialShift?.breakMinutes?.toString() ?: "0") }

    var sh by remember { mutableStateOf(initialShift?.startHour ?: 8) }
    var sm by remember { mutableStateOf(initialShift?.startMinute ?: 0) }
    var sa by remember { mutableStateOf(initialShift?.startAmPm ?: "ص") }

    var eh by remember { mutableStateOf(initialShift?.endHour ?: 4) }
    var em by remember { mutableStateOf(initialShift?.endMinute ?: 0) }
    var ea by remember { mutableStateOf(initialShift?.endAmPm ?: "م") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        title = { Text(if (initialShift == null) "إضافة شفت" else "تعديل شفت") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الشفت") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("البداية:", style = MaterialTheme.typography.labelLarge)
                    var showStartPicker by remember { mutableStateOf(false) }
                    OutlinedButton(onClick = { showStartPicker = true }) {
                        Text("${sh.toString().padStart(2, '0')}:${sm.toString().padStart(2, '0')} $sa")
                    }
                    if (showStartPicker) {
                        TimeInputDialog(
                            initialHr = sh, initialMin = sm, initialAmPm = sa,
                            onDismiss = { showStartPicker = false },
                            onConfirm = { h, m, a -> sh = h; sm = m; sa = a; showStartPicker = false }
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("النهاية:", style = MaterialTheme.typography.labelLarge)
                    var showEndPicker by remember { mutableStateOf(false) }
                    OutlinedButton(onClick = { showEndPicker = true }) {
                        Text("${eh.toString().padStart(2, '0')}:${em.toString().padStart(2, '0')} $ea")
                    }
                    if (showEndPicker) {
                        TimeInputDialog(
                            initialHr = eh, initialMin = em, initialAmPm = ea,
                            onDismiss = { showEndPicker = false },
                            onConfirm = { h, m, a -> eh = h; em = m; ea = a; showEndPicker = false }
                        )
                    }
                }

                OutlinedTextField(
                    value = breakMins,
                    onValueChange = { breakMins = it },
                    label = { Text("مدة الاستراحة (بالدقائق)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val bm = breakMins.toIntOrNull() ?: 0
                if (name.isNotBlank()) {
                    onAdd(name, sh, sm, sa, eh, em, ea, bm)
                }
            }) { Text("حفظ") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}


