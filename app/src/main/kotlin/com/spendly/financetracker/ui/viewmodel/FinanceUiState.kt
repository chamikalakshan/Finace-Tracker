package com.spendly.financetracker.ui.viewmodel

import com.spendly.financetracker.data.model.FinanceTransaction
import com.spendly.financetracker.data.model.SavingsGoal
import com.spendly.financetracker.data.model.TransactionType
import com.spendly.financetracker.data.model.UserProfile
import com.spendly.financetracker.data.model.UserSession

typealias Goal = SavingsGoal
typealias GoalDraft = com.spendly.financetracker.data.model.GoalDraft

enum class AuthMode {
    SIGN_IN,
    CREATE_ACCOUNT
}

enum class AppTab {
    HOME,
    TRANSACTIONS,
    GOALS,
    ANALYTICS,
    PROFILE
}

enum class TransactionTab(val title: String) {
    ALL("All"),
    EXPENSES("Expenses"),
    INCOMES("Incomes")
}

data class FinanceUiState(
    val isFirebaseConfigured: Boolean = true,
    val isLoading: Boolean = true,
    val isBusy: Boolean = false,
    val session: UserSession? = null,
    val profile: UserProfile? = null,
    val authMode: AuthMode = AuthMode.SIGN_IN,
    val email: String = "",
    val password: String = "",
    val transactionTitle: String = "",
    val transactionAmount: String = "",
    val transactionNote: String = "",
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val currentTab: AppTab = AppTab.HOME,
    val transactionTab: TransactionTab = TransactionTab.ALL,
    val goals: List<SavingsGoal> = emptyList(),
    val transactions: List<FinanceTransaction> = emptyList(),
    val message: String? = null
) {
    val incomeCents: Long
        get() = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amountCents }

    val expenseCents: Long
        get() = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountCents }

    val balanceCents: Long
        get() = transactions.sumOf { it.signedAmountCents }

    val primaryGoal: SavingsGoal?
        get() = goals.firstOrNull { it.isPrimary } ?: goals.firstOrNull()

    val savingsRate: Int
        get() = if (incomeCents <= 0L) 0 else (((incomeCents - expenseCents) * 100) / incomeCents).toInt().coerceIn(0, 100)
}
