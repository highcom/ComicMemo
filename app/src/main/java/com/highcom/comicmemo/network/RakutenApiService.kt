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
     * @return 検索結果リスト
     */
    @GET("/books/popular")
    fun salesItems(@Query("booksGenreId") genreId: String, @Query("page") page: String): retrofit2.Call<RakutenBookData>

    /**
     * タイトル名検索
     *
     * @param genreId ジャンルID
     * @param title 検索タイトル
     * @param page 参照ページ
     * @return 検索結果リスト
     */
    @GET("books/title")
    fun searchItems(@Query("booksGenreId") genreId: String, @Query("title") title: String, @Query("page") page: String): retrofit2.Call<RakutenBookData>

    /**
     * 著作者検索
     *
     * @param author 検索著作者名リスト
     * @return 検索結果リスト
     */
    @GET("books/author")
    fun searchAuthorListItems(@Query("author") author: String): retrofit2.Call<RakutenBookData>

    /**
     * ISBN検索
     *
     * @param isbn ISBN番号
     * @return 検索結果リスト
     */
    @GET("books/isbn")
    fun searchIsbnItems(@Query("isbn") isbn: String): retrofit2.Call<RakutenBookData>
}