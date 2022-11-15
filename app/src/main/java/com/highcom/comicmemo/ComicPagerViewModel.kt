package com.highcom.comicmemo

import androidx.lifecycle.*
import com.highcom.comicmemo.datamodel.Comic
import com.highcom.comicmemo.datamodel.ComicMemoRepository
import kotlinx.coroutines.launch

/**
 * 巻数データ一覧の操作用ViewModel生成用ファクトリ
 *
 * @property repository 巻数データのデータ操作用リポジトリ
 */
class ComicPagerViewModelFactory(private val repository: ComicMemoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ComicPagerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ComicPagerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

/**
 * 巻数データ一覧の操作用ViewModel
 *
 * @property repository 巻数データのデータ操作用リポジトリ
 */
class ComicPagerViewModel(private val repository: ComicMemoRepository) : ViewModel() {
    val continueComics: LiveData<List<Comic>> = repository.continueComics.asLiveData()
    val completeComics: LiveData<List<Comic>> = repository.completeComics.asLiveData()

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