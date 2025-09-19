package com.highcom.comicmemo.network

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 楽天APIサービスインターフェース
 */
interface RakutenApiService {
    /**
     * 人気書籍検索
     *
     * @param genreId ジャンルID
     * @param page 参照ページ
     * @param appId アプリID
     * @return 検索結果リスト
     */
    @GET("BooksBook/Search/20170404?format=json&sort=sales&hits=30")
    fun salesItems(@Query("booksGenreId") genreId: String, @Query("page") page: String, @Query("applicationId") appId: String): retrofit2.Call<RakutenBookData>

    /**
     * タイトル名検索
     *
     * @param genreId ジャンルID
     * @param title 検索タイトル
     * @param page 参照ページ
     * @param appId アプリID
     * @return 検索結果リスト
     */
    @GET("BooksBook/Search/20170404?format=json&hits=30")
    fun searchItems(@Query("booksGenreId") genreId: String, @Query("title") title: String, @Query("page") page: String, @Query("applicationId") appId: String): retrofit2.Call<RakutenBookData>

    /**
     * 著作者検索
     *
     * @param author 検索著作者名リスト
     * @param appId アプリID
     * @return 検索結果リスト
     */
    @GET("BooksBook/Search/20170404?format=json&sort=-releaseDate&hits=30")
    fun searchAuthorListItems(@Query("author") author: String, @Query("applicationId") appId: String): retrofit2.Call<RakutenBookData>

    /**
     * ISBN検索
     *
     * @param isbn ISBN番号
     * @param appId アプリID
     * @return 検索結果リスト
     */
    @GET("BooksBook/Search/20170404?format=json&sort=-releaseDate&hits=30")
    fun searchIsbnItems(@Query("isbn") isbn: String, @Query("applicationId") appId: String): retrofit2.Call<RakutenBookData>
}