package com.highcom.comicmemo.viewmodel

import androidx.lifecycle.*
import com.highcom.comicmemo.ComicMemoConstants
import com.highcom.comicmemo.datamodel.Comic
import com.highcom.comicmemo.datamodel.ComicMemoRepository
import com.highcom.comicmemo.network.Item
import com.highcom.comicmemo.network.RakutenApiService
import com.highcom.comicmemo.network.RakutenBookData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class RakutenApiStatus { LOADING, ERROR, DONE }
enum class LiveDataKind { SALES, SEARCH }

/**
 * 楽天書籍検索ViewModel
 *
 * @property repository 巻数データのデータ操作用リポジトリ
 * @property rakutenApiService 楽天APIサービスインターフェース
 */
@HiltViewModel
class RakutenBookViewModel @Inject constructor(private val repository: ComicMemoRepository, private val rakutenApiService: RakutenApiService) : ViewModel() {
    /** 楽天APIアプリケーションID */
    lateinit var appId: String
    /** 検索ジャンルID1 */
    lateinit var genreId: String
    /** LiveDataに設定しているデータ種別 */
    var liveDataKind = LiveDataKind.SALES
    /** 表示ページ数 */
    var page = 0

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

    /**
     * 楽天書籍検索ViewModelを利用するための初期設定処理
     *
     * @param appId 楽天APIアプリID
     * @param bookMode 書籍検索モード
     * @param genreId 検索ジャンルID
     */
    fun initialize(appId: String, bookMode: Int, genreId: String) {
        this.appId = appId
        this.genreId = genreId
        if (bookMode == ComicMemoConstants.BOOK_MODE_NEW) {
            // TODO:検索する著作者名を別途もらうようにする
            searchAuthorList(listOf("尾田栄一郎", "原泰久", "鳴見なる"))
        } else {
            getSalesList()
        }
    }

    /**
     * 作成した巻数データの登録処理
     *
     * @param comic 巻数データ
     */
    fun insert(comic: Comic) = viewModelScope.launch {
        repository.insert(comic)
    }

    /**
     * 巻数データの更新
     *
     * @param comic 巻数データ
     */
    fun update(comic: Comic) = viewModelScope.launch {
        repository.update(comic)
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
     * 引数で指定された著作者名一覧に対する新刊検索処理
     *
     * @param authors 著作者名一覧
     */
    fun searchAuthorList(authors: List<String>) {
        viewModelScope.launch {
            _status.value = RakutenApiStatus.LOADING
            val callbackCounter = object : retrofit2.Callback<RakutenBookData> {
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
            }

            // 検索する著作者名の分だけAPIを呼び出す
            for (author in authors) {
                rakutenApiService.searchAuthorListItems(author, appId).enqueue(callbackCounter)
            }
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