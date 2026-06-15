package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HrDao {

    // Settings
    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettings(): Flow<AppSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: AppSettings)

    // Shifts
    @Query("SELECT * FROM shifts")
    fun getAllShifts(): Flow<List<Shift>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShift(shift: Shift)

    @Update
    suspend fun updateShift(shift: Shift)

    @Delete
    suspend fun deleteShift(shift: Shift)

    // Employees
    @Query("SELECT * FROM employees")
    fun getAllEmployees(): Flow<List<Employee>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee)

    @Update
    suspend fun updateEmployee(employee: Employee)

    @Delete
    suspend fun deleteEmployee(employee: Employee)

    // Attendance
    @Query("SELECT * FROM attendance WHERE date = :date")
    fun getAttendanceForDate(date: String): Flow<List<Attendance>>
    
    @Query("SELECT * FROM attendance WHERE employeeId = :employeeId AND shiftId = :shiftId AND date = :date LIMIT 1")
    suspend fun getAttendanceRecord(employeeId: Int, shiftId: Int, date: String): Attendance?
    
    @Query("SELECT * FROM attendance")
    fun getAllAttendance(): Flow<List<Attendance>>
    
    @Query("SELECT * FROM attendance WHERE employeeId = :employeeId")
    fun getAttendanceForEmployee(employeeId: Int): Flow<List<Attendance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance)

    @Update
    suspend fun updateAttendance(attendance: Attendance)

    @Delete
    suspend fun deleteAttendance(attendance: Attendance)
    
    // Aggregation queries for Dashboard
    @Query("SELECT COUNT(*) FROM employees")
    fun getEmployeeCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM attendance WHERE date = :date AND status IN ('حضور', 'تأخير')")
    fun getPresentCount(date: String): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM attendance WHERE date = :date AND status = 'غياب'")
    fun getAbsentCount(date: String): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM attendance WHERE date = :date AND status = 'إجازة'")
    fun getLeaveCount(date: String): Flow<Int>
}
