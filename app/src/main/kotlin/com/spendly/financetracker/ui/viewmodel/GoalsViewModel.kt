package com.spendly.financetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendly.financetracker.data.model.GoalDraft
import com.spendly.financetracker.data.model.SavingsGoal
import com.spendly.financetracker.data.repository.AuthRepository
import com.spendly.financetracker.data.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class GoalsUiState(
    val goals: List<SavingsGoal> = emptyList(),
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()

    init {
        val uid = authRepository.getCurrentUserId()
        if (uid == null) {
            _uiState.update { it.copy(isLoading = false, error = "Please log in again") }
        } else {
            viewModelScope.launch {
                goalRepository.observeGoals(uid)
                    .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                    .collect { goals -> _uiState.update { it.copy(goals = goals, isLoading = false) } }
            }
        }
    }

    fun saveDraft(draft: GoalDraft, existing: SavingsGoal? = null): Boolean {
        val uid = authRepository.getCurrentUserId() ?: return fail("Please log in again")
        val targetCents = parseAmountCents(draft.targetAmount) ?: return fail("Enter a valid target amount")
        if (draft.title.isBlank()) return fail("Enter a goal name")
        val dueDate = draft.dueDateMillis ?: parseGoalDateMillis(draft.targetDate)
        if (dueDate <= 0L) return fail("Select a target date")
        val initialSaved = parseAmountCents(draft.initialSaved.ifBlank { "0" }) ?: 0L
        val currentGoals = _uiState.value.goals
        val goal = SavingsGoal(
            id = existing?.id.orEmpty(),
            userId = uid,
            title = draft.title.trim(),
            status = draft.status,
            targetCents = targetCents,
            savedCents = existing?.savedCents ?: initialSaved,
            dueDateMillis = dueDate,
            category = draft.category,
            isPrimary = existing?.isPrimary ?: currentGoals.none { it.isPrimary },
            createdAtMillis = existing?.createdAtMillis ?: 0L
        )
        viewModelScope.launch {
            goalRepository.saveGoal(goal)
                .onSuccess { _uiState.update { it.copy(isSaved = true, error = null) } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
        return true
    }

    fun deleteGoal(id: String): Boolean {
        viewModelScope.launch {
            goalRepository.deleteGoal(id)
                .onSuccess { _uiState.update { it.copy(isDeleted = true, error = null) } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
        return true
    }

    fun addSavings(id: String, amount: String): Boolean {
        val cents = parseAmountCents(amount) ?: return fail("Enter a valid savings amount")
        if (cents <= 0L) return fail("Enter a valid savings amount")
        viewModelScope.launch {
            goalRepository.addSavings(id, cents)
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
        return true
    }

    private fun fail(message: String): Boolean {
        _uiState.update { it.copy(error = message) }
        return false
    }

    private fun parseAmountCents(input: String): Long? {
        val n = input.trim()
        if (n.isBlank()) return 0L
        if (!Regex("^\\d+([.]\\d{1,2})?$").matches(n)) return null
        val parts = n.split(".")
        val whole = parts[0].toLongOrNull() ?: return null
        val cents = parts.getOrNull(1)?.padEnd(2, '0') ?: "00"
        return whole * 100L + (cents.toLongOrNull() ?: return null)
    }

    private fun parseGoalDateMillis(value: String): Long {
        val patterns = listOf("MMM d, yyyy", "MMM yyyy", "MMMM yyyy")
        return patterns.firstNotNullOfOrNull { pattern ->
            runCatching {
                SimpleDateFormat(pattern, Locale.getDefault()).apply { isLenient = false }
                    .parse(value)
                    ?.time
            }.getOrNull()
        } ?: Date().time
    }
}
