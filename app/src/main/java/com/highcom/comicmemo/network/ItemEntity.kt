package com.highcom.comicmemo.network

import java.io.Serializable

/**
 * 楽天ブックデータ詳細
 *
 * @property affiliateUrl アフィリエイトURL
 * @property author 著者名
 * @property authorKana 著者名カナ
 * @property availability 在庫情報
 * @property booksGenreId 書籍ジャンルID
 * @property chirayomiUrl 試し読みURL
 * @property contents 内容紹介
 * @property discountPrice 割引価格
 * @property discountRate 割引率
 * @property isbn ISBN番号
 * @property itemCaption 商品説明文
 * @property itemPrice 商品価格
 * @property itemUrl 商品URL
 * @property largeImageUrl 大サイズ画像URL
 * @property limitedFlag 限定フラグ
 * @property listPrice 定価
 * @property mediumImageUrl 中サイズ画像URL
 * @property postageFlag 送料フラグ
 * @property publisherName 出版社名
 * @property reviewAverage レビュー平均
 * @property reviewCount レビュー件数
 * @property salesDate 発売日
 * @property seriesName シリーズ名
 * @property seriesNameKana シリーズ名カナ
 * @property size 書籍のサイズ
 * @property smallImageUrl 小サイズ画像URL
 * @property subTitle サブタイトル
 * @property subTitleKana サブタイトルカナ
 * @property title タイトル
 * @property titleKana タイトルカナ
 */
data class ItemEntity(
    val affiliateUrl: String,
    val author: String,
    val authorKana: String,
    val availability: String,
    val booksGenreId: String,
    val chirayomiUrl: String,
    val contents: String,
    val discountPrice: Int,
    val discountRate: Int,
    val isbn: String,
    val itemCaption: String,
    val itemPrice: Int,
    val itemUrl: String,
    val largeImageUrl: String,
    val limitedFlag: Int,
    val listPrice: Int,
    val mediumImageUrl: String,
    val postageFlag: Int,
    val publisherName: String,
    val reviewAverage: String,
    val reviewCount: Int,
    val salesDate: String,
    val seriesName: String,
    val seriesNameKana: String,
    val size: String,
    val smallImageUrl: String,
    val subTitle: String,
    val subTitleKana: String,
    val title: String,
    val titleKana: String
) : Serializable