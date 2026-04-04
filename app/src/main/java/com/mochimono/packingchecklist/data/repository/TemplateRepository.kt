package com.mochimono.packingchecklist.data.repository

import com.mochimono.packingchecklist.domain.model.CustomTemplate
import com.mochimono.packingchecklist.domain.model.TemplateItem
import kotlinx.coroutines.flow.Flow

interface TemplateRepository {
    fun getDefaultTemplate(): Flow<List<TemplateItem>>
    fun getCustomTemplates(): Flow<List<CustomTemplate>>
    suspend fun updateDefaultTemplate(items: List<TemplateItem>)
    suspend fun saveAsCustomTemplate(label: String, items: List<String>): CustomTemplate
    suspend fun deleteCustomTemplate(id: String)
    suspend fun updateCustomTemplate(id: String, label: String, items: List<String>)
}
