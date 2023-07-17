package com.highcom.comicmemo.network

data class RakutenBookData(
    val GenreInformation: List<Any>,
    val Items: ArrayList<Item>,
    val carrier: Int,
    val count: Int,
    val first: Int,
    val hits: Int,
    val last: Int,
    val page: Int,
    val pageCount: Int
)