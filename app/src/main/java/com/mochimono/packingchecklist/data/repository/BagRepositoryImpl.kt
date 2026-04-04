package com.mochimono.packingchecklist.data.repository

import com.mochimono.packingchecklist.data.local.dao.BagDao
import com.mochimono.packingchecklist.data.local.entity.BagEntity
import com.mochimono.packingchecklist.domain.model.Bag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class BagRepositoryImpl @Inject constructor(
    private val bagDao: BagDao
) : BagRepository {

    override fun getAllBags(): Flow<List<Bag>> =
        bagDao.getAll().map { list -> list.map { Bag(id = it.id, name = it.name) } }

    override suspend fun createBag(name: String): Bag {
        val entity = BagEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            createdAt = System.currentTimeMillis()
        )
        bagDao.insert(entity)
        return Bag(id = entity.id, name = entity.name)
    }

    override suspend fun updateBagName(id: String, name: String) {
        val existing = bagDao.getById(id) ?: return
        bagDao.update(existing.copy(name = name))
    }

    override suspend fun deleteBag(id: String) =
        bagDao.deleteById(id)
}
