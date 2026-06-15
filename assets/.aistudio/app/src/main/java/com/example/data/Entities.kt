package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class AppSettings(
    @PrimaryKey
    val id: Int = 1,
    val companyName: String = "شركتي",
    val logoUri: String? = null
)

@Entity(tableName = "shifts")
data class Shift(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val startHour: Int, // 1-12
    val startMinute: Int, // 0-59
    val startAmPm: String, // "ص" or "م"
    val endHour: Int,
    val endMinute: Int,
    val endAmPm: String,
    val breakMinutes: Int
)

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val shiftIds: String = "" // An employee may have multiple shifts comma-separated
)

@Entity(
    tableName = "attendance",
    foreignKeys = [
        ForeignKey(
            entity = Employee::class,
            parentColumns = ["id"],
            childColumns = ["employeeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Shift::class,
            parentColumns = ["id"],
            childColumns = ["shiftId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Attendance(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val employeeId: Int,
    val shiftId: Int,
    val date: String, // format YYYY-MM-DD
    val status: String, // "حضور", "غياب", "إجازة", "تأخير"
    val inHour: Int? = null,
    val inMinute: Int? = null,
    val inAmPm: String? = null,
    val outHour: Int? = null,
    val outMinute: Int? = null,
    val outAmPm: String? = null,
    val notes: String = ""
)
