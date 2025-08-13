package com.highcom.comicmemo.viewmodel

import androidx.lifecycle.*
import com.highcom.comicmemo.datamodel.Comic
import com.highcom.comicmemo.datamodel.ComicMemoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 巻数データ一覧の操作用ViewModel
 *
 * @property repository 巻数データのデータ操作用リポジトリ
 */
@HiltViewModel
class ComicPagerViewModel @Inject constructor(private val repository: ComicMemoRepository) : ViewModel() {
    /** 続刊の巻数データ一覧 */
    val continueComics: LiveData<List<Comic>> = repository.continueComics.asLiveData()
    /** 完結の巻数データ一覧 */
    val completeComics: LiveData<List<Comic>> = repository.completeComics.asLiveData()

    /** 続刊の総巻数 */
    val sumContinueNumber: LiveData<Long> = repository.sumContinueNumber.asLiveData()
    /** 完結の総巻数 */
    val sumCompleteNumber: LiveData<Long> = repository.sumCompleteNumber.asLiveData()

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
     * 巻数データリストの更新
     *
     * @param comics 巻数データリスト
     */
    fun update(comics: List<Comic>) = viewModelScope.launch {
        repository.update(comics)
    }

    /**
     * 巻数データの削除
     *
     * @param id 削除データID
     */
    fun delete(id: Long) = viewModelScope.launch {
        repository.delete(id)
    }

    /**
     * 巻数データの全削除
     *
     */
    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }
}