package com.highcom.comicmemo

import androidx.lifecycle.*
import com.highcom.comicmemo.datamodel.Comic
import com.highcom.comicmemo.datamodel.ComicMemoRepository
import kotlinx.coroutines.launch

class ComicPagerViewModelFactory(private val repository: ComicMemoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ComicPagerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ComicPagerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

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
     * 巻数データ一覧の並べ替え処理
     *
     * @param dataIndex 0:続刊 1:完結
     * @param fromPos 移動元の位置
     * @param toPos 移動先の位置
     */
    fun rearrangeData(dataIndex: Long, fromPos: Int, toPos: Int) {
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