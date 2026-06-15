package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppSettings
import com.example.data.Attendance
import com.example.data.Employee
import com.example.data.HrDatabase
import com.example.data.HrRepository
import com.example.data.Shift
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HrViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HrRepository
    
    init {
        val database = HrDatabase.getDatabase(application)
        repository = HrRepository(database.hrDao())
        
        // Ensure default settings exist
        viewModelScope.launch {
            repository.settings.collect { settings ->
                if (settings == null) {
                    repository.saveSettings(AppSettings())
                }
            }
        }
    }

    private val _currentDate = MutableStateFlow(getCurrentDateString())
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    fun setCurrentDate(date: String) {
        _currentDate.value = date
    }

    val settings = repository.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())
    val employees = repository.allEmployees.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val shifts = repository.allShifts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allAttendance = repository.allAttendance.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val employeeCount = repository.employeeCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    val currentPresentCount = _currentDate.flatMapLatest { date ->
        repository.getPresentCount(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val currentAbsentCount = _currentDate.flatMapLatest { date ->
        repository.getAbsentCount(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val currentLeaveCount = _currentDate.flatMapLatest { date ->
        repository.getLeaveCount(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val currentAttendance = _currentDate.flatMapLatest { date ->
        repository.getAttendanceForDate(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Computations
    val topCommittedEmployees = kotlinx.coroutines.flow.combine(employees, allAttendance) { emps, atts ->
        emps.map { emp ->
            val empAtts = atts.filter { it.employeeId == emp.id }
            val present = empAtts.count { it.status == "حضور" }
            val late = empAtts.count { it.status == "تأخير" }
            val absent = empAtts.count { it.status == "غياب" }
            
            // Score system: Present (+2), Late (+1), Absent (-2)
            val score = (present * 2) + (late * 1) - (absent * 2)
            emp to score
        }.sortedByDescending { it.second }.take(3)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topWorkedEmployees = kotlinx.coroutines.flow.combine(employees, allAttendance, shifts) { emps, atts, shs ->
        emps.map { emp ->
            val totalExtraMinutes = atts.filter { it.employeeId == emp.id }.sumOf { att ->
                if (att.inHour != null && att.outHour != null && (att.status == "حضور" || att.status == "تأخير")) {
                    val in24 = to24Hr(att.inHour, att.inAmPm)
                    val out24 = to24Hr(att.outHour, att.outAmPm)
                    var actualMins = (out24 * 60 + (att.outMinute ?: 0)) - (in24 * 60 + (att.inMinute ?: 0))
                    if (actualMins < 0) actualMins += 24 * 60

                    val shift = shs.find { it.id == att.shiftId }
                    var expectedMins = 0
                    if (shift != null) {
                        val sIn = to24Hr(shift.startHour, shift.startAmPm)
                        val sOut = to24Hr(shift.endHour, shift.endAmPm)
                        expectedMins = (sOut * 60 + shift.endMinute) - (sIn * 60 + shift.startMinute)
                        if (expectedMins < 0) expectedMins += 24 * 60
                        expectedMins -= shift.breakMinutes
                    }
                    
                    val extra = actualMins - expectedMins
                    if (extra > 0) extra else 0
                } else 0
            }
            val extraHours = totalExtraMinutes / 60
            emp to extraHours
        }.sortedByDescending { it.second }.take(3)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun to24Hr(hr12: Int, amPm: String?): Int {
        val isPm = amPm == "م"
        return when {
            hr12 == 12 && !isPm -> 0
            hr12 < 12 && isPm -> hr12 + 12
            else -> hr12
        }
    }

    // Shifts
    fun addShift(name: String, startHr: Int, startMin: Int, startA: String, endHr: Int, endMin: Int, endA: String, breakMins: Int) {
        viewModelScope.launch {
            repository.insertShift(Shift(name = name, startHour = startHr, startMinute = startMin, startAmPm = startA, endHour = endHr, endMinute = endMin, endAmPm = endA, breakMinutes = breakMins))
        }
    }
    
    fun updateShift(shift: Shift) {
        viewModelScope.launch {
            repository.updateShift(shift)
        }
    }

    fun deleteShift(shift: Shift) {
        viewModelScope.launch {
            repository.deleteShift(shift)
        }
    }

    // Employees
    fun addEmployee(name: String, shiftIds: List<Int>) {
        viewModelScope.launch {
            repository.insertEmployee(Employee(name = name, shiftIds = shiftIds.joinToString(",")))
        }
    }

    fun updateEmployee(employee: Employee, name: String, shiftIds: List<Int>) {
        viewModelScope.launch {
            repository.updateEmployee(employee.copy(name = name, shiftIds = shiftIds.joinToString(",")))
        }
    }

    fun deleteEmployee(employee: Employee) {
        viewModelScope.launch {
            repository.deleteEmployee(employee)
        }
    }

    // Attendance
    fun saveAttendance(employeeId: Int, shiftId: Int, status: String, inHr: Int?, inMin: Int?, inAmPm: String?, outHr: Int?, outMin: Int?, outAmPm: String?, notes: String = "") {
        viewModelScope.launch {
            val existing = repository.getAttendanceRecord(employeeId, shiftId, _currentDate.value)
            if (existing != null) {
                repository.updateAttendance(existing.copy(status = status, inHour = inHr, inMinute = inMin, inAmPm = inAmPm, outHour = outHr, outMinute = outMin, outAmPm = outAmPm, notes = notes))
            } else {
                repository.insertAttendance(Attendance(employeeId = employeeId, shiftId = shiftId, date = _currentDate.value, status = status, inHour = inHr, inMinute = inMin, inAmPm = inAmPm, outHour = outHr, outMinute = outMin, outAmPm = outAmPm, notes = notes))
            }
        }
    }

    fun deleteAttendance(attendance: Attendance) {
        viewModelScope.launch {
            repository.deleteAttendance(attendance)
        }
    }

    fun changeDateBy(days: Int) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val date = sdf.parse(_currentDate.value)
            if (date != null) {
                val calendar = java.util.Calendar.getInstance()
                calendar.time = date
                calendar.add(java.util.Calendar.DAY_OF_YEAR, days)
                _currentDate.value = sdf.format(calendar.time)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Settings
    fun updateSettings(name: String, logoUri: String?) {
        viewModelScope.launch {
            repository.saveSettings(AppSettings(id = 1, companyName = name, logoUri = logoUri))
        }
    }

    fun exportDataToCsv(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val emps = employees.value
                val shs = shifts.value
                val atts = allAttendance.value
                
                context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
                    writer.write("SECTION:EMPLOYEES\n")
                    writer.write("id,name,shiftIds\n")
                    emps.forEach { writer.write("${it.id},${it.name},\"${it.shiftIds}\"\n") }
                    
                    writer.write("SECTION:SHIFTS\n")
                    writer.write("id,name,startHour,startMinute,startAmPm,endHour,endMinute,endAmPm,breakMinutes\n")
                    shs.forEach { writer.write("${it.id},${it.name},${it.startHour},${it.startMinute},${it.startAmPm},${it.endHour},${it.endMinute},${it.endAmPm},${it.breakMinutes}\n") }
                    
                    writer.write("SECTION:ATTENDANCE\n")
                    writer.write("id,employeeId,shiftId,date,status,inHour,inMinute,inAmPm,outHour,outMinute,outAmPm,notes\n")
                    atts.forEach { writer.write("${it.id},${it.employeeId},${it.shiftId},${it.date},${it.status},${it.inHour ?: ""},${it.inMinute ?: ""},${it.inAmPm ?: ""},${it.outHour ?: ""},${it.outMinute ?: ""},${it.outAmPm ?: ""},\"${it.notes ?: ""}\"\n") }
                }
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    android.widget.Toast.makeText(context, "تم التصدير بنجاح", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    android.widget.Toast.makeText(context, "فشل في التصدير", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun importDataFromCsv(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                var currentSection = ""
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.useLines { lines ->
                    val iterator = lines.iterator()
                    while (iterator.hasNext()) {
                        val line = iterator.next()
                        if (line.startsWith("SECTION:")) {
                            currentSection = line.substringAfter("SECTION:")
                            if (iterator.hasNext()) iterator.next()
                            continue
                        }
                        if (line.isBlank() || !line.contains(",")) continue
                        val parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$).".toRegex()).map { it.removeSurrounding("\"").trim() }
                        if (parts.isEmpty()) continue
                        
                        try {
                            when (currentSection) {
                                "EMPLOYEES" -> {
                                    if (parts.size >= 3) repository.insertEmployee(Employee(id = parts[0].toInt(), name = parts[1], shiftIds = parts[2]))
                                }
                                "SHIFTS" -> {
                                    if (parts.size >= 9) repository.insertShift(Shift(id = parts[0].toInt(), name = parts[1], startHour = parts[2].toInt(), startMinute = parts[3].toInt(), startAmPm = parts[4], endHour = parts[5].toInt(), endMinute = parts[6].toInt(), endAmPm = parts[7], breakMinutes = parts[8].toInt()))
                                }
                                "ATTENDANCE" -> {
                                    if (parts.size >= 12) repository.insertAttendance(Attendance(id = parts[0].toInt(), employeeId = parts[1].toInt(), shiftId = parts[2].toInt(), date = parts[3], status = parts[4], inHour = parts[5].toIntOrNull(), inMinute = parts[6].toIntOrNull(), inAmPm = parts[7].takeIf { it.isNotBlank() }, outHour = parts[8].toIntOrNull(), outMinute = parts[9].toIntOrNull(), outAmPm = parts[10].takeIf { it.isNotBlank() }, notes = parts[11]))
                                }
                            }
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                }
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    android.widget.Toast.makeText(context, "تم الاستيراد بنجاح", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    android.widget.Toast.makeText(context, "فشل في الاستيراد", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getCurrentDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
    }
}
