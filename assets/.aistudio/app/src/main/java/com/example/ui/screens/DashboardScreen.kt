package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.HrViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: HrViewModel) {
    val totalEmp by viewModel.employeeCount.collectAsStateWithLifecycle()
    val present by viewModel.currentPresentCount.collectAsStateWithLifecycle()
    val absent by viewModel.currentAbsentCount.collectAsStateWithLifecycle()
    val leave by viewModel.currentLeaveCount.collectAsStateWithLifecycle()
    val currentDate by viewModel.currentDate.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("لوحة التحكم", fontWeight = FontWeight.Bold) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Date Selector
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.changeDateBy(-1) }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "السابق")
                }
                Text(
                    text = "إحصائيات اليوم: $currentDate",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
                IconButton(onClick = { viewModel.changeDateBy(1) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "التالي")
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                item { StatCard("إجمالي الموظفين", totalEmp.toString(), Icons.Default.People, StatTotalBg, StatTotalText, StatTotalBorder) }
                item { StatCard("الحضور", present.toString(), Icons.Default.CheckCircle, StatPresentBg, StatPresentText, StatPresentBorder) }
                item { StatCard("الغياب", absent.toString(), Icons.Default.Block, StatAbsentBg, StatAbsentText, StatAbsentBorder) }
                item { StatCard("إجازة", leave.toString(), Icons.Default.Luggage, StatLeaveBg, StatLeaveText, StatLeaveBorder) }
            }
            
            val topCommitted by viewModel.topCommittedEmployees.collectAsStateWithLifecycle()
            val topWorked by viewModel.topWorkedEmployees.collectAsStateWithLifecycle()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(text = "أكثر 3 موظفين التزاماً (بالنقاط)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Outline),
                colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (topCommitted.isEmpty()) {
                        Text("لا يوجد بيانات كافية.", style = MaterialTheme.typography.bodyMedium, color = OnSurface)
                    } else {
                        topCommitted.forEach { (emp, count) ->
                            Text("- ${emp.name} ($count نقطة)", style = MaterialTheme.typography.bodyMedium, color = OnSurface)
                        }
                    }
                }
            }

            Text(text = "أكثر 3 موظفين إنجازاً للوقت الإضافي", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Outline),
                colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (topWorked.isEmpty()) {
                        Text("لا يوجد بيانات كافية.", style = MaterialTheme.typography.bodyMedium, color = OnSurface)
                    } else {
                        topWorked.forEach { (emp, hours) ->
                            Text("- ${emp.name} ($hours ساعة إضافية)", style = MaterialTheme.typography.bodyMedium, color = OnSurface)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, bgColor: Color, contentColor: Color, borderColor: Color) {
    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = contentColor)
            Text(text = value, fontSize = 32.sp, fontWeight = FontWeight.Black, color = contentColor)
        }
    }
}
