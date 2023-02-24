package com.highcom.comicmemo

import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.highcom.comicmemo.databinding.GridBookItemBinding
import com.highcom.comicmemo.network.Item

/**
 * 書籍アイテム表示用のHolder
 *
 * @property binding 設定する書籍アイテム
 */
class BookItemViewHolder(private var binding: GridBookItemBinding): RecyclerView.ViewHolder(binding.root) {
    /**
     * 書籍に対するイベントリスナー
     */
    interface BookItemListener {
        /**
         * 書籍選択時イベント
         */
        fun bookItemSelected()
    }

    fun bind(item: Item, listener: BookItemListener) {
        // 画像URLから画像を表示する
        val imgUri = item.Item.mediumImageUrl.toUri().buildUpon().scheme("https").build()
        Glide.with(binding.itemImageView.context)
            .load(imgUri)
            .apply(
                RequestOptions()
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.ic_baseline_broken_image_24))
            .into(binding.itemImageView)
        // タイトルを設定
        binding.itemTextView.text = item.Item.title

        binding.itemImageView.setOnClickListener {
            listener.bookItemSelected()
        }
    }
}