package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.HrViewModel
import com.example.data.Employee
import com.example.data.Attendance
import com.example.data.Shift
import android.content.Context
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(viewModel: HrViewModel) {
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val shifts by viewModel.shifts.collectAsStateWithLifecycle()
    val dailyAttendance by viewModel.currentAttendance.collectAsStateWithLifecycle()
    val currentDate by viewModel.currentDate.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("الحضور والغياب") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            // Date Selector
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.changeDateBy(-1) }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "السابق")
                }
                Text(text = currentDate, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { viewModel.changeDateBy(1) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "التالي")
                }
            }

            val singleShiftEmps = employees.filter { emp -> emp.shiftIds.split(",").mapNotNull { it.toIntOrNull() }.size <= 1 }
            val multiShiftEmps = employees.filter { emp -> emp.shiftIds.split(",").mapNotNull { it.toIntOrNull() }.size > 1 }

            if (employees.isEmpty()) {
                Text("لا يوجد موظفين حالياً.")
            } else {
                var selectedTab by remember { mutableStateOf(0) }
                val tabs = listOf("شفت واحد", "شفتات متعددة")
                
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (selectedTab == 0) {
                        items(singleShiftEmps) { emp ->
                            val empShiftIds = emp.shiftIds.split(",").mapNotNull { it.toIntOrNull() }
                            val empShifts = shifts.filter { it.id in empShiftIds }
                            
                            EmployeeAttendanceCard(
                                employee = emp,
                                empShifts = empShifts,
                                dailyAttendance = dailyAttendance.filter { it.employeeId == emp.id },
                                onSave = { shiftId, st, inH, inM, inA, outH, outM, outA, notes ->
                                    viewModel.saveAttendance(emp.id, shiftId, st, inH, inM, inA, outH, outM, outA, notes)
                                },
                                onDelete = { record ->
                                    viewModel.deleteAttendance(record)
                                }
                            )
                        }
                    } else {
                        items(multiShiftEmps) { emp ->
                            val empShiftIds = emp.shiftIds.split(",").mapNotNull { it.toIntOrNull() }
                            val empShifts = shifts.filter { it.id in empShiftIds }
                            
                            EmployeeAttendanceCard(
                                employee = emp,
                                empShifts = empShifts,
                                dailyAttendance = dailyAttendance.filter { it.employeeId == emp.id },
                                onSave = { shiftId, st, inH, inM, inA, outH, outM, outA, notes ->
                                    viewModel.saveAttendance(emp.id, shiftId, st, inH, inM, inA, outH, outM, outA, notes)
                                },
                                onDelete = { record ->
                                    viewModel.deleteAttendance(record)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun EmployeeAttendanceCard(
    employee: Employee,
    empShifts: List<Shift>,
    dailyAttendance: List<Attendance>,
    onSave: (Int, String, Int?, Int?, String?, Int?, Int?, String?, String) -> Unit,
    onDelete: (Attendance) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = employee.name, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (empShifts.isEmpty()) {
                    val record = dailyAttendance.find { it.shiftId == -1 }
                    ShiftAttendanceRow(
                        shiftId = -1,
                        shiftName = "بدون شفت",
                        attendanceRecord = record,
                        onSave = { st, inH, inM, inA, outH, outM, outA, notes ->
                            onSave(-1, st, inH, inM, inA, outH, outM, outA, notes)
                        },
                        onDelete = { record?.let { onDelete(it) } }
                    )
                } else {
                    empShifts.forEach { shift ->
                        val record = dailyAttendance.find { it.shiftId == shift.id }
                        ShiftAttendanceRow(
                            shiftId = shift.id,
                            shiftName = shift.name,
                            attendanceRecord = record,
                            onSave = { st, inH, inM, inA, outH, outM, outA, notes ->
                                onSave(shift.id, st, inH, inM, inA, outH, outM, outA, notes)
                            },
                            onDelete = { record?.let { onDelete(it) } }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ShiftAttendanceRow(
    shiftId: Int,
    shiftName: String,
    attendanceRecord: Attendance?,
    onSave: (String, Int?, Int?, String?, Int?, Int?, String?, String) -> Unit,
    onDelete: () -> Unit
) {
    var status by remember(attendanceRecord) { mutableStateOf(attendanceRecord?.status ?: "غياب") }
    var inHr by remember(attendanceRecord) { mutableStateOf(attendanceRecord?.inHour ?: 8) }
    var inMin by remember(attendanceRecord) { mutableStateOf(attendanceRecord?.inMinute ?: 0) }
    var inAmPm by remember(attendanceRecord) { mutableStateOf(attendanceRecord?.inAmPm ?: "ص") }
    var outHr by remember(attendanceRecord) { mutableStateOf(attendanceRecord?.outHour ?: 4) }
    var outMin by remember(attendanceRecord) { mutableStateOf(attendanceRecord?.outMinute ?: 0) }
    var outAmPm by remember(attendanceRecord) { mutableStateOf(attendanceRecord?.outAmPm ?: "م") }
    var notes by remember(attendanceRecord) { mutableStateOf(attendanceRecord?.notes ?: "") }
    
    var showInPicker by remember { mutableStateOf(false) }
    var showOutPicker by remember { mutableStateOf(false) }

    val statuses = listOf("حضور", "غياب", "إجازة", "تأخير")
    val context = androidx.compose.ui.platform.LocalContext.current
    val isSaved = attendanceRecord != null

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSaved) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "شفت: $shiftName", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                if (isSaved) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Text(text = "تم التسجيل", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            if (isSaved && attendanceRecord != null) {
                val stColor = when(attendanceRecord.status) {
                    "حضور" -> Color(0xFF4CAF50)
                    "غياب" -> Color(0xFFF44336)
                    "تأخير" -> Color(0xFFFF9800)
                    else -> Color(0xFF2196F3)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("الحالة:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text(attendanceRecord.status, color = stColor, fontWeight = FontWeight.Bold)
                }
                if (attendanceRecord.status == "حضور" || attendanceRecord.status == "تأخير") {
                    Text("من: ${attendanceRecord.inHour?.toString()?.padStart(2, '0')}:${attendanceRecord.inMinute?.toString()?.padStart(2, '0')} ${attendanceRecord.inAmPm}  إلى: ${attendanceRecord.outHour?.toString()?.padStart(2, '0')}:${attendanceRecord.outMinute?.toString()?.padStart(2, '0')} ${attendanceRecord.outAmPm}", style = MaterialTheme.typography.bodySmall)
                }
                if (attendanceRecord.notes?.isNotBlank() == true) {
                    Text("ملاحظات: ${attendanceRecord.notes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { onDelete() }, modifier = Modifier.height(36.dp), contentPadding = PaddingValues(horizontal = 12.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تعديل / إعادة تعيين")
                    }
                }
            } else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    statuses.forEach { st ->
                        FilterChip(
                            selected = status == st,
                            onClick = { status = st },
                            label = { Text(st) }
                        )
                    }
                }
    
                if (status == "حضور" || status == "تأخير") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("الحضور:", style = MaterialTheme.typography.bodySmall)
                        OutlinedButton(
                            onClick = { showInPicker = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("${inHr.toString().padStart(2, '0')}:${inMin.toString().padStart(2, '0')} $inAmPm")
                        }
                        
                        Spacer(Modifier.width(8.dp))
                        
                        Text("الانصراف:", style = MaterialTheme.typography.bodySmall)
                        OutlinedButton(
                            onClick = { showOutPicker = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("${outHr.toString().padStart(2, '0')}:${outMin.toString().padStart(2, '0')} $outAmPm")
                        }
                    }
                }
    
                if (showInPicker) {
                    TimeInputDialog(
                        initialHr = inHr, initialMin = inMin, initialAmPm = inAmPm,
                        onDismiss = { showInPicker = false },
                        onConfirm = { h, m, a -> inHr = h; inMin = m; inAmPm = a; showInPicker = false }
                    )
                }
                if (showOutPicker) {
                    TimeInputDialog(
                        initialHr = outHr, initialMin = outMin, initialAmPm = outAmPm,
                        onDismiss = { showOutPicker = false },
                        onConfirm = { h, m, a -> outHr = h; outMin = m; outAmPm = a; showOutPicker = false }
                    )
                }
    
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("الملاحظات") },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall
                )
    
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = {
                        val finalInHr = if (status == "حضور" || status == "تأخير") inHr else null
                        val finalInMin = if (status == "حضور" || status == "تأخير") inMin else null
                        val finalInAp = if (status == "حضور" || status == "تأخير") inAmPm else null
                        val finalOutHr = if (status == "حضور" || status == "تأخير") outHr else null
                        val finalOutMin = if (status == "حضور" || status == "تأخير") outMin else null
                        val finalOutAp = if (status == "حضور" || status == "تأخير") outAmPm else null
                        onSave(status, finalInHr, finalInMin, finalInAp, finalOutHr, finalOutMin, finalOutAp, notes)
                        android.widget.Toast.makeText(context, "تم حفظ الحضور", android.widget.Toast.LENGTH_SHORT).show()
                    }) {
                        Text("حفظ الحضور")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeInputDialog(
    initialHr: Int, initialMin: Int, initialAmPm: String,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, String) -> Unit
) {
    val isPm = initialAmPm == "م"
    val initial24Hr = when {
        initialHr == 12 && !isPm -> 0
        initialHr < 12 && isPm -> initialHr + 12
        else -> initialHr
    }
    
    val state = rememberTimePickerState(initialHour = initial24Hr, initialMinute = initialMin, is24Hour = false)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("أدخل الوقت") },
        text = {
            TimeInput(state = state)
        },
        confirmButton = {
            TextButton(onClick = {
                val hourOfDay = state.hour
                val isSelectedPm = hourOfDay >= 12
                val hr12 = when {
                    hourOfDay == 0 -> 12
                    hourOfDay > 12 -> hourOfDay - 12
                    else -> hourOfDay
                }
                val amPmStr = if (isSelectedPm) "م" else "ص"
                onConfirm(hr12, state.minute, amPmStr)
            }) { Text("موافق") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
