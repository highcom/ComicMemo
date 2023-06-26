package com.highcom.comicmemo.di

import android.content.Context
import androidx.room.Room
import com.highcom.comicmemo.datamodel.ComicMemoRoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ComicMemoModule {

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(context, ComicMemoRoomDatabase::class.java, "ComicMemoDB").build()

    @Singleton
    @Provides
    fun provideComicDao(db: ComicMemoRoomDatabase) = db.comicDao()
}