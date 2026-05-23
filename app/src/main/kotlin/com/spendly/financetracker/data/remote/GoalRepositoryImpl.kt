package com.spendly.financetracker.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.spendly.financetracker.data.local.dao.GoalDao
import com.spendly.financetracker.data.local.entity.SavingsGoalEntity
import com.spendly.financetracker.data.model.SavingsGoal
import com.spendly.financetracker.data.repository.GoalRepository
import com.spendly.financetracker.util.toEntity
import com.spendly.financetracker.util.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val goalDao: GoalDao
) : GoalRepository {
    override fun observeGoals(userId: String): Flow<List<SavingsGoal>> =
        goalDao.observeByUser(userId).map { rows -> rows.map { it.toModel() } }

    override suspend fun getGoal(id: String): SavingsGoal? = goalDao.getById(id)?.toModel()

    override suspend fun saveGoal(goal: SavingsGoal): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        val entity = goal.copy(
            id = goal.id.ifBlank { UUID.randomUUID().toString() },
            isSynced = false,
            createdAtMillis = if (goal.createdAtMillis == 0L) now else goal.createdAtMillis,
            updatedAtMillis = now
        ).toEntity()
        goalDao.insert(entity)
        syncOne(entity)
    }

    override suspend fun deleteGoal(id: String): Result<Unit> = runCatching {
        val existing = goalDao.getById(id)
        goalDao.deleteById(id)
        existing?.let {
            firestore.collection("users").document(it.userId)
                .collection("goals").document(id).delete().await()
        }
    }

    override suspend fun addSavings(goalId: String, amountCents: Long): Result<Unit> = runCatching {
        val existing = goalDao.getById(goalId) ?: error("Goal not found")
        val updated = existing.copy(
            savedCents = (existing.savedCents + amountCents).coerceAtLeast(0L),
            isSynced = false,
            updatedAtMillis = System.currentTimeMillis()
        )
        goalDao.insert(updated)
        syncOne(updated)
    }

    override suspend fun syncWithFirestore(userId: String) {
        goalDao.getUnsynced(userId).forEach { syncOne(it) }
        val snapshot = firestore.collection("users").document(userId).collection("goals").get().await()
        snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            SavingsGoalEntity(
                id = doc.id,
                userId = userId,
                title = data["title"] as? String ?: return@mapNotNull null,
                status = data["status"] as? String ?: "On track",
                targetCents = (data["targetCents"] as? Number)?.toLong() ?: 0L,
                savedCents = (data["savedCents"] as? Number)?.toLong() ?: 0L,
                dueDateMillis = (data["dueDateMillis"] as? Number)?.toLong() ?: 0L,
                category = data["category"] as? String ?: "Custom",
                isPrimary = data["isPrimary"] as? Boolean ?: false,
                isSynced = true,
                createdAtMillis = (data["createdAtMillis"] as? Number)?.toLong() ?: 0L,
                updatedAtMillis = (data["updatedAtMillis"] as? Number)?.toLong() ?: 0L
            )
        }.forEach { goalDao.insert(it) }
    }

    private suspend fun syncOne(entity: SavingsGoalEntity) {
        firestore.collection("users").document(entity.userId)
            .collection("goals").document(entity.id)
            .set(entity.toFirestoreMap())
            .await()
        goalDao.markAsSynced(entity.id)
    }

    private fun SavingsGoalEntity.toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id,
        "userId" to userId,
        "title" to title,
        "status" to status,
        "targetCents" to targetCents,
        "savedCents" to savedCents,
        "dueDateMillis" to dueDateMillis,
        "category" to category,
        "isPrimary" to isPrimary,
        "createdAtMillis" to createdAtMillis,
        "updatedAtMillis" to updatedAtMillis
    )
}
