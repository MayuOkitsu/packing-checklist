package com.mochimono.packingchecklist.data.repository

import com.mochimono.packingchecklist.domain.model.Bag
import kotlinx.coroutines.flow.Flow

interface BagRepository {
    fun getAllBags(): Flow<List<Bag>>
    suspend fun createBag(name: String): Bag
    suspend fun updateBagName(id: String, name: String)
    suspend fun deleteBag(id: String)
}
