package com.example.programmingassignment.data

import java.util.Date

data class Task(
    val id : String = "",
    val email: String = "",
    val title: String = "",
    val description: String = "",
    val completed: Boolean = false,
    val important: Boolean = false,
    var dueDate: Date? = null // This will hold both date and time
)
