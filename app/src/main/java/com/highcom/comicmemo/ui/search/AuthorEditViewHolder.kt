package com.highcom.comicmemo.ui.search

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.highcom.comicmemo.databinding.AuthorEditItemBinding
import com.highcom.comicmemo.datamodel.Author

/**
 * 著作者名表示用のHolder
 *
 * @property binding バインディングデータ
 */
class AuthorEditViewHolder(private var binding: AuthorEditItemBinding, listener: AuthorEditViewHolderListener): RecyclerView.ViewHolder(binding.root) {
    var author: Author? = null

    /**
     * 著作者名表示用ViewHolder操作通知用リスナー
     *
     */
    interface AuthorEditViewHolderListener {
        /**
         * 著作者名をタップした時のイベント
         *
         * @param view タップしたView
         */
        fun onContentsClicked(view: View)

        /**
         * 著作者名からフォーカスが外れた時のイベント
         *
         * @param view フォーカスが外れたView
         * @param author Viewが保持している著作者名データ
         * @param contents 変更した著作者名
         */
        fun onContentsOutOfFocused(view: View, author: Author?, contents: String)
    }

    init {
        // 著作者名の領域に対するタップ操作通知イベントを登録する
        binding.itemAuthorText.setOnClickListener {
            listener.onContentsClicked(it)
        }
        // 著作者名の領域に対するフォーカス変更時の通知イベントを登録
        binding.itemAuthorText.setOnFocusChangeListener { view, bFocus ->
            if (bFocus) listener.onContentsClicked(view)
            else listener.onContentsOutOfFocused(view, author, binding.itemAuthorText.text.toString())
        }
    }

    /**
     * 著作者名データのバインド
     *
     * @param author 著作者名データ
     */
    fun bind(author: Author) {
        this.author = author
        binding.itemAuthorText.setText(author.author)
        // 内容が空の場合、新規に作成されたものなので編集状態にする
        if (author.author == "") {
            binding.itemAuthorText.performClick()
        }
    }
}