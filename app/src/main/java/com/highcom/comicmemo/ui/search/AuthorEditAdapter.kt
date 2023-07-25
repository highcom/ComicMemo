package com.highcom.comicmemo.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.highcom.comicmemo.databinding.HeaderBookItemBinding
import com.highcom.comicmemo.datamodel.Author

/**
 * 著作者名一覧表示用アダプタ
 *
 */
class AuthorEditAdapter : ListAdapter<Author, AuthorEditViewHolder>(DiffCallback) {
    companion object DiffCallback : DiffUtil.ItemCallback<Author>() {
        override fun areItemsTheSame(oldAuthor: Author, newAuthor: Author): Boolean {
            return oldAuthor === newAuthor
        }

        override fun areContentsTheSame(oldAuthor: Author, newAuthor: Author): Boolean {
            return oldAuthor.author == newAuthor.author
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuthorEditViewHolder {
        return AuthorEditViewHolder(HeaderBookItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: AuthorEditViewHolder, position: Int) {
        val item = getItem(holder.bindingAdapterPosition)
        holder.bind(item)
    }
}