package com.mochimono.packingchecklist.data.repository

import com.mochimono.packingchecklist.data.local.dao.CustomTemplateDao
import com.mochimono.packingchecklist.data.local.dao.CustomTemplateItemDao
import com.mochimono.packingchecklist.data.local.dao.DefaultTemplateItemDao
import com.mochimono.packingchecklist.data.local.entity.CustomTemplateEntity
import com.mochimono.packingchecklist.data.local.entity.CustomTemplateItemEntity
import com.mochimono.packingchecklist.data.local.entity.DefaultTemplateItemEntity
import com.mochimono.packingchecklist.domain.model.CustomTemplate
import com.mochimono.packingchecklist.domain.model.TemplateItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class TemplateRepositoryImpl @Inject constructor(
    private val defaultTemplateItemDao: DefaultTemplateItemDao,
    private val customTemplateDao: CustomTemplateDao,
    private val customTemplateItemDao: CustomTemplateItemDao
) : TemplateRepository {

    override fun getDefaultTemplate(): Flow<List<TemplateItem>> =
        defaultTemplateItemDao.getAll().map { list ->
            list.map { TemplateItem(id = it.id, name = it.name, sortOrder = it.sortOrder) }
        }

    override fun getCustomTemplates(): Flow<List<CustomTemplate>> =
        customTemplateDao.getAll().map { templates ->
            templates.map { t ->
                val items = customTemplateItemDao.getByTemplateId(t.id).first()
                CustomTemplate(id = t.id, label = t.label, items = items.map { it.name })
            }
        }

    override suspend fun updateDefaultTemplate(items: List<TemplateItem>) {
        defaultTemplateItemDao.deleteAll()
        defaultTemplateItemDao.insertAll(items.map {
            DefaultTemplateItemEntity(id = it.id, name = it.name, sortOrder = it.sortOrder)
        })
    }

    override suspend fun saveAsCustomTemplate(label: String, items: List<String>): CustomTemplate {
        val now = System.currentTimeMillis()
        val templateId = UUID.randomUUID().toString()
        customTemplateDao.insert(
            CustomTemplateEntity(id = templateId, label = label, createdAt = now, updatedAt = now)
        )
        customTemplateItemDao.insertAll(items.mapIndexed { i, name ->
            CustomTemplateItemEntity(
                id = UUID.randomUUID().toString(),
                templateId = templateId,
                name = name,
                sortOrder = i
            )
        })
        return CustomTemplate(id = templateId, label = label, items = items)
    }

    override suspend fun deleteCustomTemplate(id: String) =
        customTemplateDao.deleteById(id)

    override suspend fun updateCustomTemplate(id: String, label: String, items: List<String>) {
        val existing = customTemplateDao.getById(id) ?: return
        customTemplateDao.update(existing.copy(label = label, updatedAt = System.currentTimeMillis()))
        customTemplateItemDao.deleteByTemplateId(id)
        customTemplateItemDao.insertAll(items.mapIndexed { i, name ->
            CustomTemplateItemEntity(
                id = UUID.randomUUID().toString(),
                templateId = id,
                name = name,
                sortOrder = i
            )
        })
    }
}
