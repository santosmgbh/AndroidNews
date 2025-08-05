package com.gabrielsantos.shortnify.di

import com.gabrielsantos.shortnify.data.network.LinkAPI
import com.gabrielsantos.shortnify.data.network.LinkAPIImpl
import com.gabrielsantos.shortnify.data.LinkRepository
import com.gabrielsantos.shortnify.data.LinkRepositoryImpl
import com.gabrielsantos.shortnify.data.local.LocalDataSource
import com.gabrielsantos.shortnify.data.local.LocalDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    abstract fun bindRepository(repository: LinkRepositoryImpl): LinkRepository

    @Binds
    abstract fun bindFactAPI(linkAPI: LinkAPIImpl): LinkAPI

    @Binds
    abstract fun bindLocalDataSource(localDataSource: LocalDataSourceImpl): LocalDataSource
}