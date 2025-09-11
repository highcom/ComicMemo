package com.highcom.comicmemo.network

import java.io.Serializable

/**
 * 楽天ブックデータ詳細
 *
 * @property affiliateUrl
 * @property author
 * @property authorKana
 * @property availability
 * @property booksGenreId
 * @property chirayomiUrl
 * @property contents
 * @property discountPrice
 * @property discountRate
 * @property isbn
 * @property itemCaption
 * @property itemPrice
 * @property itemUrl
 * @property largeImageUrl
 * @property limitedFlag
 * @property listPrice
 * @property mediumImageUrl
 * @property postageFlag
 * @property publisherName
 * @property reviewAverage
 * @property reviewCount
 * @property salesDate
 * @property seriesName
 * @property seriesNameKana
 * @property size
 * @property smallImageUrl
 * @property subTitle
 * @property subTitleKana
 * @property title
 * @property titleKana
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