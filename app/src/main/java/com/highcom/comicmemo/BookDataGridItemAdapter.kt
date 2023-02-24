package com.highcom.comicmemo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.highcom.comicmemo.databinding.GridBookItemBinding
import com.highcom.comicmemo.network.Item

/**
 * 書籍データの各アイテムをグリッド表示するためのAdapter
 */
class BookDataGridItemAdapter(private val listener: BookItemViewHolder.BookItemListener) : ListAdapter<Item, BookItemViewHolder>(DiffCallback) {
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
        return BookItemViewHolder(GridBookItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: BookItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, listener)
    }
}