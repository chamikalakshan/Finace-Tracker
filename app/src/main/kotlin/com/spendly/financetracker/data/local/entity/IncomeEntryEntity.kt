package com.spendly.financetracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "income_entries",
    indices = [
        Index("userId"),
        Index("dateMillis"),
        Index(value = ["userId", "dateMillis"]),
        Index("source")
    ]
)
data class IncomeEntryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val amountCents: Long,
    val source: String,
    val dateMillis: Long,
    val note: String,
    val isSynced: Boolean,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)
