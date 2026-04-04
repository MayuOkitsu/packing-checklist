package com.mochimono.packingchecklist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mochimono.packingchecklist.data.local.entity.PendingSyncEntity

@Dao
interface PendingSyncDao {

    @Query("SELECT * FROM pending_syncs WHERE isSynced = 0 ORDER BY timestamp ASC")
    suspend fun getUnsynced(): List<PendingSyncEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PendingSyncEntity)

    @Query("UPDATE pending_syncs SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("DELETE FROM pending_syncs WHERE isSynced = 1")
    suspend fun deleteSynced()
}
