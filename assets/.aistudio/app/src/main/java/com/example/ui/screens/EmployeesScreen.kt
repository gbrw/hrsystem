package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.HrViewModel
import com.example.data.Employee

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeesScreen(viewModel: HrViewModel) {
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val shifts by viewModel.shifts.collectAsStateWithLifecycle()

    var showDialog by remember { mutableStateOf(false) }
    var editingEmployee by remember { mutableStateOf<Employee?>(null) }
    var employeeName by remember { mutableStateOf("") }
    var selectedShiftIds by remember { mutableStateOf(setOf<Int>()) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("الموظفين") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingEmployee = null
                employeeName = ""
                selectedShiftIds = emptySet()
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "إضافة موظف")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(employees) { emp ->
                val empShiftIds = emp.shiftIds.split(",").mapNotNull { it.toIntOrNull() }
                val shiftNames = shifts.filter { it.id in empShiftIds }.map { it.name }
                val shiftText = if (shiftNames.isEmpty()) "بدون شفت" else shiftNames.joinToString("، ")
                
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "الاسم: ${emp.name}", style = MaterialTheme.typography.titleMedium)
                            Text(text = "الشفتات: $shiftText", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row {
                            IconButton(onClick = {
                                editingEmployee = emp
                                employeeName = emp.name
                                selectedShiftIds = empShiftIds.toSet()
                                showDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { viewModel.deleteEmployee(emp) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(if (editingEmployee == null) "إضافة موظف" else "تعديل الموظف") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        OutlinedTextField(
                            value = employeeName,
                            onValueChange = { employeeName = it },
                            label = { Text("اسم الموظف") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("تعيين الشفتات:", style = MaterialTheme.typography.labelLarge)
                        if (shifts.isEmpty()) {
                            Text("لا توجد شفتات مضافة بعد.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        } else {
                            shifts.forEach { shift ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Checkbox(
                                        checked = selectedShiftIds.contains(shift.id),
                                        onCheckedChange = { checked ->
                                            if (checked) {
                                                selectedShiftIds = selectedShiftIds + shift.id
                                            } else {
                                                selectedShiftIds = selectedShiftIds - shift.id
                                            }
                                        }
                                    )
                                    Text(text = shift.name)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (employeeName.isNotBlank()) {
                            if (editingEmployee == null) {
                                viewModel.addEmployee(employeeName, selectedShiftIds.toList())
                            } else {
                                viewModel.updateEmployee(editingEmployee!!, employeeName, selectedShiftIds.toList())
                            }
                            showDialog = false
                        }
                    }) { Text("حفظ") }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("إلغاء") }
                }
            )
        }
    }
}
