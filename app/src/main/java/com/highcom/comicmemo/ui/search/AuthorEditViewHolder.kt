package com.highcom.comicmemo.ui.search

import androidx.recyclerview.widget.RecyclerView
import com.highcom.comicmemo.databinding.HeaderBookItemBinding
import com.highcom.comicmemo.datamodel.Author

/**
 * 著作者名表示用のHolder
 *
 * @property binding バインディングデータ
 */
class AuthorEditViewHolder(private var binding: HeaderBookItemBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(author: Author) {
        binding.itemAuthorView.text = author.author
    }
}