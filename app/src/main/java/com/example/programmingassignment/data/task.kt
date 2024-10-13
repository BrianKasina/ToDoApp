package com.example.programmingassignment.data

import java.util.Date

data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val isCompleted: Boolean = false,
    val isImportant: Boolean = false,
    var dueDate: Date? = null // This will hold both date and time
)
