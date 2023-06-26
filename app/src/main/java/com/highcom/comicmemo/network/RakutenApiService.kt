package com.highcom.comicmemo.network

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 楽天APIサービスインターフェース
 */
interface RakutenApiService {
    @GET("BooksBook/Search/20170404?format=json&sort=sales&hits=30")
    fun salesItems(@Query("booksGenreId") genreId: String, @Query("page") page: String, @Query("applicationId") appId: String): retrofit2.Call<RakutenBookData>

    @GET("BooksBook/Search/20170404?format=json&hits=30")
    fun searchItems(@Query("booksGenreId") genreId: String, @Query("title") title: String, @Query("page") page: String, @Query("applicationId") appId: String): retrofit2.Call<RakutenBookData>
}