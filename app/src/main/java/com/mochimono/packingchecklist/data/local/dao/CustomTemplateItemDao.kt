package com.mochimono.packingchecklist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mochimono.packingchecklist.data.local.entity.CustomTemplateItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomTemplateItemDao {

    @Query("SELECT * FROM custom_template_items WHERE templateId = :templateId ORDER BY sortOrder ASC")
    fun getByTemplateId(templateId: String): Flow<List<CustomTemplateItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<CustomTemplateItemEntity>)

    @Query("DELETE FROM custom_template_items WHERE templateId = :templateId")
    suspend fun deleteByTemplateId(templateId: String)
}
