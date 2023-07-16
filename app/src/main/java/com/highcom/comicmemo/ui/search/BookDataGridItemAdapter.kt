package com.highcom.comicmemo.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.highcom.comicmemo.ComicMemoConstants
import com.highcom.comicmemo.databinding.GridBookItemBinding
import com.highcom.comicmemo.databinding.NewBookItemBinding
import com.highcom.comicmemo.network.Item

/**
 * 書籍データの各アイテムをグリッド表示するためのAdapter
 */
class BookDataGridItemAdapter(private val listener: BookItemViewHolder.BookItemListener, private val bookMode: Int) : ListAdapter<Item, BookItemViewHolder>(
    DiffCallback
) {
    companion object DiffCallback : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem.Item.title == newItem.Item.title
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): BookItemViewHolder {
        // 検索モードによって生成するレイアウトを変更する
        return if (bookMode == ComicMemoConstants.BOOK_MODE_NEW) {
            BookItemViewHolder(NewBookItemBinding.inflate(LayoutInflater.from(parent.context)))
        } else {
            BookItemViewHolder(GridBookItemBinding.inflate(LayoutInflater.from(parent.context)))
        }
    }

    override fun onBindViewHolder(holder: BookItemViewHolder, position: Int) {
        val item = getItem(holder.bindingAdapterPosition)
        holder.bind(item, listener)
    }
}