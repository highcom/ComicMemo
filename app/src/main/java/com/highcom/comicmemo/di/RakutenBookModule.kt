package com.highcom.comicmemo.di

import com.highcom.comicmemo.network.RakutenApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

private const val baseApiUrl = "https://app.rakuten.co.jp/services/api/"

@Module
@InstallIn(SingletonComponent::class)
class RakutenBookModule {

    @Singleton
    @Provides
    fun provideRetrofit(): Retrofit {

        val httpLogging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        val httpClientBuilder = OkHttpClient.Builder().addInterceptor(httpLogging)

        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseApiUrl)
            .client(httpClientBuilder.build())
            .build()
    }

    @Singleton
    @Provides
    fun provideRakutenApiService(retrofit: Retrofit): RakutenApiService = retrofit.create(RakutenApiService::class.java)
}