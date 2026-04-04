package com.mochimono.packingchecklist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mochimono.packingchecklist.data.local.entity.BagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BagDao {

    @Query("SELECT * FROM bags ORDER BY createdAt ASC")
    fun getAll(): Flow<List<BagEntity>>

    @Query("SELECT * FROM bags WHERE id = :id")
    suspend fun getById(id: String): BagEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BagEntity)

    @Update
    suspend fun update(entity: BagEntity)

    @Query("DELETE FROM bags WHERE id = :id")
    suspend fun deleteById(id: String)
}
