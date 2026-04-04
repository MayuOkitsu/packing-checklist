package com.mochimono.packingchecklist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mochimono.packingchecklist.data.local.entity.CustomTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomTemplateDao {

    @Query("SELECT * FROM custom_templates ORDER BY createdAt DESC")
    fun getAll(): Flow<List<CustomTemplateEntity>>

    @Query("SELECT * FROM custom_templates WHERE id = :id")
    suspend fun getById(id: String): CustomTemplateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CustomTemplateEntity)

    @Update
    suspend fun update(entity: CustomTemplateEntity)

    @Query("DELETE FROM custom_templates WHERE id = :id")
    suspend fun deleteById(id: String)
}
