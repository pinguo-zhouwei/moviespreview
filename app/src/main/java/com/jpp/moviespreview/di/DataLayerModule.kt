package com.jpp.moviespreview.di

import android.content.Context
import com.jpp.moviespreview.BuildConfig
import com.jpp.moviespreview.datalayer.api.ServerRepository
import com.jpp.moviespreview.datalayer.db.MPDataBase
import com.jpp.moviespreview.datalayer.db.MPDataBaseImpl
import com.jpp.moviespreview.datalayer.db.cache.MPCache
import com.jpp.moviespreview.datalayer.db.cache.MPCacheImpl
import com.jpp.moviespreview.datalayer.db.repository.DBConfigurationRepository
import com.jpp.moviespreview.datalayer.db.room.RoomModelAdapter
import com.jpp.moviespreview.datalayer.repository.ConfigurationRepository
import com.jpp.moviespreview.datalayer.repository.ConfigurationRepositoryImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Provides all dependencies for the data layer.
 */
@Module
class DataLayerModule {

    @Singleton
    @Provides
    fun provideConfigurationRepository(dbConfigurationRepository: DBConfigurationRepository, serverRepositoryImpl: ServerRepository)
            : ConfigurationRepository = ConfigurationRepositoryImpl(dbConfigurationRepository, serverRepositoryImpl)

    @Singleton
    @Provides
    fun providesDBConfigurationRepository(mpCache: MPCache, mpDataBase: MPDataBase)
            : DBConfigurationRepository = DBConfigurationRepository(mpCache, mpDataBase)

    @Singleton
    @Provides
    fun providesServerRepositoryImpl() = ServerRepository(BuildConfig.API_KEY)

    @Singleton
    @Provides
    fun providesMPCache(context: Context): MPCache = MPCacheImpl(context)

    @Singleton
    @Provides
    fun providesMPDataBase(context: Context): MPDataBase = MPDataBaseImpl(context, RoomModelAdapter())
}