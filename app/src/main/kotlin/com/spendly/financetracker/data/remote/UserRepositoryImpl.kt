package com.spendly.financetracker.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.spendly.financetracker.data.local.dao.UserProfileDao
import com.spendly.financetracker.data.local.entity.UserProfileEntity
import com.spendly.financetracker.data.model.UserProfile
import com.spendly.financetracker.data.repository.UserRepository
import com.spendly.financetracker.util.toEntity
import com.spendly.financetracker.util.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userProfileDao: UserProfileDao
) : UserRepository {
    override fun observeProfile(uid: String): Flow<UserProfile?> =
        userProfileDao.observeById(uid).map { it?.toModel() }

    override suspend fun upsertProfile(profile: UserProfile): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        val entity = profile.copy(
            isSynced = false,
            createdAtMillis = if (profile.createdAtMillis == 0L) now else profile.createdAtMillis,
            updatedAtMillis = now
        ).toEntity()
        userProfileDao.upsert(entity)
        syncOne(entity)
    }

    override suspend fun syncWithFirestore(uid: String) {
        userProfileDao.getUnsynced(uid).forEach { syncOne(it) }
        val doc = firestore.collection("users").document(uid).collection("profile").document("main").get().await()
        val data = doc.data ?: return
        userProfileDao.upsert(
            UserProfileEntity(
                uid = uid,
                name = data["name"] as? String ?: "",
                email = data["email"] as? String ?: "",
                defaultCurrency = data["defaultCurrency"] as? String ?: "LKR",
                createdAtMillis = (data["createdAtMillis"] as? Number)?.toLong() ?: 0L,
                updatedAtMillis = (data["updatedAtMillis"] as? Number)?.toLong() ?: 0L,
                isSynced = true
            )
        )
    }

    private suspend fun syncOne(entity: UserProfileEntity) {
        firestore.collection("users").document(entity.uid)
            .collection("profile").document("main")
            .set(entity.toFirestoreMap())
            .await()
        userProfileDao.markAsSynced(entity.uid)
    }

    private fun UserProfileEntity.toFirestoreMap(): Map<String, Any> = mapOf(
        "uid" to uid,
        "name" to name,
        "email" to email,
        "defaultCurrency" to defaultCurrency,
        "createdAtMillis" to createdAtMillis,
        "updatedAtMillis" to updatedAtMillis
    )
}
