package com.mochimono.packingchecklist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mochimono.packingchecklist.data.local.entity.DefaultTemplateItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DefaultTemplateItemDao {

    @Query("SELECT * FROM default_template_items ORDER BY sortOrder ASC")
    fun getAll(): Flow<List<DefaultTemplateItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<DefaultTemplateItemEntity>)

    @Query("DELETE FROM default_template_items")
    suspend fun deleteAll()
}
