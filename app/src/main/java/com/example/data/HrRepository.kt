package com.example.data

import kotlinx.coroutines.flow.Flow

class HrRepository(private val dao: HrDao) {

    // Settings
    val settings: Flow<AppSettings?> = dao.getSettings()
    suspend fun saveSettings(settings: AppSettings) = dao.insertOrUpdateSettings(settings)

    // Shifts
    val allShifts: Flow<List<Shift>> = dao.getAllShifts()
    suspend fun insertShift(shift: Shift) = dao.insertShift(shift)
    suspend fun updateShift(shift: Shift) = dao.updateShift(shift)
    suspend fun deleteShift(shift: Shift) = dao.deleteShift(shift)

    // Employees
    val allEmployees: Flow<List<Employee>> = dao.getAllEmployees()
    suspend fun insertEmployee(employee: Employee) = dao.insertEmployee(employee)
    suspend fun updateEmployee(employee: Employee) = dao.updateEmployee(employee)
    suspend fun deleteEmployee(employee: Employee) = dao.deleteEmployee(employee)

    // Attendance
    fun getAttendanceForDate(date: String) = dao.getAttendanceForDate(date)
    suspend fun getAttendanceRecord(employeeId: Int, shiftId: Int, date: String) = dao.getAttendanceRecord(employeeId, shiftId, date)
    val allAttendance: Flow<List<Attendance>> = dao.getAllAttendance()
    suspend fun insertAttendance(attendance: Attendance) = dao.insertAttendance(attendance)
    suspend fun updateAttendance(attendance: Attendance) = dao.updateAttendance(attendance)
    suspend fun deleteAttendance(attendance: Attendance) = dao.deleteAttendance(attendance)
    
    // Dashboard Stats
    val employeeCount = dao.getEmployeeCount()
    fun getPresentCount(date: String) = dao.getPresentCount(date)
    fun getAbsentCount(date: String) = dao.getAbsentCount(date)
    fun getLeaveCount(date: String) = dao.getLeaveCount(date)
    
    fun getEmployeeAttendance(employeeId: Int) = dao.getAttendanceForEmployee(employeeId)
}
