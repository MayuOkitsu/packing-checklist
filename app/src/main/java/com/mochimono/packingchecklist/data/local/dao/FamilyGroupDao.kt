package com.mochimono.packingchecklist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mochimono.packingchecklist.data.local.entity.FamilyGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyGroupDao {

    @Query("SELECT * FROM family_groups LIMIT 1")
    fun getCurrent(): Flow<FamilyGroupEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FamilyGroupEntity)

    @Query("DELETE FROM family_groups")
    suspend fun deleteAll()
}
