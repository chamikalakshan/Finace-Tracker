package com.spendly.financetracker.data.model

enum class TransactionType {
    INCOME,
    EXPENSE
}

data class FinanceTransaction(
    val id: String,
    val userId: String,
    val title: String,
    val amountCents: Long,
    val type: TransactionType,
    val category: String,
    val source: String,
    val note: String,
    val dateMillis: Long,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val isSynced: Boolean
) {
    val signedAmountCents: Long
        get() = if (type == TransactionType.INCOME) amountCents else -amountCents
}

data class TransactionDraft(
    val title: String,
    val amountCents: Long,
    val type: TransactionType,
    val category: String = "",
    val source: String = "",
    val note: String,
    val dateMillis: Long
)
