package com.highcom.comicmemo.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.highcom.comicmemo.databinding.AuthorEditItemBinding
import com.highcom.comicmemo.datamodel.Author

/**
 * 著作者名一覧表示用アダプタ
 *
 */
class AuthorEditAdapter(val listener: AuthorEditViewHolder.AuthorEditViewHolderListener) : ListAdapter<Author, AuthorEditViewHolder>(DiffCallback) {
    /**
     * 著作者名の変更差分通知用コールバック
     */
    companion object DiffCallback : DiffUtil.ItemCallback<Author>() {
        override fun areItemsTheSame(oldAuthor: Author, newAuthor: Author): Boolean {
            return oldAuthor === newAuthor
        }

        override fun areContentsTheSame(oldAuthor: Author, newAuthor: Author): Boolean {
            return oldAuthor.author == newAuthor.author
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuthorEditViewHolder {
        return AuthorEditViewHolder(AuthorEditItemBinding.inflate(LayoutInflater.from(parent.context)), listener)
    }

    override fun onBindViewHolder(holder: AuthorEditViewHolder, position: Int) {
        val item = getItem(holder.bindingAdapterPosition)
        holder.bind(item)
    }
}