package com.highcom.comicmemo.ui.search

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.highcom.comicmemo.R
import com.highcom.comicmemo.databinding.FragmentAuthorEditBinding
import com.highcom.comicmemo.datamodel.Author
import com.highcom.comicmemo.ui.edit.ComicListPersistent
import com.highcom.comicmemo.ui.SimpleCallbackHelper
import com.highcom.comicmemo.viewmodel.AuthorEditViewModel
import java.util.ArrayList

/**
 * 著作者名の一覧表示と編集用のFragment
 *
 */
class AuthorEditFragment : Fragment(), AuthorEditViewHolder.AuthorEditViewHolderListener {
    private lateinit var binding:FragmentAuthorEditBinding
    private lateinit var authorEditAdapter: AuthorEditAdapter
    /** Activityで生成されたViewModelを利用する */
    private val viewModel: AuthorEditViewModel by activityViewModels()
    /** スワイプメニュー用ヘルパー */
    private var simpleCallbackHelper: SimpleCallbackHelper? = null
    /** 登録されている著作者名一覧データ */
    private var authorList: List<Author>? = null

    /**
     * スワイプメニュー用コールバックリスナー
     */
    inner class AuthorEditCallbackListener : SimpleCallbackHelper.SimpleCallbackListener {
        private var fromPos = -1
        private var toPos = -1
        /**
         * 並べ替えイベントコールバック
         *
         * @param viewHolder 元の位置のデータ
         * @param target 移動後の位置のデータ
         * @return 移動したかどうか
         */
        override fun onSimpleCallbackMove(
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            // 移動元位置は最初のイベント時の値を保持する
            if (fromPos == -1) fromPos = viewHolder.adapterPosition
            // 通知用の移動元位置は毎回更新する
            val notifyFromPos = viewHolder.adapterPosition
            // 移動先位置は最後イベント時の値を保持する
            toPos = target.adapterPosition
            authorEditAdapter.notifyItemMoved(notifyFromPos, toPos)
            return true
        }

        /**
         * 項目入れ替え後の処理
         *
         * @param recyclerView
         * @param viewHolder
         */
        override fun clearSimpleCallbackView(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ) {
            // 入れ替え完了後に最後に一度DBの更新をする
            val authorList = rearrangeAuthorList(fromPos, toPos)
            viewModel.update(authorList)
            // 移動位置情報を初期化
            fromPos = -1
            toPos = -1
        }

        /**
         * 巻数データ一覧の並べ替え処理
         *
         * @param fromPos 移動元の位置
         * @param toPos 移動先の位置
         */
        private fun rearrangeAuthorList(fromPos: Int, toPos: Int): List<Author> {
            val authorIds = ArrayList<Long>()
            val rearrangeAuthorList = ArrayList<Author>()
            // 元のIDの並びを保持と並べ替えができるリストに入れ替える
            for (author in authorList!!) {
                authorIds.add(author.id)
                rearrangeAuthorList.add(author)
            }
            // 引数で渡された位置で並べ替え
            val fromComic = rearrangeAuthorList[fromPos]
            rearrangeAuthorList.removeAt(fromPos)
            rearrangeAuthorList.add(toPos, fromComic)
            // 再度IDを振り直す
            val itr = authorIds.listIterator()
            for (author in rearrangeAuthorList) {
                author.id = itr.next()
            }

            return rearrangeAuthorList
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthorEditBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authorEditAdapter = AuthorEditAdapter(this)
        binding.authorEditView.adapter = authorEditAdapter
        viewModel.authors.observe(viewLifecycleOwner) {
            authorList = it
            authorEditAdapter.submitList(it)
            // 新規作成時は対象のセルにフォーカスされるようにスクロールする
            for (pos in it.indices) {
                if (it.get(pos).author == "") {
                    binding.authorEditView.smoothScrollToPosition(pos)
                    break
                }
            }
        }

        // 著作者名の追加FABを押下で新規データを追加
        binding.authorEditFab.setOnClickListener {
            viewModel.insert(Author(0, ""))
        }

        // 項目スワイプ時の操作を実装する
        simpleCallbackHelper = object : SimpleCallbackHelper(context, binding.authorEditView, resources.displayMetrics.density, AuthorEditCallbackListener()) {
            override fun instantiateUnderlayButton(
                viewHolder: RecyclerView.ViewHolder,
                underlayButtons: MutableList<UnderlayButton>
            ) {
                if (viewHolder.itemView.id == R.id.row_footer) return

                underlayButtons.add(UnderlayButton(
                    getString(R.string.swipe_delete),
                    0,
                    Color.parseColor("#FF3C30"),
                    viewHolder as AuthorEditViewHolder
                ) { holder, _ ->
                    // 項目が削除されるためバッファデータをクリアして描画データの再生成
                    clearButtonBuffer()
                    // 最後に更新した項目IDをクリアする
                    ComicListPersistent.lastUpdateId = 0L
                    // データベースから削除する
                    val author = (holder as AuthorEditViewHolder).author
                    author?.let { viewModel.delete(it.id) }
                })
            }
        }
    }

    override fun onContentsClicked(view: View) {
        view.post {
            binding.authorEditFab.visibility = View.INVISIBLE
            // Viewにフォーカスを当ててソフトキーボードを表示する
            view.isFocusable = true
            view.isFocusableInTouchMode = true
            view.requestFocus()
            (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(view, 0)
        }
    }

    override fun onContentsOutOfFocused(view: View, author: Author?, contents: String) {
        // Viewからフォーカスを外してソフトキーボードを終了する
        (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(getView()?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        view.isFocusable = false
        view.isFocusableInTouchMode = false
        view.requestFocus()
        author?.let {
            // 内容が空白の場合には削除する
            if (contents == "") {
                viewModel.delete(it.id)
                return
            }
            // 著作者名が変更されていたらデータを更新する
            if (it.author != contents) {
                it.author = contents
                viewModel.update(it)
            }
        }
        binding.authorEditFab.visibility = View.VISIBLE
    }
}