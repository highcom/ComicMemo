package com.highcom.comicmemo

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.highcom.comicmemo.ListViewAdapter.AdapterListener
import com.highcom.comicmemo.SimpleCallbackHelper.SimpleCallbackListener
import java.text.SimpleDateFormat
import java.util.*

/**
 * A placeholder fragment containing a simple view.
 */
class PlaceholderFragment : Fragment(), AdapterListener, SimpleCallbackListener {
    var pageViewModel: PageViewModel? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: ListViewAdapter? = null
    private var simpleCallbackHelper: SimpleCallbackHelper? = null
    private var searchViewWord = ""
    var index = 0
        private set
    private var mListData: List<Map<String?, String?>?>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProvider(requireActivity()).get(PageViewModel::class.java)
        if (arguments != null) {
            index = arguments!!.getInt(ARG_SECTION_NUMBER)
        }
        mListData = pageViewModel!!.getListData(index.toLong()).value
    }

    override fun onResume() {
        super.onResume()
        recyclerView!!.adapter = adapter
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_comic_memo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ListViewAdapter(
            context,
            mListData,
            R.layout.row, arrayOf("title", "comment"), intArrayOf(
                android.R.id.text1,
                android.R.id.text2
            ),
            this
        )
        recyclerView = view.findViewById<View>(R.id.comicListView) as RecyclerView
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        recyclerView!!.adapter = adapter
        // セル間に区切り線を実装する
        val itemDecoration: ItemDecoration =
            DividerItemDecoration(context, DividerItemDecoration.Companion.VERTICAL_LIST)
        recyclerView!!.addItemDecoration(itemDecoration)
        val scale = resources.displayMetrics.density
        // ドラックアンドドロップの操作を実装する
        simpleCallbackHelper = object : SimpleCallbackHelper(context, recyclerView, scale, this) {
            override fun instantiateUnderlayButton(
                viewHolder: RecyclerView.ViewHolder,
                underlayButtons: MutableList<UnderlayButton>
            ) {
                underlayButtons.add(UnderlayButton(
                    "削除",
                    0,
                    Color.parseColor("#FF3C30"),
                    viewHolder as ListViewAdapter.ViewHolder
                ) { holder, pos ->
                    ListDataManager.Companion.getInstance().setLastUpdateId(0)
                    // データベースから削除する
                    pageViewModel!!.deleteData(index.toLong(), holder.id.toString())
                    // フィルタしている場合はフィルタデータの一覧も更新する
                    setSearchWordFilter(searchViewWord)
                })
                underlayButtons.add(UnderlayButton(
                    "編集",
                    0,
                    Color.parseColor("#C7C7CB"),
                    viewHolder
                ) { holder, pos ->
                    ListDataManager.Companion.getInstance().setLastUpdateId(0)
                    // 入力画面を生成
                    val intent = Intent(context, InputMemo::class.java)
                    // 選択アイテムを設定
                    intent.putExtra("EDIT", true)
                    intent.putExtra("ID", holder.id!!.toLong())
                    intent.putExtra("TITLE", holder.title.text.toString())
                    intent.putExtra("AUTHOR", holder.author.text.toString())
                    intent.putExtra("NUMBER", holder.number.text.toString())
                    intent.putExtra("MEMO", holder.memo.text.toString())
                    intent.putExtra("STATUS", holder.status!!.toLong())
                    startActivityForResult(intent, 1001)
                })
            }
        }
        pageViewModel!!.getListData(index.toLong()).observe(viewLifecycleOwner) { map ->
            mListData = map
            setSearchWordFilter(searchViewWord)
        }
    }

    fun updateData() {
        pageViewModel!!.updateData(index.toLong())
    }

    fun setSearchWordFilter(word: String) {
        searchViewWord = word
        val filter = (recyclerView!!.adapter as Filterable?)!!.filter
        if (TextUtils.isEmpty(searchViewWord)) {
            filter.filter(null)
        } else {
            filter.filter(searchViewWord)
        }
    }

    fun changeEditEnable() {
        if (adapter.getEditEnable()) {
            adapter.setEditEnable(false)
            simpleCallbackHelper!!.setSwipeEnable(true)
        } else {
            adapter.setEditEnable(true)
            simpleCallbackHelper!!.setSwipeEnable(false)
        }
        recyclerView!!.adapter = adapter
    }

    fun setEditEnable(enable: Boolean) {
        if (!enable && adapter.getEditEnable()) {
            adapter.setEditEnable(false)
            simpleCallbackHelper!!.setSwipeEnable(true)
            recyclerView!!.adapter = adapter
        } else if (enable && !adapter.getEditEnable()) {
            adapter.setEditEnable(true)
            simpleCallbackHelper!!.setSwipeEnable(false)
            recyclerView!!.adapter = adapter
        }
    }

    fun sortData(key: String) {
        pageViewModel!!.sortData(index.toLong(), key)
    }

    override fun onSimpleCallbackMove(
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        if (adapter.getEditEnable() && TextUtils.isEmpty(searchViewWord)) {
            val fromPos = viewHolder.adapterPosition
            val toPos = target.adapterPosition
            adapter!!.notifyItemMoved(fromPos, toPos)
            pageViewModel!!.rearrangeData(index.toLong(), fromPos, toPos)
            return true
        }
        return false
    }

    override fun clearSimpleCallbackView(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ) {
        ListDataManager.Companion.getInstance().setLastUpdateId(0)
        // 項目入れ替え後にAdapterを再設定する事で＋ボタンを動作させる
        recyclerView.adapter = adapter
    }

    override fun onAdapterClicked(view: View, position: Int) {
        // 編集状態でない場合は入力画面に遷移しない
        if (!adapter.getEditEnable()) {
            return
        }
        ListDataManager.Companion.getInstance().setLastUpdateId(0)
        // 入力画面を生成
        val intent = Intent(context, InputMemo::class.java)
        // 選択アイテムを設定
        val holder = view.tag as ListViewAdapter.ViewHolder
        intent.putExtra("EDIT", true)
        intent.putExtra("ID", holder.id!!.toLong())
        intent.putExtra("TITLE", holder.title.text.toString())
        intent.putExtra("AUTHOR", holder.author.text.toString())
        intent.putExtra("NUMBER", holder.number.text.toString())
        intent.putExtra("MEMO", holder.memo.text.toString())
        intent.putExtra("STATUS", holder.status!!.toLong())
        startActivityForResult(intent, 1001)
    }

    override fun onAdapterStatusSelected(view: View?, status: Long) {
        val holder = view!!.tag as ListViewAdapter.ViewHolder
        if (holder.status!!.toLong() == status) return
        ListDataManager.Companion.getInstance().setLastUpdateId(0)
        holder.status = status
        holder.inputdate.text = nowDate

        // データベースを更新する
        val data: MutableMap<String, String> = HashMap()
        data["id"] = holder.id.toString()
        data["title"] = holder.title.text.toString()
        data["author"] = holder.author.text.toString()
        data["number"] = holder.number.text.toString()
        data["memo"] = holder.memo.text.toString()
        data["inputdate"] = holder.inputdate.text.toString()
        data["status"] = holder.status.toString()
        pageViewModel!!.setData(index.toLong(), true, data)
        // フィルタしている場合はフィルタデータの一覧も更新する
        setSearchWordFilter(searchViewWord)
    }

    override fun onAdapterAddBtnClicked(view: View) {
        val holder = view.tag as ListViewAdapter.ViewHolder
        // 巻数を+1する
        var num = holder.number.text.toString().toInt()
        // 999を上限とする
        if (num < 999) {
            num++
            ListDataManager.Companion.getInstance().setLastUpdateId(holder.id!!.toInt())
        }
        holder.number.text = num.toString()
        holder.inputdate.text = nowDate

        // データベースを更新する
        val data: MutableMap<String, String> = HashMap()
        data["id"] = holder.id.toString()
        data["title"] = holder.title.text.toString()
        data["author"] = holder.author.text.toString()
        data["number"] = holder.number.text.toString()
        data["memo"] = holder.memo.text.toString()
        data["inputdate"] = holder.inputdate.text.toString()
        data["status"] = holder.status.toString()
        pageViewModel!!.setData(index.toLong(), true, data)
        // フィルタしている場合はフィルタデータの一覧も更新する
        setSearchWordFilter(searchViewWord)
    }

    override fun onAdapterDelBtnClicked(view: View) {
        ListDataManager.Companion.getInstance().setLastUpdateId(0)
        val holder = view.tag as ListViewAdapter.ViewHolder
        // データベースから削除する
        pageViewModel!!.deleteData(index.toLong(), holder.id.toString())
        // フィルタしている場合はフィルタデータの一覧も更新する
        setSearchWordFilter(searchViewWord)
    }

    override fun onPause() {
        super.onPause()
        ListDataManager.Companion.getInstance().setLastUpdateId(0)
    }

    override fun onDestroy() {
        super.onDestroy()
        pageViewModel!!.closeData()
    }

    private val nowDate: String
        private get() {
            val date = Date()
            val sdf = SimpleDateFormat("yyyy/MM/dd")
            return sdf.format(date)
        }

    companion object {
        private const val ARG_SECTION_NUMBER = "section_number"
        fun newInstance(index: Int): PlaceholderFragment {
            val fragment = PlaceholderFragment()
            val bundle = Bundle()
            bundle.putInt(ARG_SECTION_NUMBER, index)
            fragment.arguments = bundle
            return fragment
        }
    }
}