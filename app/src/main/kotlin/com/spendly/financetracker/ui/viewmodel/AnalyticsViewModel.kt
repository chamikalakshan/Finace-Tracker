package com.spendly.financetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.spendly.financetracker.data.model.FinanceTransaction
import com.spendly.financetracker.data.model.TransactionType
import com.spendly.financetracker.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AnalyticsUiState(
    val transactions: List<FinanceTransaction> = emptyList(),
    val isLoading: Boolean = true
) {
    val totalIncome: Long get() = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amountCents }
    val totalExpense: Long get() = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountCents }
}

class AnalyticsViewModel(
    private val transactionRepository: TransactionRepository,
    private val userId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            transactionRepository.observeTransactions(userId)
                .catch { _uiState.update { it.copy(isLoading = false) } }
                .collect { list -> _uiState.update { it.copy(transactions = list, isLoading = false) } }
        }
    }

    class Factory(
        private val transactionRepository: TransactionRepository,
        private val userId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AnalyticsViewModel(transactionRepository, userId) as T
    }
}
