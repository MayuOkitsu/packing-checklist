package com.mochimono.packingchecklist.di

import com.mochimono.packingchecklist.data.repository.BagRepository
import com.mochimono.packingchecklist.data.repository.BagRepositoryImpl
import com.mochimono.packingchecklist.data.repository.ChecklistItemRepository
import com.mochimono.packingchecklist.data.repository.ChecklistItemRepositoryImpl
import com.mochimono.packingchecklist.data.repository.ChecklistRepository
import com.mochimono.packingchecklist.data.repository.ChecklistRepositoryImpl
import com.mochimono.packingchecklist.data.repository.FamilyGroupRepository
import com.mochimono.packingchecklist.data.repository.FamilyGroupRepositoryImpl
import com.mochimono.packingchecklist.data.repository.SyncRepository
import com.mochimono.packingchecklist.data.repository.SyncRepositoryImpl
import com.mochimono.packingchecklist.data.repository.TemplateRepository
import com.mochimono.packingchecklist.data.repository.TemplateRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds abstract fun bindChecklistRepository(impl: ChecklistRepositoryImpl): ChecklistRepository
    @Binds abstract fun bindChecklistItemRepository(impl: ChecklistItemRepositoryImpl): ChecklistItemRepository
    @Binds abstract fun bindTemplateRepository(impl: TemplateRepositoryImpl): TemplateRepository
    @Binds abstract fun bindBagRepository(impl: BagRepositoryImpl): BagRepository
    @Binds abstract fun bindFamilyGroupRepository(impl: FamilyGroupRepositoryImpl): FamilyGroupRepository
    @Binds abstract fun bindSyncRepository(impl: SyncRepositoryImpl): SyncRepository
}
