package com.mochimono.packingchecklist.data.repository

import com.mochimono.packingchecklist.domain.model.Checklist
import kotlinx.coroutines.flow.Flow

interface ChecklistRepository {
    fun getAllChecklists(): Flow<List<Checklist>>
    suspend fun getChecklistById(id: String): Checklist?
    suspend fun createChecklist(title: String): Checklist
    suspend fun deleteChecklist(id: String)
    suspend fun updateChecklistTitle(id: String, title: String)
    suspend fun setSharingEnabled(id: String, enabled: Boolean)
}
