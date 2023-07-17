package com.highcom.comicmemo.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.highcom.comicmemo.ComicMemoConstants
import com.highcom.comicmemo.databinding.GridBookItemBinding
import com.highcom.comicmemo.databinding.HeaderBookItemBinding
import com.highcom.comicmemo.databinding.NewBookItemBinding
import com.highcom.comicmemo.network.Item
import com.highcom.comicmemo.viewmodel.RakutenBookViewModel

/**
 * 書籍データの各アイテムをグリッド表示するためのAdapter
 */
class BookDataGridItemAdapter(private val listener: BookItemViewHolder.BookItemListener, private val bookMode: Int) : ListAdapter<Item, BookItemViewHolder>(
    DiffCallback
) {
    companion object DiffCallback : DiffUtil.ItemCallback<Item>() {
        /** 新刊検索のヘッダータイプ */
        const val TYPE_HEADER = 0
        /** 新刊検索のデータタイプ */
        const val TYPE_DATA = 1
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
        return if (bookMode == ComicMemoConstants.BOOK_MODE_NEW && viewType == TYPE_HEADER) {
            BookItemViewHolder(HeaderBookItemBinding.inflate(LayoutInflater.from(parent.context)))
        } else if (bookMode == ComicMemoConstants.BOOK_MODE_NEW && viewType == TYPE_DATA) {
            BookItemViewHolder(NewBookItemBinding.inflate(LayoutInflater.from(parent.context)))
        } else {
            BookItemViewHolder(GridBookItemBinding.inflate(LayoutInflater.from(parent.context)))
        }
    }

    override fun onBindViewHolder(holder: BookItemViewHolder, position: Int) {
        val item = getItem(holder.bindingAdapterPosition)
        holder.bind(item, listener)
    }

    override fun getItemViewType(position: Int): Int {
        val item = super.getItem(position)
        return if (item.Item.size == RakutenBookViewModel.TYPE_HEADER_ITEM) TYPE_HEADER
        else TYPE_DATA
    }

}