package com.jawahir.amoro.di

import android.content.Context
import androidx.room.Room
import com.jawahir.amoro.data.local.AppDatabase
import com.jawahir.amoro.data.local.dao.MovieDao
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase{
        return Room.databaseBuilder(context, AppDatabase::class.java,"amoro_db").build()
    }

    @Provides
    @Singleton
    fun provideMovieDao(database: AppDatabase): MovieDao{
        return database.movieDao()
    }
}