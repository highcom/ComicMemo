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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.highcom.comicmemo.R
import com.highcom.comicmemo.datamodel.Comic

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
 * @param context コンテキスト
 * @param listener アダプタ操作用イベントリスナー
 */
class ComicListAdapter (
    context: Context?,
    listener: AdapterListener
) : ListAdapter<Comic, ComicListAdapter.ComicViewHolder>(COMIC_COMPARATOR) {
    /** レイアウト */
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    /** 編集が有効かどうか */
    var editEnable = false
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
     * @param itemView 巻数データのアイテム
     */
    @SuppressLint("InflateParams")
    inner class ComicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        /** バインドしている巻数データ */
        var comic: Comic? = null
        /** 巻数データID */
        var id: Long? = null
        /** タイトル名 */
        var title: TextView? = itemView.findViewById<View>(R.id.title) as? TextView
        /** 著者名 */
        var author: TextView? = itemView.findViewById<View>(R.id.author) as? TextView
        /** 巻数 */
        var number: TextView? = itemView.findViewById<View>(R.id.number) as? TextView
        /** メモ */
        var memo: TextView? = itemView.findViewById<View>(R.id.memo) as? TextView
        /** 入力日付 */
        var inputdate: TextView? = itemView.findViewById<View>(R.id.inputdate) as? TextView
        /** 続刊・完結 */
        var status: Long? = null
        /** 巻数追加ボタン */
        var addbtn: Button? = itemView.findViewById<View>(R.id.addbutton) as? Button
        /** 削除ボタン */
        var deletebtn: Button?
        /** 並べ替えボタン */
        var rearrangebtn: ImageButton?
        /** ポップアップメニュー続刊 */
        var popupContinue: ToggleButton? = null
        /** ポップアップメニュー完結 */
        var popupComplete: ToggleButton? = null

        fun bind(comic: Comic) {
            this.comic = comic
            id = comic.id
            title?.let { it.text = comic.title }
            author?.let { it.text = comic.author }
            number?.let { it.text = comic.number }
            memo?.let { it.text = comic.memo }
            inputdate?.let { it.text = comic.inputdate }

            if (id!! == ComicListPersistent.lastUpdateId) {
                number?.setTextColor(Color.RED)
            } else {
                number?.setTextColor(Color.BLACK)
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
                popupWindow!!.height = view.height
                // PopupWindowの実装をする　続刊と完結を選択できるようにする
                popupWindow!!.showAsDropDown(view, view.width, -view.height)
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
            val contentView =
                LayoutInflater.from(context).inflate(R.layout.popupmenu, null)
            popupWindow!!.contentView = contentView
            popupContinue = contentView.findViewById<View>(R.id.popupContinue) as? ToggleButton
            popupComplete = contentView.findViewById<View>(R.id.popupComplete) as? ToggleButton
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
            popupWindow!!.height = WindowManager.LayoutParams.WRAP_CONTENT
            popupWindow!!.width = width.toInt()
            // PopupWindow!!の外をタッチしたらPopupWindow!!が閉じるように設定
            popupWindow!!.isOutsideTouchable = true
            // PopupWindow!!外のUIのタッチイベントが走らないようにフォーカスを持っておく
            popupWindow!!.isFocusable = true
            // PopupWindow!!内のクリックを可能にしておく
            popupWindow!!.isTouchable = true
            // レイアウトファイルで設定した背景のさらに背景(黒とか)が生成される為、ここで好みの背景を設定しておく
            popupWindow!!.setBackgroundDrawable(
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
            addbtn?.setOnClickListener { adapterListener.onAdapterAddBtnClicked(itemView) }

            // 削除ボタン処理
            deletebtn = itemView.findViewById<View>(R.id.deletebutton) as? Button
            rearrangebtn = itemView.findViewById<View>(R.id.rearrangebutton) as? ImageButton
            if (editEnable) {
                deletebtn?.visibility = View.VISIBLE
                rearrangebtn?.visibility = View.VISIBLE
                // 削除ボタンを押下された行を削除する
                deletebtn?.setOnClickListener { adapterListener.onAdapterDelBtnClicked(itemView) }
            } else {
                deletebtn?.visibility = View.GONE
                rearrangebtn?.visibility = View.GONE
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
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComicViewHolder {
        return when (viewType) {
            TYPE_ITEM -> ComicViewHolder(inflater.inflate(R.layout.row, parent, false))
            TYPE_FOOTER -> ComicViewHolder(inflater.inflate(R.layout.row_footer, parent, false))
            else -> ComicViewHolder(inflater.inflate(R.layout.row_footer, parent, false))
        }
    }

    /**
     * ViewHolderのバインド
     *
     * @param holder ViewHolderのデータ
     * @param position 一覧データの位置
     */
    override fun onBindViewHolder(holder: ComicViewHolder, position: Int) {
        // フッターにはデータをバインドしない
        // getItemCountとgetItemをプロパティアクセスすると動作に問題が発生するのでsuperで明示的にメソッドアクセスする
        if (holder.bindingAdapterPosition >= super.getItemCount()) return
        val current = super.getItem(holder.bindingAdapterPosition)
        holder.bind(current)
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