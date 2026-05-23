package com.spendly.financetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendly.financetracker.data.model.FinanceTransaction
import com.spendly.financetracker.data.model.TransactionType
import com.spendly.financetracker.data.repository.AuthRepository
import com.spendly.financetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionsUiState(
    val transactions: List<FinanceTransaction> = emptyList(),
    val filter: TransactionTab = TransactionTab.ALL,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val filtered: List<FinanceTransaction>
        get() = when (filter) {
            TransactionTab.ALL -> transactions
            TransactionTab.EXPENSES -> transactions.filter { it.type == TransactionType.EXPENSE }
            TransactionTab.INCOMES -> transactions.filter { it.type == TransactionType.INCOME }
        }.sortedByDescending { it.dateMillis }
}

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TransactionsUiState())
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()

    init {
        observeTransactions()
    }

    fun setFilter(tab: TransactionTab) = _uiState.update { it.copy(filter = tab) }

    fun delete(transaction: FinanceTransaction) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transaction)
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    private fun observeTransactions() {
        val uid = authRepository.getCurrentUserId()
        if (uid == null) {
            _uiState.update { it.copy(isLoading = false, error = "Please log in again") }
            return
        }
        viewModelScope.launch {
            transactionRepository.observeTransactions(uid)
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { list -> _uiState.update { it.copy(transactions = list, isLoading = false) } }
        }
    }
}
