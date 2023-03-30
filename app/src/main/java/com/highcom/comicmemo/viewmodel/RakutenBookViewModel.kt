package com.highcom.comicmemo.viewmodel

import androidx.lifecycle.*
import com.highcom.comicmemo.network.Item
import com.highcom.comicmemo.network.RakutenApiService
import com.highcom.comicmemo.network.RakutenBookData
import kotlinx.coroutines.launch

enum class RakutenApiStatus { LOADING, ERROR, DONE }
enum class LiveDataKind { SALES, SEARCH }

/**
 * 楽天書籍検索ViewModel
 *
 * @property appId 楽天API利用アプリケーションID
 */
class RakutenBookViewModel(private val rakutenApiService: RakutenApiService, private val appId: String, private var genreId: String) : ViewModel() {
    /** LiveDataに設定しているデータ種別 */
    var liveDataKind = LiveDataKind.SALES
    /** 表示ページ数 */
    var page = 0
    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val apiService: RakutenApiService,
        private val id: String,
        private val genreId: String
    ) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RakutenBookViewModel(rakutenApiService = apiService, appId = id, genreId = genreId) as T
        }
    }

    /** 検索ワードを保持する内部変数 */
    private val _searchWord = MutableLiveData<String?>()
    /** 検索ワード */
    val searchWord: LiveData<String?>
        get() = _searchWord

    /** 楽天APIのステータスを保持する内部変数 */
    private val _status = MutableLiveData<RakutenApiStatus>()
    /** 楽天APIのステータス */
    val status: LiveData<RakutenApiStatus>
        get() = _status

    /** 楽天APIのレスポンスデータを保持する内部変数 */
    private val _bookList = MutableLiveData<List<Item>?>()
    /** 楽天APIのレスポンスデータ */
    val bookList: LiveData<List<Item>?>
        get() = _bookList

    init {
        getSalesList()
    }

    /**
     * 検索文字列をクリアする
     */
    fun clearSerachWord() {
        _searchWord.value = null
    }

    /**
     * 楽天APIを利用した書籍データ取得処理
     *
     * @param id 楽天API検索ジャンルID
     */
    fun getSalesList(id: String = genreId) {
        // APIの仕様で100ページを超える場合はエラーとなるので呼び出さない
        if (page >= MAX_PAGE_COUNT) return
        // 他の種別でLiveDataが設定されていた場合は初期ページから取得
        if (liveDataKind != LiveDataKind.SALES || genreId != id) {
            liveDataKind = LiveDataKind.SALES
            _bookList.value = null
            page = 0
            // 検索ジャンルIDの設定
            genreId = id
        }
        // 呼び出される毎にページ番号を更新
        ++page
        viewModelScope.launch {
            _status.value = RakutenApiStatus.LOADING
            rakutenApiService.salesItems(genreId, page.toString(), appId).enqueue(object : retrofit2.Callback<RakutenBookData> {
                override fun onFailure(call: retrofit2.Call<RakutenBookData>?, t: Throwable?) {
                    _status.value = RakutenApiStatus.ERROR
                }

                override fun onResponse(call: retrofit2.Call<RakutenBookData>?, response: retrofit2.Response<RakutenBookData>) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            _status.value = RakutenApiStatus.DONE
                            setBookList(it)
                        }
                    }
                }
            })
        }
    }

    /**
     * 引数で指定された文字列での書籍データをタイトル検索処理
     *
     * @param word 検索文字列
     */
    fun search(word: String) {
        // APIの仕様で100ページを超える場合はエラーとなるので呼び出さない
        if (page >= MAX_PAGE_COUNT) return
        // 他の種別でLiveDataが設定されていたか検索ワードが変わった場合は初期ページから取得
        if (liveDataKind != LiveDataKind.SEARCH || !_searchWord.value.equals(word)) {
            liveDataKind = LiveDataKind.SEARCH
            _searchWord.value = word
            _bookList.value = null
            page = 0
        }
        // 呼び出される毎にページ番号を更新
        ++page
        viewModelScope.launch {
            _status.value = RakutenApiStatus.LOADING
            rakutenApiService.searchItems(genreId, _searchWord.value ?: "", page.toString(), appId).enqueue(object : retrofit2.Callback<RakutenBookData> {
                override fun onFailure(call: retrofit2.Call<RakutenBookData>?, t: Throwable?) {
                    _status.value = RakutenApiStatus.ERROR
                }

                override fun onResponse(call: retrofit2.Call<RakutenBookData>?, response: retrofit2.Response<RakutenBookData>) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            _status.value = RakutenApiStatus.DONE
                            setBookList(it)
                        }
                    }
                }
            })
        }
    }

    /**
     * 楽天APIレスポンスデータから書籍データリストに設定する処理
     *
     * @param data レスポンスデータ
     */
    private fun setBookList(data: RakutenBookData) {
        val items = mutableListOf<Item>()
        // 既に表示しているデータを一度設定
        _bookList.value?.let {
            for (item in it.iterator()) {
                items.add(item)
            }
        }
        // APIで新しく取得したデータを追加する
        val res = data.Items.iterator()
        for (item in res) {
            items.add(item)
        }

        _bookList.value = items
    }

    companion object {
        /** 検索ジャンル：漫画（コミック） */
        const val GENRE_ID_COMIC = "001001"
        /** 検索ジャンル：小説・エッセイ */
        const val GENRE_ID_NOVEL = "001004"
        /** 検索ジャンル：ライトノベル */
        const val GENRE_ID_LIGHT_NOVEL = "001017"
        /** 検索ジャンル：文庫 */
        const val GENRE_ID_PAPERBACK = "001019"
        /** 検索ジャンル：新書 */
        const val GENRE_ID_NEW_BOOK = "001020"
        /** 検索結果の最大ページ数 */
        private const val MAX_PAGE_COUNT = 100
    }
}