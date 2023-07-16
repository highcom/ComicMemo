package com.highcom.comicmemo.ui.search

import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.highcom.comicmemo.R
import com.highcom.comicmemo.databinding.GridBookItemBinding
import com.highcom.comicmemo.databinding.NewBookItemBinding
import com.highcom.comicmemo.network.Item

/**
 * 書籍アイテム表示用のHolder
 *
 */
class BookItemViewHolder: RecyclerView.ViewHolder {
    /**
     * 人気書籍検索用書籍アイテム表示コンストラクタ
     */
    constructor(binding: GridBookItemBinding): super(binding.root) {
        gridBookItemBinding = binding
    }
    /**
     * 新刊書籍検索用書籍アイテム表示コンストラクタ
     */
    constructor(binding: NewBookItemBinding): super(binding.root) {
        newBookItemBinding = binding
    }

    private var gridBookItemBinding: GridBookItemBinding? = null
    private var newBookItemBinding: NewBookItemBinding? = null

    /**
     * 書籍に対するイベントリスナー
     */
    interface BookItemListener {
        /**
         * 書籍選択時イベント
         */
        fun bookItemSelected(item: Item)
    }

    fun bind(item: Item, listener: BookItemListener) {
        // 人気書籍検索の場合
        gridBookItemBinding?.also { binding ->
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
                listener.bookItemSelected(item)
            }
        }
        // 新刊書籍検索の場合
        newBookItemBinding?.also { binding ->
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
            binding.itemTitleView.text = item.Item.title
            // 発売日を設定
            binding.itemReleaseView.text = item.Item.salesDate

            binding.itemImageView.setOnClickListener {
                listener.bookItemSelected(item)
            }
        }
    }
}