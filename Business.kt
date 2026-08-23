package com.example.businessapp.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "businesses")
data class Business(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String = "",
    val phone: String = "",
    val address: String = "",
    val notes: String = ""
)
