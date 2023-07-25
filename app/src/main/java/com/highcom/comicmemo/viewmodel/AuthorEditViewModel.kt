package com.highcom.comicmemo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.highcom.comicmemo.datamodel.Author
import com.highcom.comicmemo.datamodel.ComicMemoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 著作者名一覧編集の操作用ViewModel
 *
 * @property repository 著作者名一覧データのデータ操作用リポジトリ
 */
@HiltViewModel
class AuthorEditViewModel @Inject constructor(private val repository: ComicMemoRepository) : ViewModel() {
    val authors: LiveData<List<Author>> = repository.authors.asLiveData()

    /**
     * 著作者の登録処理
     *
     * @param author 著作者
     */
    fun insert(author: Author) = viewModelScope.launch {
        repository.insertAuthor(author)
    }

    /**
     * 著作者の更新
     *
     * @param author 著作者
     */
    fun update(author: Author) = viewModelScope.launch {
        repository.updateAuthor(author)
    }

    /**
     * 著作者リストの更新
     *
     * @param authors 著作者リスト
     */
    fun update(authors: List<Author>) = viewModelScope.launch {
        repository.updateAuthors(authors)
    }

    /**
     * 著作者の削除
     *
     * @param id 削除データID
     */
    fun delete(id: Long) = viewModelScope.launch {
        repository.deleteAuthor(id)
    }
}