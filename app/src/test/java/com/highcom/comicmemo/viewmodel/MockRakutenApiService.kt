package com.highcom.comicmemo.viewmodel

import com.highcom.comicmemo.network.Item
import com.highcom.comicmemo.network.ItemEntity
import com.highcom.comicmemo.network.RakutenApiService
import com.highcom.comicmemo.network.RakutenBookData
import retrofit2.Call
import retrofit2.mock.BehaviorDelegate

/**
 * 楽天APIサービスのモッククラス
 *
 * @property delegate
 */
class MockRakutenApiService(private val delegate: BehaviorDelegate<RakutenApiService>) : RakutenApiService {
    override fun salesItems(page: String, appId: String): Call<RakutenBookData> {
        val data = RakutenBookData(
            GenreInformation = arrayListOf(),
            Items = arrayListOf(Item(
                ItemEntity(
                    affiliateUrl = "",
                    author = "知念 実希人",
                    authorKana = "チネン ミキト",
                    availability = "1",
                    booksGenreId = "001004008004",
                    chirayomiUrl = "",
                    contents = "",
                    discountPrice = 0,
                    discountRate = 0,
                    isbn = "9784163916088",
                    itemCaption = "これは未知のウイルスとの戦いに巻き込まれ、“戦場”に身を投じた３人の物語ー大学病院の勤務医で、シングルマザーの椎名梓。同じ病院に勤務する２０代の女性看護師・硲瑠璃子。引退間近の７０代の町医者・長峰邦昭。あのとき医療の現場では何が起こっていたのか？自らも現役医師として現場に立ち続けたからこそ描き出せた圧巻の物語。",
                    itemPrice = 1980,
                    itemUrl = "https://books.rakuten.co.jp/rb/17260498/",
                    largeImageUrl = "https://thumbnail.image.rakuten.co.jp/@0_mall/book/cabinet/6088/9784163916088_1_5.jpg?_ex=200x200",
                    limitedFlag = 0,
                    listPrice = 0,
                    mediumImageUrl = "https://thumbnail.image.rakuten.co.jp/@0_mall/book/cabinet/6088/9784163916088_1_5.jpg?_ex=120x120",
                    postageFlag = 2,
                    publisherName = "文藝春秋",
                    reviewAverage = "3.99",
                    reviewCount = 78,
                    salesDate = "2022年10月24日頃",
                    seriesName = "",
                    seriesNameKana = "",
                    size = "単行本",
                    smallImageUrl = "https://thumbnail.image.rakuten.co.jp/@0_mall/book/cabinet/6088/9784163916088_1_5.jpg?_ex=64x64",
                    subTitle = "",
                    subTitleKana = "",
                    title = "機械仕掛けの太陽",
                    titleKana = "キカイジカケノタイヨウ"
                )
            )),
            carrier = 1,
            count = 100,
            first = 1,
            hits = 30,
            last = 30,
            page = 1,
            pageCount = 4
        )
        return delegate.returningResponse(data).salesItems(page, appId)
    }

    override fun searchItems(title: String, page: String, appId: String): Call<RakutenBookData> {
        val data = RakutenBookData(
            GenreInformation = arrayListOf(),
            Items = arrayListOf(Item(
                ItemEntity(
                    affiliateUrl = "",
                    author = "尾田 栄一郎",
                    authorKana = "オダ エイイチロウ",
                    availability = "1",
                    booksGenreId = "001001001008",
                    chirayomiUrl = "",
                    contents = "",
                    discountPrice = 0,
                    discountRate = 0,
                    isbn = "9784088834368",
                    itemCaption = "",
                    itemPrice = 528,
                    itemUrl = "https://books.rakuten.co.jp/rb/17406316/",
                    largeImageUrl = "https://thumbnail.image.rakuten.co.jp/@0_mall/book/cabinet/4368/9784088834368_1_3.jpg?_ex=200x200",
                    limitedFlag = 0,
                    listPrice = 0,
                    mediumImageUrl = "https://thumbnail.image.rakuten.co.jp/@0_mall/book/cabinet/4368/9784088834368_1_3.jpg?_ex=120x120",
                    postageFlag = 2,
                    publisherName = "集英社",
                    reviewAverage = "4.63",
                    reviewCount = 127,
                    salesDate = "2023年03月03日",
                    seriesName = "ジャンプコミックス",
                    seriesNameKana = "",
                    size = "コミック",
                    smallImageUrl = "https://thumbnail.image.rakuten.co.jp/@0_mall/book/cabinet/4368/9784088834368_1_3.jpg?_ex=64x64",
                    subTitle = "",
                    subTitleKana = "",
                    title = "ONE PIECE 105",
                    titleKana = "ワンピース"
                )
            )),
            carrier = 1,
            count = 100,
            first = 1,
            hits = 30,
            last = 30,
            page = 1,
            pageCount = 4
        )
        return delegate.returningResponse(data).searchItems(title, page, appId)
    }
}