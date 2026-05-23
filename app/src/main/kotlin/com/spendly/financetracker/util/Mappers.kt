package com.spendly.financetracker.util

import com.spendly.financetracker.data.local.entity.ExpenseEntryEntity
import com.spendly.financetracker.data.local.entity.IncomeEntryEntity
import com.spendly.financetracker.data.local.entity.SavingsGoalEntity
import com.spendly.financetracker.data.local.entity.UserProfileEntity
import com.spendly.financetracker.data.model.FinanceTransaction
import com.spendly.financetracker.data.model.SavingsGoal
import com.spendly.financetracker.data.model.TransactionType
import com.spendly.financetracker.data.model.UserProfile

fun IncomeEntryEntity.toTransaction(): FinanceTransaction = FinanceTransaction(
    id = id,
    userId = userId,
    title = name,
    amountCents = amountCents,
    type = TransactionType.INCOME,
    category = "",
    source = source,
    note = note,
    dateMillis = dateMillis,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
    isSynced = isSynced
)

fun ExpenseEntryEntity.toTransaction(): FinanceTransaction = FinanceTransaction(
    id = id,
    userId = userId,
    title = name,
    amountCents = amountCents,
    type = TransactionType.EXPENSE,
    category = category,
    source = "",
    note = note,
    dateMillis = dateMillis,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
    isSynced = isSynced
)

fun SavingsGoalEntity.toModel(): SavingsGoal = SavingsGoal(
    id = id,
    userId = userId,
    title = title,
    status = status,
    targetCents = targetCents,
    savedCents = savedCents,
    dueDateMillis = dueDateMillis,
    category = category,
    isPrimary = isPrimary,
    isSynced = isSynced,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis
)

fun SavingsGoal.toEntity(): SavingsGoalEntity = SavingsGoalEntity(
    id = id,
    userId = userId,
    title = title,
    status = status,
    targetCents = targetCents,
    savedCents = savedCents,
    dueDateMillis = dueDateMillis,
    category = category,
    isPrimary = isPrimary,
    isSynced = isSynced,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis
)

fun UserProfileEntity.toModel(): UserProfile = UserProfile(
    uid = uid,
    name = name,
    email = email,
    defaultCurrency = defaultCurrency,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
    isSynced = isSynced
)

fun UserProfile.toEntity(): UserProfileEntity = UserProfileEntity(
    uid = uid,
    name = name,
    email = email,
    defaultCurrency = defaultCurrency,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
    isSynced = isSynced
)
