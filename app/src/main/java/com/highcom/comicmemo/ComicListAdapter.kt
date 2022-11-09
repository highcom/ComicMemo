package com.highcom.comicmemo

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
import com.highcom.comicmemo.datamodel.Comic

object ComicListPersistent {
    /**
     * 最後に巻数を更新したデータのID
     *
     * 巻数を赤文字表示するために利用する
     */
    var lastUpdateId = 0L
}

class ComicListAdapter (
    context: Context?,
    listener: AdapterListener
) : ListAdapter<Comic, ComicListAdapter.ComicViewHolder>(COMIC_COMPARATOR), Filterable {
    /** レイアウト */
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    /** 巻数一覧データ */
    private var comicList: List<Comic>? = null
    /** フィルタ前の巻数一覧データ */
    private var origComicList: List<Comic>? = null
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
        fun onAdapterClicked(view: View, position: Int)
        fun onAdapterStatusSelected(view: View?, status: Long)
        fun onAdapterAddBtnClicked(view: View)
        fun onAdapterDelBtnClicked(view: View)
    }

    /**
     * 巻数データのホルダークラス
     *
     * @param itemView 巻数データのアイテム
     */
    inner class ComicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        /** バインドしている巻数データ */
        var comic: Comic? = null
        /** 巻数データID */
        var id: Long? = null
        /** タイトル名 */
        var title: TextView = itemView.findViewById<View>(R.id.title) as TextView
        /** 著者名 */
        var author: TextView = itemView.findViewById<View>(R.id.author) as TextView
        /** 巻数 */
        var number: TextView = itemView.findViewById<View>(R.id.number) as TextView
        /** メモ */
        var memo: TextView = itemView.findViewById<View>(R.id.memo) as TextView
        /** 入力日付 */
        var inputdate: TextView = itemView.findViewById<View>(R.id.inputdate) as TextView
        /** 続刊・完結 */
        var status: Long? = null
        /** 巻数追加ボタン */
        var addbtn: Button = itemView.findViewById<View>(R.id.addbutton) as Button
        /** 削除ボタン */
        var deletebtn: Button
        /** 並べ替えボタン */
        var rearrangebtn: ImageButton
        /** ポップアップメニュー続刊 */
        var popupContinue: ToggleButton
        /** ポップアップメニュー完結 */
        var popupComplete: ToggleButton

        fun bind(comic: Comic) {
            this.comic = comic
            id = comic.id
            title.text = comic.title
            author.text = comic.author
            number.text = comic.number
            memo.text = comic.memo
            inputdate.text = comic.inputdate
            if (id!! == ComicListPersistent.lastUpdateId) {
                number.setTextColor(Color.RED)
            } else {
                number.setTextColor(Color.GRAY)
            }
            status = comic.status
            itemView.tag = comic
            itemView.setOnClickListener { view ->
                adapterListener.onAdapterClicked(
                    view,
                    position
                )
            }
            itemView.setOnLongClickListener(OnLongClickListener { view ->
                if (editEnable) return@OnLongClickListener true
                // PopupWindowの実装をする　続刊と完結を選択できるようにする
                popupWindow!!.showAsDropDown(view, view.width, -view.height)
                // PopupWindowで選択したViewに対して更新できるようにViewを保持する
                popupView = view
                true
            })
            if (status == 0L) {
                setEnableLayoutContinue(itemView.context)
            } else {
                setEnableLayoutComplete(itemView.context)
            }
        }
        /**
         * ポップアップメニューの続刊を有効にする
         *
         * @param context コンテキスト
         */
        fun setEnableLayoutContinue(context: Context?) {
            popupContinue.setTextColor(ContextCompat.getColor(context!!, R.color.white))
            popupContinue.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.toggle_select_button
                )
            )
            popupComplete.setTextColor(ContextCompat.getColor(context, R.color.appcolor))
            popupComplete.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.toggle_unselect_button
                )
            )
        }

        /**
         * ポップアップメニューの完結を有効にする
         *
         * @param context コンテキスト
         */
        fun setEnableLayoutComplete(context: Context?) {
            popupContinue.setTextColor(ContextCompat.getColor(context!!, R.color.appcolor))
            popupContinue.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.toggle_unselect_button
                )
            )
            popupComplete.setTextColor(ContextCompat.getColor(context, R.color.white))
            popupComplete.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.toggle_select_button
                )
            )
        }

        init {

            // カウント追加ボタン処理
            addbtn.setOnClickListener { adapterListener.onAdapterAddBtnClicked(itemView) }

            // 削除ボタン処理
            deletebtn = itemView.findViewById<View>(R.id.deletebutton) as Button
            rearrangebtn = itemView.findViewById<View>(R.id.rearrangebutton) as ImageButton
            if (editEnable) {
                deletebtn.visibility = View.VISIBLE
                rearrangebtn.visibility = View.VISIBLE
                // 削除ボタンを押下された行を削除する
                deletebtn.setOnClickListener { adapterListener.onAdapterDelBtnClicked(itemView) }
            } else {
                deletebtn.visibility = View.GONE
                rearrangebtn.visibility = View.GONE
            }
            popupWindow = PopupWindow(itemView.context)

            // PopupWindowに表示するViewを生成
            val contentView =
                LayoutInflater.from(itemView.context).inflate(R.layout.popupmenu, null)
            popupWindow!!.setContentView(contentView)
            popupContinue = contentView.findViewById<View>(R.id.popupContinue) as ToggleButton
            popupComplete = contentView.findViewById<View>(R.id.popupComplete) as ToggleButton
            popupContinue.setOnCheckedChangeListener { buttonView, isChecked ->
                setEnableLayoutContinue(buttonView.context)
                adapterListener.onAdapterStatusSelected(popupView, 0)
                popupWindow!!.dismiss()
            }
            popupComplete.setOnCheckedChangeListener { buttonView, isChecked ->
                setEnableLayoutComplete(buttonView.context)
                adapterListener.onAdapterStatusSelected(popupView, 1)
                popupWindow!!.dismiss()
            }

            // PopupWindowに表示するViewのサイズを設定
            val width = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                200f,
                itemView.context.resources.displayMetrics
            )
            popupWindow!!.setHeight(WindowManager.LayoutParams.WRAP_CONTENT)
            popupWindow!!.setWidth(width.toInt())
            // PopupWindow!!の外をタッチしたらPopupWindow!!が閉じるように設定
            popupWindow!!.setOutsideTouchable(true)
            // PopupWindow!!外のUIのタッチイベントが走らないようにフォーカスを持っておく
            popupWindow!!.setFocusable(true)
            // PopupWindow!!内のクリックを可能にしておく
            popupWindow!!.setTouchable(true)
            // レイアウトファイルで設定した背景のさらに背景(黒とか)が生成される為、ここで好みの背景を設定しておく
            popupWindow!!.setBackgroundDrawable(
                ColorDrawable(
                    ContextCompat.getColor(
                        itemView.context,
                        android.R.color.white
                    )
                )
            )
        }
    }

    companion object {
        private val COMIC_COMPARATOR = object : DiffUtil.ItemCallback<Comic>() {
            override fun areItemsTheSame(oldItem: Comic, newItem: Comic): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: Comic, newItem: Comic): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }

    /**
     * フィルタ前の関数データ一覧設定
     *
     * @param list フィルタ前の関数データ一覧
     */
    fun setOrigComicList(list: List<Comic>?) {
        origComicList = list
    }

    /**
     * ViewHolderの生成
     *
     * @param parent 親のViewGroup
     * @param viewType
     * @return
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComicViewHolder {
        return ComicViewHolder(inflater.inflate(R.layout.row, parent, false))
    }

    /**
     * ViewHolderのバインド
     *
     * @param holder ViewHolderのデータ
     * @param position 一覧データの位置
     */
    override fun onBindViewHolder(holder: ComicViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    /**
     * 巻数データ一覧を設定
     *
     * @param list 巻数データ一覧
     */
    override fun submitList(list: List<Comic>?) {
        super.submitList(list)
        comicList = list
    }

    /**
     * 検索文字列での一覧のフィルタ処理
     *
     * @return フィルタした結果
     */
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val oReturn = FilterResults()
                val results = ArrayList<Comic>()
                if (constraint != null) {
                    if (origComicList!!.isNotEmpty()) {
                        for (orig in origComicList!!) {
                            if (orig.title.toLowerCase()
                                    .contains(constraint.toString())
                            ) results.add(orig)
                        }
                    }
                    oReturn.values = results
                } else {
                    oReturn.values = origComicList
                }
                return oReturn
            }

            override fun publishResults(
                constraint: CharSequence?,
                results: FilterResults
            ) {
                val resultList = results.values as List<Comic>?
                resultList?.let {
                    submitList(resultList)
                }
            }
        }
    }

}