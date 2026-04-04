package com.mochimono.packingchecklist.data.repository

import com.mochimono.packingchecklist.data.local.dao.PendingSyncDao
import com.mochimono.packingchecklist.data.local.entity.PendingSyncEntity
import javax.inject.Inject
import javax.inject.Singleton

interface SyncRepository {
    suspend fun enqueue(entity: PendingSyncEntity)
    suspend fun getUnsynced(): List<PendingSyncEntity>
    suspend fun markSynced(id: String)
}

@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val pendingSyncDao: PendingSyncDao
) : SyncRepository {

    override suspend fun enqueue(entity: PendingSyncEntity) =
        pendingSyncDao.insert(entity)

    override suspend fun getUnsynced(): List<PendingSyncEntity> =
        pendingSyncDao.getUnsynced()

    override suspend fun markSynced(id: String) =
        pendingSyncDao.markSynced(id)
}
