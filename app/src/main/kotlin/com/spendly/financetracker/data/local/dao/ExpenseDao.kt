package com.spendly.financetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.spendly.financetracker.data.local.entity.ExpenseEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ExpenseEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ExpenseEntryEntity>)

    @Update
    suspend fun update(entity: ExpenseEntryEntity)

    @Query("DELETE FROM expense_entries WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM expense_entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ExpenseEntryEntity?

    @Query("SELECT * FROM expense_entries WHERE userId = :userId ORDER BY dateMillis DESC")
    fun observeByUser(userId: String): Flow<List<ExpenseEntryEntity>>

    @Query("SELECT * FROM expense_entries WHERE userId = :userId AND dateMillis BETWEEN :startMillis AND :endMillis ORDER BY dateMillis DESC")
    fun observeByMonth(userId: String, startMillis: Long, endMillis: Long): Flow<List<ExpenseEntryEntity>>

    @Query("SELECT * FROM expense_entries WHERE isSynced = 0 AND userId = :userId")
    suspend fun getUnsynced(userId: String): List<ExpenseEntryEntity>

    @Query("UPDATE expense_entries SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}
