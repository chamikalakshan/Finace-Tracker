package com.spendly.financetracker.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.spendly.financetracker.data.local.dao.ExpenseDao
import com.spendly.financetracker.data.local.entity.ExpenseEntryEntity
import com.spendly.financetracker.data.model.FinanceTransaction
import com.spendly.financetracker.data.model.TransactionDraft
import com.spendly.financetracker.data.repository.ExpenseRepository
import com.spendly.financetracker.util.toTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val expenseDao: ExpenseDao
) : ExpenseRepository {
    override fun observeExpenses(userId: String): Flow<List<FinanceTransaction>> =
        expenseDao.observeByUser(userId).map { rows -> rows.map { it.toTransaction() } }

    override fun observeMonthlyExpenses(
        userId: String,
        startMillis: Long,
        endMillis: Long
    ): Flow<List<FinanceTransaction>> =
        expenseDao.observeByMonth(userId, startMillis, endMillis).map { rows -> rows.map { it.toTransaction() } }

    override suspend fun addExpense(userId: String, draft: TransactionDraft): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        val entity = ExpenseEntryEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            name = draft.title,
            amountCents = draft.amountCents,
            category = draft.category.ifBlank { "Other" },
            dateMillis = draft.dateMillis,
            note = draft.note,
            isSynced = false,
            createdAtMillis = now,
            updatedAtMillis = now
        )
        expenseDao.insert(entity)
        syncOne(entity)
    }

    override suspend fun deleteExpense(id: String): Result<Unit> = runCatching {
        val existing = expenseDao.getById(id)
        expenseDao.deleteById(id)
        existing?.let {
            firestore.collection("users").document(it.userId)
                .collection("expenses").document(id).delete().await()
        }
    }

    override suspend fun syncWithFirestore(userId: String) {
        expenseDao.getUnsynced(userId).forEach { syncOne(it) }
        val snapshot = firestore.collection("users").document(userId).collection("expenses").get().await()
        expenseDao.insertAll(snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            ExpenseEntryEntity(
                id = doc.id,
                userId = userId,
                name = data["name"] as? String ?: return@mapNotNull null,
                amountCents = (data["amountCents"] as? Number)?.toLong() ?: return@mapNotNull null,
                category = data["category"] as? String ?: "Other",
                dateMillis = (data["dateMillis"] as? Number)?.toLong() ?: 0L,
                note = data["note"] as? String ?: "",
                isSynced = true,
                createdAtMillis = (data["createdAtMillis"] as? Number)?.toLong() ?: 0L,
                updatedAtMillis = (data["updatedAtMillis"] as? Number)?.toLong() ?: 0L
            )
        })
    }

    private suspend fun syncOne(entity: ExpenseEntryEntity) {
        firestore.collection("users").document(entity.userId)
            .collection("expenses").document(entity.id)
            .set(entity.toFirestoreMap())
            .await()
        expenseDao.markAsSynced(entity.id)
    }

    private fun ExpenseEntryEntity.toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id,
        "userId" to userId,
        "name" to name,
        "amountCents" to amountCents,
        "category" to category,
        "dateMillis" to dateMillis,
        "note" to note,
        "createdAtMillis" to createdAtMillis,
        "updatedAtMillis" to updatedAtMillis
    )
}
