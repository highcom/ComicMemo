package com.highcom.comicmemo

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.TypedValue
import android.view.*
import android.view.View.OnLongClickListener
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

/**
 * 巻数データ一覧用リストアダプタ
 *
 * @param context コンテキスト
 * @param data 巻数データリスト
 * @param resource
 * @param from
 * @param to
 * @param listener コールバック用のリスナー
 */
class ListViewAdapter(
    context: Context?,
    data: List<Map<String, *>>?,
    resource: Int,
    from: Array<String>?,
    to: IntArray?,
    listener: AdapterListener
) : RecyclerView.Adapter<ListViewAdapter.ViewHolder>(), Filterable {
    /** レイアウト */
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    /** 巻数一覧データ */
    private var listData: List<Map<String, *>>?
    /** フィルタ前の巻数一覧データ */
    private var orig: List<Map<String, *>>? = null
    /** 編集が有効かどうか */
    var editEnable = false
    /** アダプタの操作イベントリスナー */
    private val adapterListener: AdapterListener
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
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        /** 巻数データID */
        var id: Long? = null
        /** タイトル名 */
        var title: TextView
        /** 著者名 */
        var author: TextView
        /** 巻数 */
        var number: TextView
        /** メモ */
        var memo: TextView
        /** 入力日付 */
        var inputdate: TextView
        /** 続刊・完結 */
        var status: Long? = null
        /** 巻数追加ボタン */
        var addbtn: Button
        /** 削除ボタン */
        var deletebtn: Button
        /** 並べ替えボタン */
        var rearrangebtn: ImageButton
        /** ポップアップメニュー続刊 */
        var popupContinue: ToggleButton
        /** ポップアップメニュー完結 */
        var popupComplete: ToggleButton

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
            title = itemView.findViewById<View>(R.id.title) as TextView
            author = itemView.findViewById<View>(R.id.author) as TextView
            number = itemView.findViewById<View>(R.id.number) as TextView
            memo = itemView.findViewById<View>(R.id.memo) as TextView
            inputdate = itemView.findViewById<View>(R.id.inputdate) as TextView
            addbtn = itemView.findViewById<View>(R.id.addbutton) as Button

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

    /**
     * ViewHolderの生成
     *
     * @param parent 親のViewGroup
     * @param viewType
     * @return
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.row, parent, false))
    }

    /**
     * ViewHolderのバインド
     *
     * @param holder ViewHolderのデータ
     * @param position 一覧データの位置
     */
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val id = (listData!![position] as HashMap<*, *>?)!!["id"].toString()
        val title = (listData!![position] as HashMap<*, *>?)!!["title"].toString()
        val author = (listData!![position] as HashMap<*, *>?)!!["author"].toString()
        val number = (listData!![position] as HashMap<*, *>?)!!["number"].toString()
        val memo = (listData!![position] as HashMap<*, *>?)!!["memo"].toString()
        val inputdate = (listData!![position] as HashMap<*, *>?)!!["inputdate"].toString()
        val status = (listData!![position] as HashMap<*, *>?)!!["status"].toString()
        holder.id = id.toLong()
        holder.title.text = title
        holder.author.text = author
        holder.number.text = number
        if (holder.id!!.toInt() == ListDataManager.instance?.lastUpdateId ?: 0) {
            holder.number.setTextColor(Color.RED)
        } else {
            holder.number.setTextColor(Color.GRAY)
        }
        holder.memo.text = memo
        holder.inputdate.text = inputdate
        holder.status = status.toLong()
        holder.itemView.tag = holder
        holder.itemView.setOnClickListener { view ->
            adapterListener.onAdapterClicked(
                view,
                position
            )
        }
        holder.itemView.setOnLongClickListener(OnLongClickListener { view ->
            if (editEnable) return@OnLongClickListener true
            // PopupWindowの実装をする　続刊と完結を選択できるようにする
            popupWindow!!.showAsDropDown(view, view.width, -view.height)
            // PopupWindowで選択したViewに対して更新できるようにViewを保持する
            popupView = view
            true
        })
        if (holder.status!!.toLong() == 0L) {
            holder.setEnableLayoutContinue(holder.itemView.context)
        } else {
            holder.setEnableLayoutComplete(holder.itemView.context)
        }
    }

    /**
     * アイテム数取得処理
     *
     * @return アイテム数
     */
    override fun getItemCount(): Int {
        return if (listData != null) {
            listData!!.size
        } else {
            0
        }
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
                val results = ArrayList<Map<String, *>>()
                if (orig == null) orig = listData
                if (constraint != null) {
                    if (orig != null && orig!!.size > 0) {
                        for (g in orig!!) {
                            if (g!!["title"].toString().toLowerCase()
                                    .contains(constraint.toString())
                            ) results.add(g)
                        }
                    }
                    oReturn.values = results
                } else {
                    oReturn.values = orig
                }
                return oReturn
            }

            override fun publishResults(
                constraint: CharSequence?,
                results: FilterResults
            ) {
                listData = results.values as ArrayList<Map<String, String>>
                notifyDataSetChanged()
            }
        }
    }

    init {
        listData = data
        adapterListener = listener
    }
}