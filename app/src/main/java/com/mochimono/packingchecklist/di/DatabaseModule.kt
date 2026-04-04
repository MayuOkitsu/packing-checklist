package com.mochimono.packingchecklist.di

import android.content.Context
import androidx.room.Room
import com.mochimono.packingchecklist.data.local.dao.BagDao
import com.mochimono.packingchecklist.data.local.dao.ChecklistDao
import com.mochimono.packingchecklist.data.local.dao.ChecklistItemDao
import com.mochimono.packingchecklist.data.local.dao.CustomTemplateDao
import com.mochimono.packingchecklist.data.local.dao.CustomTemplateItemDao
import com.mochimono.packingchecklist.data.local.dao.DefaultTemplateItemDao
import com.mochimono.packingchecklist.data.local.dao.FamilyGroupDao
import com.mochimono.packingchecklist.data.local.dao.PendingSyncDao
import com.mochimono.packingchecklist.data.local.db.PackingChecklistDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PackingChecklistDatabase =
        Room.databaseBuilder(context, PackingChecklistDatabase::class.java, PackingChecklistDatabase.DATABASE_NAME)
            .addCallback(PackingChecklistDatabase.createCallback())
            .build()

    @Provides fun provideChecklistDao(db: PackingChecklistDatabase): ChecklistDao = db.checklistDao()
    @Provides fun provideChecklistItemDao(db: PackingChecklistDatabase): ChecklistItemDao = db.checklistItemDao()
    @Provides fun provideBagDao(db: PackingChecklistDatabase): BagDao = db.bagDao()
    @Provides fun provideDefaultTemplateItemDao(db: PackingChecklistDatabase): DefaultTemplateItemDao = db.defaultTemplateItemDao()
    @Provides fun provideCustomTemplateDao(db: PackingChecklistDatabase): CustomTemplateDao = db.customTemplateDao()
    @Provides fun provideCustomTemplateItemDao(db: PackingChecklistDatabase): CustomTemplateItemDao = db.customTemplateItemDao()
    @Provides fun provideFamilyGroupDao(db: PackingChecklistDatabase): FamilyGroupDao = db.familyGroupDao()
    @Provides fun providePendingSyncDao(db: PackingChecklistDatabase): PendingSyncDao = db.pendingSyncDao()
}
