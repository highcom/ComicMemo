package com.highcom.comicmemo.network

import java.io.Serializable

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