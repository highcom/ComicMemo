package com.highcom.comicmemo.network

import androidx.lifecycle.*
import kotlinx.coroutines.launch

enum class RakutenApiStatus { LOADING, ERROR, DONE }

/**
 * 楽天書籍検索ViewModel
 *
 * @property appId 楽天API利用アプリケーションID
 */
class RakutenBookViewModel(private val appId: String) : ViewModel() {
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
    private val _bookData = MutableLiveData<RakutenBookData>()
    // 楽天APIのレスポンスデータ
    val bookData: LiveData<RakutenBookData>
        get() = _bookData

    init {
        getRakutenBookData()
    }

    /**
     * 楽天APIを利用した書籍データ取得処理
     *
     * @param appId 楽天API利用アプリケーションID
     */
    fun getRakutenBookData() {
        // 呼び出される毎にページ番号を更新
        ++page
        viewModelScope.launch {
            _status.value = RakutenApiStatus.LOADING
            RakutenApi.retrofitService.items(page.toString(), appId).enqueue(object : retrofit2.Callback<RakutenBookData> {
                override fun onFailure(call: retrofit2.Call<RakutenBookData>?, t: Throwable?) {
                    _status.value = RakutenApiStatus.ERROR
                }

                override fun onResponse(call: retrofit2.Call<RakutenBookData>?, response: retrofit2.Response<RakutenBookData>) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            _bookData.value = response.body()
                            _status.value = RakutenApiStatus.DONE
                        }
                    }
                }
            })
        }
    }
}