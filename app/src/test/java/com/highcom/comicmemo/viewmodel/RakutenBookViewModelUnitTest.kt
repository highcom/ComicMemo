package com.highcom.comicmemo.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.highcom.comicmemo.network.*
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import org.junit.Before
import kotlinx.coroutines.test.setMain
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.After
import org.junit.Rule
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior
import junit.framework.Assert.assertEquals
import java.util.concurrent.TimeUnit

/**
 * 楽天書籍検索ViewModelのUnitTest
 *
 */
class RakutenBookViewModelUnitTest {

    // LiveDataをテストするための設定
    // java.lang.RuntimeException: Method getMainLooper in android.os.Looper not mocked. 対策で必要
    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var behavior: NetworkBehavior
    private lateinit var mockRakutenApiService: MockRakutenApiService

    @ExperimentalCoroutinesApi
    @Before
    fun setUp() {
        // viewModelScopeを利用するための設定
        // Exception in thread "main" java.lang.IllegalStateException 対策で必要
        Dispatchers.setMain(Dispatchers.Unconfined)

        // Retrofitのモックライブラリを作成
        val httpLogging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        val httpClientBuilder = OkHttpClient.Builder().addInterceptor(httpLogging)
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://mock.com")
            .client(httpClientBuilder.build())
            .build()

        behavior = NetworkBehavior.create()

        val mockRetrofit = MockRetrofit.Builder(retrofit)
            .networkBehavior(behavior)
            .build()
        val delegate = mockRetrofit.create(RakutenApiService::class.java)
        mockRakutenApiService = MockRakutenApiService(delegate)
    }

    @ExperimentalCoroutinesApi
    @After
    fun tearDown() {
        // viewModelScopeを利用するための設定
        // Exception in thread "main" java.lang.IllegalStateException 対策で必要
        Dispatchers.resetMain()
    }

    /**
     * 楽天APIを利用した書籍データ取得処理の初回呼び出し成功テスト
     *
     */
    @Test
    fun getSalesList_success() {
        behavior.apply {
            setDelay(0, TimeUnit.MILLISECONDS) // 即座に結果が返ってくるようにする
            setVariancePercent(0)
            setFailurePercent(0)
            setErrorPercent(0)
        }
        val target = RakutenBookViewModel.Factory(mockRakutenApiService, "123").create(RakutenBookViewModel::class.java)
        val mockObserver = spyk<Observer<List<Item>?>>()
        target.bookList.observeForever(mockObserver)

        val result = mutableListOf<Item>()
        result.add(Item(
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
        ))

        // LiveDataの値の変更が通知されるまで待つ
        Thread.sleep(1000)
        // 変化通知が呼び出されている事を確認
        verify(exactly = 1) {
            mockObserver.onChanged(result)
        }

        assertEquals(LiveDataKind.SALES, target.liveDataKind)
        assertEquals(1, target.page)
        assertEquals(RakutenApiStatus.DONE, target.status.value)
        assertEquals("機械仕掛けの太陽", target.bookList.value?.first()?.Item?.title )
    }

    /**
     * 楽天APIを利用した書籍データ取得処理の2回呼び出し成功テスト
     *
     */
    @Test
    fun getSalesList_page2_success() {
        behavior.apply {
            setDelay(0, TimeUnit.MILLISECONDS) // 即座に結果が返ってくるようにする
            setVariancePercent(0)
            setFailurePercent(0)
            setErrorPercent(0)
        }
        val target = RakutenBookViewModel.Factory(mockRakutenApiService, "123")
            .create(RakutenBookViewModel::class.java)
        val mockObserver = spyk<Observer<List<Item>?>>()
        target.bookList.observeForever(mockObserver)
        // LiveDataの値の変更が通知されるまで待つ
        Thread.sleep(1000)

        target.getSalesList()
        // LiveDataの値の変更が通知されるまで待つ
        Thread.sleep(1000)

        assertEquals(LiveDataKind.SALES, target.liveDataKind)
        assertEquals(2, target.page)
        assertEquals(RakutenApiStatus.DONE, target.status.value)
        assertEquals("機械仕掛けの太陽", target.bookList.value?.first()?.Item?.title )
    }

    /**
     * 楽天APIを利用した書籍データ取得処理の呼び出し失敗テスト
     *
     */
    @Test
    fun getSalesList_failure() {
        behavior.apply {
            setDelay(0, TimeUnit.MILLISECONDS) // 即座に結果が返ってくるようにする
            setVariancePercent(0)
            setFailurePercent(100) // 100%失敗させる
            setErrorPercent(0)
        }
        val target = RakutenBookViewModel.Factory(mockRakutenApiService, "123").create(RakutenBookViewModel::class.java)
        val mockObserver = spyk<Observer<List<Item>?>>()
        target.bookList.observeForever(mockObserver)
        // LiveDataの値の変更が通知されるまで待つ
        Thread.sleep(1000)

        assertEquals(LiveDataKind.SALES, target.liveDataKind)
        assertEquals(1, target.page)
        assertEquals(RakutenApiStatus.ERROR, target.status.value)
        assertEquals(null, target.bookList.value )
    }

    /**
     * 引数で指定された文字列での書籍データをタイトル検索処理初回呼び出し成功テスト
     *
     */
    @Test
    fun search_success() {
        behavior.apply {
            setDelay(0, TimeUnit.MILLISECONDS) // 即座に結果が返ってくるようにする
            setVariancePercent(0)
            setFailurePercent(0)
            setErrorPercent(0)
        }
        val target = RakutenBookViewModel.Factory(mockRakutenApiService, "123").create(RakutenBookViewModel::class.java)
        val mockObserver = spyk<Observer<List<Item>?>>()
        target.bookList.observeForever(mockObserver)
        // LiveDataの値の変更が通知されるまで待つ
        Thread.sleep(1000)

        target.search("ONE PIECE")
        // LiveDataの値の変更が通知されるまで待つ
        Thread.sleep(1000)

        assertEquals(LiveDataKind.SEARCH, target.liveDataKind)
        assertEquals(1, target.page)
        assertEquals(RakutenApiStatus.DONE, target.status.value)
        assertEquals("ONE PIECE", target.searchWord.value )
        assertEquals("ONE PIECE 105", target.bookList.value?.first()?.Item?.title )
    }

    /**
     * 引数で指定された文字列での書籍データをタイトル検索処理2回呼び出し成功テスト
     *
     */
    @Test
    fun search_page2_success() {
        behavior.apply {
            setDelay(0, TimeUnit.MILLISECONDS) // 即座に結果が返ってくるようにする
            setVariancePercent(0)
            setFailurePercent(0)
            setErrorPercent(0)
        }
        val target = RakutenBookViewModel.Factory(mockRakutenApiService, "123").create(RakutenBookViewModel::class.java)
        val mockObserver = spyk<Observer<List<Item>?>>()
        target.bookList.observeForever(mockObserver)
        // LiveDataの値の変更が通知されるまで待つ
        Thread.sleep(1000)

        target.search("ONE PIECE")
        target.search("ONE PIECE")
        // LiveDataの値の変更が通知されるまで待つ
        Thread.sleep(1000)

        assertEquals(LiveDataKind.SEARCH, target.liveDataKind)
        assertEquals(2, target.page)
        assertEquals(RakutenApiStatus.DONE, target.status.value)
        assertEquals("ONE PIECE", target.searchWord.value )
        assertEquals("ONE PIECE 105", target.bookList.value?.first()?.Item?.title )
    }

    /**
     * 引数で指定された文字列での書籍データをタイトル検索処理呼び出し失敗テスト
     *
     */
    @Test
    fun search_failure() {
        behavior.apply {
            setDelay(0, TimeUnit.MILLISECONDS) // 即座に結果が返ってくるようにする
            setVariancePercent(0)
            setFailurePercent(100) // 100%失敗させる
            setErrorPercent(0)
        }
        val target = RakutenBookViewModel.Factory(mockRakutenApiService, "123").create(RakutenBookViewModel::class.java)
        val mockObserver = spyk<Observer<List<Item>?>>()
        target.bookList.observeForever(mockObserver)
        // LiveDataの値の変更が通知されるまで待つ
        Thread.sleep(1000)

        target.search("ONE PIECE")
        // LiveDataの値の変更が通知されるまで待つ
        Thread.sleep(1000)

        assertEquals(LiveDataKind.SEARCH, target.liveDataKind)
        assertEquals(1, target.page)
        assertEquals(RakutenApiStatus.ERROR, target.status.value)
        assertEquals(null, target.bookList.value )
    }
}