package com.highcom.comicmemo.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val baseApiUrl = "https://app.rakuten.co.jp/services/api/"

private val httpLogging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
private val httpClientBuilder = OkHttpClient.Builder().addInterceptor(httpLogging)

private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(baseApiUrl)
    .client(httpClientBuilder.build())
    .build()

/**
 * 楽天APIサービスインターフェース
 */
interface RakutenApiService {
    @GET("BooksBook/Search/20170404?format=json&sort=sales&hits=30")
    fun salesItems(@Query("booksGenreId") genreId: String, @Query("page") page: String, @Query("applicationId") appId: String): retrofit2.Call<RakutenBookData>

    @GET("BooksBook/Search/20170404?format=json&hits=30")
    fun searchItems(@Query("booksGenreId") genreId: String, @Query("title") title: String, @Query("page") page: String, @Query("applicationId") appId: String): retrofit2.Call<RakutenBookData>
}

/**
 * 楽天API
 */
object RakutenApi {
    val retrofitService: RakutenApiService by lazy { retrofit.create(RakutenApiService::class.java)}
}