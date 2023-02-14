package com.highcom.comicmemo.network

import androidx.lifecycle.*
import kotlinx.coroutines.launch

enum class RakutenApiStatus { LOADING, ERROR, DONE }
enum class LiveDataKind { SALES, SEARCH }

/**
 * 楽天書籍検索ViewModel
 *
 * @property appId 楽天API利用アプリケーションID
 */
class RakutenBookViewModel(private val appId: String) : ViewModel() {
    /** LiveDataに設定しているデータ種別 */
    var liveDataKind = LiveDataKind.SALES
    /** 検索ワード */
    var searchWord = ""
    /** 表示ページ数 */
    var page = 0
    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val id: String
    ) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return RakutenBookViewModel(appId = id) as T
        }
    }

    // 楽天APIのステータスを保持する内部変数
    private val _status = MutableLiveData<RakutenApiStatus>()
    // 楽天APIのステータス
    val status: LiveData<RakutenApiStatus>
        get() = _status

    // 楽天APIのレスポンスデータを保持する内部変数
    private val _bookList = MutableLiveData<List<Item>>()
    // 楽天APIのレスポンスデータ
    val bookList: LiveData<List<Item>>
        get() = _bookList

    init {
        getSalesList()
    }

    /**
     * 楽天APIを利用した書籍データ取得処理
     *
     * @param appId 楽天API利用アプリケーションID
     */
    fun getSalesList() {
        // APIの仕様で100ページを超える場合はエラーとなるので呼び出さない
        if (page >= MAX_PAGE_COUNT) return
        // 他の種別でLiveDataが設定されていた場合は初期ページから取得
        if (liveDataKind != LiveDataKind.SALES) {
            liveDataKind = LiveDataKind.SALES
            _bookList.value = null
            page = 0
        }
        // 呼び出される毎にページ番号を更新
        ++page
        viewModelScope.launch {
            _status.value = RakutenApiStatus.LOADING
            RakutenApi.retrofitService.salesItems(page.toString(), appId).enqueue(object : retrofit2.Callback<RakutenBookData> {
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
        if (liveDataKind != LiveDataKind.SEARCH || searchWord != word) {
            liveDataKind = LiveDataKind.SEARCH
            searchWord = word
            _bookList.value = null
            page = 0
        }
        // 呼び出される毎にページ番号を更新
        ++page
        viewModelScope.launch {
            _status.value = RakutenApiStatus.LOADING
            RakutenApi.retrofitService.searchItems(searchWord, page.toString(), appId).enqueue(object : retrofit2.Callback<RakutenBookData> {
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
        private const val MAX_PAGE_COUNT = 100
    }
}