package com.example.ui.screens

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.HrViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: HrViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val currentDateText by viewModel.currentDate.collectAsStateWithLifecycle()
    val currentTimeText = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date())
    
    val currentPresent by viewModel.currentPresentCount.collectAsStateWithLifecycle()
    val currentAbsent by viewModel.currentAbsentCount.collectAsStateWithLifecycle()
    val currentLeave by viewModel.currentLeaveCount.collectAsStateWithLifecycle()
    
    val allAttendance by viewModel.allAttendance.collectAsStateWithLifecycle()
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val shifts by viewModel.shifts.collectAsStateWithLifecycle()
    
    var selectedReportType by remember { mutableStateOf("يومي") } // يومي، شهري، سنوي
    val context = LocalContext.current
    
    Scaffold(
        topBar = { TopAppBar(title = { Text("التقارير") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { 
                    val html = generateHtmlReport(
                        reportType = selectedReportType,
                        companyName = settings?.companyName ?: "اسم الشركة",
                        logoUri = settings?.logoUri,
                        date = currentDateText,
                        emps = employees,
                        shifts = shifts,
                        allAtt = allAttendance
                    )
                    printReport(context, html)
                },
                icon = { Icon(Icons.Default.Print, "طباعة") },
                text = { Text("طباعة الكشف") }
            )
        }
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
                Text(text = currentDateText, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { viewModel.changeDateBy(1) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "التالي")
                }
            }
            
            Text("اختر نوع التقرير:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("يومي", "شهري", "سنوي").forEach { type ->
                    FilterChip(
                        selected = selectedReportType == type,
                        onClick = { selectedReportType = type },
                        label = { Text(type) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (selectedReportType == "يومي") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("أداء اليوم:", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("- الحضور: $currentPresent", style = MaterialTheme.typography.bodyLarge)
                            Text("- الغياب المسجل: $currentAbsent", style = MaterialTheme.typography.bodyLarge)
                            Text("- الإجازات: $currentLeave", style = MaterialTheme.typography.bodyLarge)
                        }
                    }

                    Text("غياب الشفتات:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    shifts.forEach { shift ->
                        val shiftEmpIds = employees.filter { emp -> 
                            val empShiftIds = emp.shiftIds.split(",").mapNotNull { it.toIntOrNull() }
                            empShiftIds.contains(shift.id)
                        }.map { it.id }
                        
                        val absentEmpNames = employees.filter { emp ->
                            val record = allAttendance.find { it.employeeId == emp.id && it.shiftId == shift.id && it.date == currentDateText }
                            val isAbsent = record == null || record.status == "غياب"
                            shiftEmpIds.contains(emp.id) && isAbsent
                        }.map { it.name }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "شفت: ${shift.name}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                                if (absentEmpNames.isEmpty()) {
                                    Text("الكل حاضر", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                } else {
                                    absentEmpNames.forEach { name ->
                                        Text("- $name", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    val prefix = if (selectedReportType == "شهري") currentDateText.substring(0, 7) else currentDateText.substring(0, 4)
                    val relevantAtt = allAttendance.filter { it.date.startsWith(prefix) }
                    
                    val totalP = relevantAtt.count { it.status == "حضور" || it.status == "تأخير" }
                    val totalA = relevantAtt.count { it.status == "غياب" }
                    val totalL = relevantAtt.count { it.status == "إجازة" }
                    
                    MonthlyStatsChart(present = totalP, absent = totalA, leave = totalL)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    employees.forEach { emp ->
                        val empAtt = relevantAtt.filter { it.employeeId == emp.id }
                        val p = empAtt.count { it.status == "حضور" || it.status == "تأخير" }
                        val a = empAtt.count { it.status == "غياب" }
                        val l = empAtt.count { it.status == "إجازة" }
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = emp.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("إجمالي الحضور/التأخير: $p", style = MaterialTheme.typography.bodyMedium)
                                Text("إجمالي الغياب: $a", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                                Text("الإجازات: $l", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

fun generateHtmlReport(
    reportType: String,
    companyName: String,
    logoUri: String?,
    date: String,
    emps: List<com.example.data.Employee>,
    shifts: List<com.example.data.Shift>,
    allAtt: List<com.example.data.Attendance>
): String {
    val prefix = when(reportType) {
        "يومي" -> date
        "شهري" -> date.substring(0, 7)
        "سنوي" -> date.substring(0, 4)
        else -> date
    }
    
    val relevantAtt = allAtt.filter { it.date.startsWith(prefix) }
    
    var rows = ""
    
    if (reportType == "يومي") {
        emps.forEach { emp ->
            val empAtts = relevantAtt.filter { it.employeeId == emp.id }
            if (empAtts.isEmpty()) {
                val shiftNames = emp.shiftIds.split(",").mapNotNull { id -> shifts.find { it.id.toString() == id }?.name }.joinToString(" و ")
                rows += """
                    <tr>
                        <td style="padding: 12px; border: 1px solid #e0e0e0; font-weight: bold;">${emp.name}</td>
                        <td style="padding: 12px; border: 1px solid #e0e0e0;">${if (shiftNames.isEmpty()) "بدون شفت" else shiftNames}</td>
                        <td style="padding: 12px; border: 1px solid #e0e0e0; color: #d32f2f;">غياب (غير مسجل)</td>
                        <td style="padding: 12px; border: 1px solid #e0e0e0;">-</td>
                        <td style="padding: 12px; border: 1px solid #e0e0e0;">-</td>
                        <td style="padding: 12px; border: 1px solid #e0e0e0;">-</td>
                    </tr>
                """
            } else {
                empAtts.forEach { att ->
                    val shiftName = shifts.find { it.id == att.shiftId }?.name ?: "بدون شفت"
                    val inTime = if (att.inHour != null) "${att.inHour.toString().padStart(2, '0')}:${att.inMinute.toString().padStart(2, '0')} ${att.inAmPm}" else "-"
                    val outTime = if (att.outHour != null) "${att.outHour.toString().padStart(2, '0')}:${att.outMinute.toString().padStart(2, '0')} ${att.outAmPm}" else "-"
                    
                    val statusColor = when (att.status) {
                        "حضور" -> "#388e3c"
                        "تأخير" -> "#f57c00"
                        "إجازة" -> "#1976d2"
                        else -> "#d32f2f"
                    }

                    rows += """
                        <tr>
                            <td style="padding: 12px; border: 1px solid #e0e0e0; font-weight: bold;">${emp.name}</td>
                            <td style="padding: 12px; border: 1px solid #e0e0e0;">$shiftName</td>
                            <td style="padding: 12px; border: 1px solid #e0e0e0; color: $statusColor;">${att.status}</td>
                            <td style="padding: 12px; border: 1px solid #e0e0e0;">$inTime</td>
                            <td style="padding: 12px; border: 1px solid #e0e0e0;">$outTime</td>
                            <td style="padding: 12px; border: 1px solid #e0e0e0;">${att.notes}</td>
                        </tr>
                    """
                }
            }
        }
    } else {
        emps.forEach { emp ->
            val empAtt = relevantAtt.filter { it.employeeId == emp.id }
            val p = empAtt.count { it.status == "حضور" }
            val late = empAtt.count { it.status == "تأخير" }
            val a = empAtt.count { it.status == "غياب" }
            val l = empAtt.count { it.status == "إجازة" }
            rows += """
                <tr>
                    <td style="padding: 12px; border: 1px solid #e0e0e0; font-weight: bold;">${emp.name}</td>
                    <td style="padding: 12px; border: 1px solid #e0e0e0;">$p</td>
                    <td style="padding: 12px; border: 1px solid #e0e0e0;">$late</td>
                    <td style="padding: 12px; border: 1px solid #e0e0e0; color: #d32f2f;">$a</td>
                    <td style="padding: 12px; border: 1px solid #e0e0e0;">$l</td>
                </tr>
            """
        }
    }

    val imgTag = if (logoUri != null) "<img src='$logoUri' style='max-height: 100px; display: block; margin: 0 auto 10px;' />" else ""

    val tableHeader = if (reportType == "يومي") {
        """
        <tr style="background-color: #f5f5f5; color: #333;">
            <th style="padding: 12px; border: 1px solid #e0e0e0;">اسم الموظف</th>
            <th style="padding: 12px; border: 1px solid #e0e0e0;">الشفت</th>
            <th style="padding: 12px; border: 1px solid #e0e0e0;">الحالة</th>
            <th style="padding: 12px; border: 1px solid #e0e0e0;">وقت الحضور</th>
            <th style="padding: 12px; border: 1px solid #e0e0e0;">وقت الانصراف</th>
            <th style="padding: 12px; border: 1px solid #e0e0e0;">ملاحظات</th>
        </tr>
        """
    } else {
        """
        <tr style="background-color: #f5f5f5; color: #333;">
            <th style="padding: 12px; border: 1px solid #e0e0e0;">اسم الموظف</th>
            <th style="padding: 12px; border: 1px solid #e0e0e0;">أيام الحضور</th>
            <th style="padding: 12px; border: 1px solid #e0e0e0;">أيام التأخير</th>
            <th style="padding: 12px; border: 1px solid #e0e0e0;">أيام الغياب</th>
            <th style="padding: 12px; border: 1px solid #e0e0e0;">أيام الإجازة</th>
        </tr>
        """
    }

    return """
        <!DOCTYPE html>
        <html dir="rtl" lang="ar">
        <head>
            <meta charset="utf-8">
            <title>تقرير $reportType</title>
            <style>
                body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; text-align: right; padding: 40px; color: #333; }
                table { width: 100%; border-collapse: collapse; text-align: center; margin-top: 20px; font-size: 14px; page-break-inside: auto; }
                tr { page-break-inside: avoid; page-break-after: auto; }
                thead { display: table-header-group; }
                tfoot { display: table-footer-group; }
                h1 { margin: 0 0 10px 0; color: #1a237e; font-size: 28px; }
                h2 { margin: 0 0 5px 0; color: #555; font-size: 20px; }
                p { margin: 0 0 20px 0; color: #777; font-size: 16px; }
                .header-container { text-align: center; margin-bottom: 30px; border-bottom: 2px solid #3f51b5; padding-bottom: 20px; }
                @media print {
                    @page { size: A4; margin: 15mm; }
                    body { padding: 0; background: white; -webkit-print-color-adjust: exact; }
                    .header-container { border-bottom: 2px solid #3f51b5; }
                    table, th, td { border: 1px solid #ccc !important; }
                    th { font-weight: bold !important; background-color: #f5f5f5 !important; color: #333 !important; }
                    * { color: black !important; }
                }
            </style>
        </head>
        <body>
            <div class="header-container">
                $imgTag
                <h1>$companyName</h1>
                <h2>كشف الحضور والغياب ($reportType)</h2>
                <p>الفترة: $prefix | تاريخ الطباعة: $date</p>
            </div>
            <table>
                <thead>
                    $tableHeader
                </thead>
                <tbody>
                    $rows
                </tbody>
            </table>
        </body>
        </html>
    """.trimIndent()
}

@Composable
fun MonthlyStatsChart(present: Int, absent: Int, leave: Int) {
    val total = present + absent + leave
    if (total == 0) return
    
    val presentAngle = (present.toFloat() / total) * 360f
    val absentAngle = (absent.toFloat() / total) * 360f
    val leaveAngle = (leave.toFloat() / total) * 360f
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("نظرة عامة (مرئي)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    var startAngle = -90f
                    
                    if (presentAngle > 0) {
                        drawArc(
                            color = Color(0xFF4CAF50),
                            startAngle = startAngle,
                            sweepAngle = presentAngle,
                            useCenter = false,
                            style = Stroke(width = 40f, cap = StrokeCap.Round)
                        )
                        startAngle += presentAngle
                    }
                    if (absentAngle > 0) {
                        drawArc(
                            color = Color(0xFFF44336),
                            startAngle = startAngle,
                            sweepAngle = absentAngle,
                            useCenter = false,
                            style = Stroke(width = 40f, cap = StrokeCap.Round)
                        )
                        startAngle += absentAngle
                    }
                    if (leaveAngle > 0) {
                        drawArc(
                            color = Color(0xFF2196F3),
                            startAngle = startAngle,
                            sweepAngle = leaveAngle,
                            useCenter = false,
                            style = Stroke(width = 40f, cap = StrokeCap.Round)
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "$total", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(text = "الإجمالي", style = MaterialTheme.typography.bodyMedium)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                LegendItem(color = Color(0xFF4CAF50), text = "حضور ($present)")
                LegendItem(color = Color(0xFFF44336), text = "غياب ($absent)")
                LegendItem(color = Color(0xFF2196F3), text = "إجازة ($leave)")
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).background(color, shape = CircleShape))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, style = MaterialTheme.typography.bodySmall)
    }
}

fun printReport(context: Context, htmlContent: String) {
    val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
    val webView = WebView(context)
    webView.loadDataWithBaseURL(null, htmlContent, "text/HTML", "UTF-8", null)
    
    // Create print adapter
    val printAdapter = webView.createPrintDocumentAdapter("كشف_الغيابات")
    printManager.print("التقرير", printAdapter, PrintAttributes.Builder().build())
}
