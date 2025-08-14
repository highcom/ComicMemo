package com.highcom.comicmemo.ui.edit

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.View.OnLongClickListener
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.highcom.comicmemo.R
import com.highcom.comicmemo.databinding.PopupmenuBinding
import com.highcom.comicmemo.databinding.RowBinding
import com.highcom.comicmemo.databinding.RowFooterBinding
import com.highcom.comicmemo.datamodel.Comic
import com.highcom.comicmemo.datamodel.ComicMemoRepository
import com.highcom.comicmemo.viewmodel.ComicPagerViewModel

object ComicListPersistent {
    /**
     * ソート種別
     */
    enum class SortType {
        ID, TITLE, AUTHOR
    }

    /**
     * 最後に巻数を更新したデータのID
     *
     * 巻数を赤文字表示するために利用する
     */
    var lastUpdateId = 0L
}

/**
 * 巻数データ一覧表示用リストアダプタ
 * 続刊用・完結用とそれぞれでインスタンス化される
 *
 * @param index 続刊/完結のインデックス
 * @param viewModel 巻数データ一覧の操作用ViewModel
 * @param lifecycleOwner 巻数データ一覧のライフサイクルオーナー
 * @param listener アダプタ操作用イベントリスナー
 */
class ComicListAdapter (
    index: Int,
    viewModel: ComicPagerViewModel,
    lifecycleOwner: LifecycleOwner,
    listener: AdapterListener
) : ListAdapter<Comic, ViewHolder>(COMIC_COMPARATOR) {
    /** 編集が有効かどうか */
    var editEnable = false
    /** 続刊/完結のインデックス */
    private val state:Int = index
    /** 巻数データ一覧の操作用ViewModel */
    private val pageViewModel: ComicPagerViewModel = viewModel
    /** 巻数データ一覧のライフサイクルオーナー */
    private val pageLifecycleOwner: LifecycleOwner = lifecycleOwner
    /** アダプタの操作イベントリスナー */
    private val adapterListener: AdapterListener = listener
    /** ポップアップメニュー */
    private var popupWindow: PopupWindow? = null
    /** ポップアップメニュー用のView */
    private var popupView: View? = null

    /**
     * アダプタの操作イベントリスナー
     *
     */
    interface AdapterListener {
        fun onAdapterClicked(view: View)
        fun onAdapterStatusSelected(view: View?, status: Long)
        fun onAdapterAddBtnClicked(view: View)
        fun onAdapterDelBtnClicked(view: View)
    }

    /**
     * 巻数データのホルダークラス
     *
     * @property binding 巻数データのバインディング
     */
    @SuppressLint("InflateParams")
    inner class ComicViewHolder(private val binding: RowBinding) : ViewHolder(binding.root) {
        /** バインドしている巻数データ */
        var comic: Comic? = null
        /** 巻数データID */
        var id: Long? = null
        /** 続刊・完結 */
        var status: Long? = null
        /** ポップアップメニュー続刊 */
        private var popupContinue: ToggleButton? = null
        /** ポップアップメニュー完結 */
        private var popupComplete: ToggleButton? = null

        /**
         * データバインド処理
         *
         * @param comic 巻数データ
         */
        fun bind(comic: Comic) {
            this.comic = comic
            id = comic.id
            binding.title.text = comic.title
            binding.author.text = comic.author
            binding.number.text = comic.number
            binding.memo.text = comic.memo
            binding.inputdate.text = comic.inputdate

            if (id!! == ComicListPersistent.lastUpdateId) {
                binding.number.setTextColor(Color.RED)
            } else {
                binding.number.setTextColor(Color.BLACK)
            }
            status = comic.status
            itemView.tag = comic
            itemView.setOnClickListener { view ->
                adapterListener.onAdapterClicked(view)
            }
            // 続刊・完結のポップアップウインドウの設定
            setupPopupWindow(itemView.context, status ?: 0L)
            itemView.setOnLongClickListener(OnLongClickListener { view ->
                if (editEnable) return@OnLongClickListener true
                // 選択されたViewに合わせて高さを調節
                popupWindow?.height = view.height
                // PopupWindowの実装をする　続刊と完結を選択できるようにする
                popupWindow?.showAsDropDown(view, view.width, -view.height)
                // PopupWindowで選択したViewに対して更新できるようにViewを保持する
                popupView = view
                true
            })
        }

        /**
         * ポップアップメニューの設定
         *
         * @param context コンテキスト
         * @param status 続刊・完結の状態
         */
        private fun setupPopupWindow(context: Context, status: Long) {
            popupWindow = PopupWindow(context)

            // PopupWindowに表示するViewを生成
            val popupBinding = PopupmenuBinding.inflate(LayoutInflater.from(context))
            popupWindow?.contentView = popupBinding.root
            popupContinue = popupBinding.popupContinue
            popupComplete = popupBinding.popupComplete
            popupContinue?.setOnCheckedChangeListener { buttonView, _ ->
                setEnableLayoutContinue(buttonView.context)
                adapterListener.onAdapterStatusSelected(popupView, 0)
                popupWindow!!.dismiss()
            }
            popupComplete?.setOnCheckedChangeListener { buttonView, _ ->
                setEnableLayoutComplete(buttonView.context)
                adapterListener.onAdapterStatusSelected(popupView, 1)
                popupWindow!!.dismiss()
            }

            // PopupWindowに表示するViewのサイズを設定
            val width = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                200f,
                context.resources.displayMetrics
            )
            popupWindow?.height = WindowManager.LayoutParams.WRAP_CONTENT
            popupWindow?.width = width.toInt()
            // PopupWindow?の外をタッチしたらPopupWindow?が閉じるように設定
            popupWindow?.isOutsideTouchable = true
            // PopupWindow?外のUIのタッチイベントが走らないようにフォーカスを持っておく
            popupWindow?.isFocusable = true
            // PopupWindow?内のクリックを可能にしておく
            popupWindow?.isTouchable = true
            // レイアウトファイルで設定した背景のさらに背景(黒とか)が生成される為、ここで好みの背景を設定しておく
            popupWindow?.setBackgroundDrawable(
                ColorDrawable(
                    ContextCompat.getColor(
                        context,
                        android.R.color.white
                    )
                )
            )
            // 状態に応じて有効ボタンの表示設定
            if (status == 0L) {
                setEnableLayoutContinue(context)
            } else {
                setEnableLayoutComplete(context)
            }
        }

        /**
         * ポップアップメニューの続刊を有効にする
         *
         * @param context コンテキスト
         */
        @Suppress("DEPRECATION")
        private fun setEnableLayoutContinue(context: Context?) {
            popupContinue?.setTextColor(ContextCompat.getColor(context!!, R.color.white))
            popupContinue?.setBackgroundDrawable(
                context?.let {
                    ContextCompat.getDrawable(
                        it,
                        R.drawable.toggle_select_button
                    )
                }
            )
            popupComplete?.setTextColor(ContextCompat.getColor(context!!, R.color.appcolor))
            popupComplete?.setBackgroundDrawable(
                context?.let {
                    ContextCompat.getDrawable(
                        it,
                        R.drawable.toggle_unselect_button
                    )
                }
            )
        }

        /**
         * ポップアップメニューの完結を有効にする
         *
         * @param context コンテキスト
         */
        @Suppress("DEPRECATION")
        private fun setEnableLayoutComplete(context: Context?) {
            popupContinue?.setTextColor(ContextCompat.getColor(context!!, R.color.appcolor))
            popupContinue?.setBackgroundDrawable(
                context?.let {
                    ContextCompat.getDrawable(
                        it,
                        R.drawable.toggle_unselect_button
                    )
                }
            )
            popupComplete?.setTextColor(ContextCompat.getColor(context!!, R.color.white))
            popupComplete?.setBackgroundDrawable(
                context?.let {
                    ContextCompat.getDrawable(
                        it,
                        R.drawable.toggle_select_button
                    )
                }
            )
        }

        init {
            // カウント追加ボタン処理
            binding.addbutton.setOnClickListener { adapterListener.onAdapterAddBtnClicked(itemView) }

            // 削除ボタン処理
            if (editEnable) {
                binding.deletebutton.visibility = View.VISIBLE
                binding.rearrangebutton.visibility = View.VISIBLE
                // 削除ボタンを押下された行を削除する
                binding.deletebutton.setOnClickListener { adapterListener.onAdapterDelBtnClicked(itemView) }
            } else {
                binding.deletebutton.visibility = View.GONE
                binding.rearrangebutton.visibility = View.GONE
            }
        }
    }

    /**
     * 巻数データフッターのホルダークラス
     *
     * @property viewModel 巻数データ一覧の操作用ViewModel
     * @property binding 巻数データフッターのバインディング
     */
    inner class FooterViewHolder(private val binding: RowFooterBinding, private val viewModel: ComicPagerViewModel) : ViewHolder(binding.root) {

        /**
         * データバインド処理
         *
         */
        @SuppressLint("SetTextI18n")
        fun bind() {
            if (state.toLong() == ComicMemoRepository.STATE_CONTINUE) {
                viewModel.sumContinueNumber.observe(pageLifecycleOwner) {
                    val sum = it ?: 0L
                    binding.sumNumber.text = itemView.context.getString(R.string.sum_number) + sum.toString()
                }
            }
            // 完結巻数合計の表示
            if (state.toLong() == ComicMemoRepository.STATE_COMPLETE) {
                viewModel.sumCompleteNumber.observe(pageLifecycleOwner) {
                    val sum = it ?: 0L
                    binding.sumNumber.text = itemView.context.getString(R.string.sum_number) + sum.toString()
                }
            }
        }
    }

    companion object {
        private const val TYPE_ITEM = 1
        private const val TYPE_FOOTER = 2

        private val COMIC_COMPARATOR = object : DiffUtil.ItemCallback<Comic>() {
            override fun areItemsTheSame(oldItem: Comic, newItem: Comic): Boolean {
                return oldItem.id == newItem.id
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: Comic, newItem: Comic): Boolean {
                return oldItem === newItem
            }
        }
    }

    /**
     * ViewHolderの生成
     *
     * @param parent 親のViewGroup
     * @param viewType アダプタに設定するViewの種別
     * @return
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            TYPE_ITEM -> ComicViewHolder(RowBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            TYPE_FOOTER -> FooterViewHolder(RowFooterBinding.inflate(LayoutInflater.from(parent.context), parent, false), pageViewModel)
            else -> ComicViewHolder(RowBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    /**
     * ViewHolderのバインド
     *
     * @param holder ViewHolderのデータ
     * @param position 一覧データの位置
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ComicViewHolder -> {
                // getItemCountとgetItemをプロパティアクセスすると動作に問題が発生するのでsuperで明示的にメソッドアクセスする
                val current = super.getItem(holder.bindingAdapterPosition)
                holder.bind(current)
            }
            is FooterViewHolder -> {
                holder.bind()
            }
        }
    }

    /**
     * 一覧に表示するデータ数＋フッター行数を返却する
     *
     * @return 表示データ数＋フッター行数
     */
    override fun getItemCount(): Int {
        return if (super.getItemCount() > 0) {
            super.getItemCount() + 1
        } else {
            // 常に総巻数を表示するため、フッター行を表示する
            1
        }
    }

    /**
     * アダプタの位置に対応するViewの種別を返却する
     *
     * @param position アダプタの表示するViewの位置
     * @return Viewの種別
     */
    override fun getItemViewType(position: Int): Int {
        return if (position >= super.getItemCount()) {
            TYPE_FOOTER
        } else {
            TYPE_ITEM
        }
    }
}