package com.highcom.comicmemo

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.text.format.DateFormat
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.highcom.comicmemo.ComicListAdapter.AdapterListener
import com.highcom.comicmemo.SimpleCallbackHelper.SimpleCallbackListener
import com.highcom.comicmemo.databinding.FragmentComicMemoBinding
import com.highcom.comicmemo.datamodel.ComicMemoRepository
import java.io.Serializable
import java.util.*

/**
 * 巻数データ一覧を表示するためのFragment
 *
 * @property comicPagerViewModel 巻数一覧を制御するためのViewModel
 */
class PlaceholderFragment(private val comicPagerViewModel: ComicPagerViewModel) : Fragment(), AdapterListener {
    private lateinit var binding: FragmentComicMemoBinding
    /** 巻数一覧を制御するためのViewModel */
//    var pageViewModel: PageViewModel? = null
    private val pageViewModel: ComicPagerViewModel by viewModels {
        ComicPagerViewModelFactory((activity?.application as ComicMemoApplication).repository)
    }
    /** 巻数データ一覧を格納するためのView */
    private var recyclerView: RecyclerView? = null
    /** 巻数データを表示するためのadapter */
//    private var adapter: ListViewAdapter? = null
    private lateinit var adapter: ComicListAdapter
    /** スワイプメニュー用リスナー */
    private var simpleCallbackHelper: SimpleCallbackHelper? = null
    /** 検索文字列 */
    private var searchViewWord = ""
    /** 0:続刊 1:完結のインデックス */
    var index = 0
        private set
    /** 巻数データ一覧 */
//    private var mListData: List<Map<String, String>>? = null

    /**
     * スワイプメニュー用リスナー
     *
     */
    inner class MySimpleCallbackListener : SimpleCallbackListener {
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
            if (adapter.editEnable && TextUtils.isEmpty(searchViewWord)) {
                val fromPos = viewHolder.adapterPosition
                val toPos = target.adapterPosition
                adapter.notifyItemMoved(fromPos, toPos)
                // TODO:並べ替えの実装方法を検討
                pageViewModel.rearrangeData(index.toLong(), fromPos, toPos)
                return true
            }
            return false
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
            ListDataManager.instance!!.lastUpdateId = 0
            // 項目入れ替え後にAdapterを再設定する事で＋ボタンを動作させる
            recyclerView.adapter = adapter
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        pageViewModel = ViewModelProvider(requireActivity()).get(PageViewModel::class.java)
        if (arguments != null) {
            index = arguments!!.getInt(ARG_SECTION_NUMBER)
        }
//        mListData = pageViewModel!!.getListData(index.toLong())?.value
    }

    override fun onResume() {
        super.onResume()
        recyclerView!!.adapter = adapter
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentComicMemoBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        adapter = ListViewAdapter(
//            context,
//            mListData,
//            R.layout.row, arrayOf("title", "comment"), intArrayOf(
//                android.R.id.text1,
//                android.R.id.text2
//            ),
//            this
//        )
        adapter = ComicListAdapter(context, this)
        recyclerView = binding.comicListView
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        recyclerView!!.adapter = adapter

        // 続刊・完結のデータ更新を監視する
        if (index.toLong() == ComicMemoRepository.STATE_CONTINUE) {
            pageViewModel.continueComics.observe(viewLifecycleOwner) { continueComics ->
                continueComics.let { adapter.submitList(it)}
            }
        } else if (index.toLong() == ComicMemoRepository.STATE_COMPLETE) {
            pageViewModel.completeComics.observe(viewLifecycleOwner) { completeComics ->
                completeComics.let { adapter.submitList(it) }
            }
        }
        setSearchWordFilter(searchViewWord)

        val scale = resources.displayMetrics.density
        // ドラックアンドドロップの操作を実装する
        simpleCallbackHelper = object : SimpleCallbackHelper(context, recyclerView, scale, MySimpleCallbackListener()) {
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
                    ListDataManager.instance!!.lastUpdateId = 0
                    // データベースから削除する
                    (holder as ComicListAdapter.ComicViewHolder).id?.let { pageViewModel.delete(it) }
                    // フィルタしている場合はフィルタデータの一覧も更新する
                    setSearchWordFilter(searchViewWord)
                })
                underlayButtons.add(UnderlayButton(
                    "編集",
                    0,
                    Color.parseColor("#C7C7CB"),
                    viewHolder
                ) { holder, pos ->
                    ListDataManager.instance!!.lastUpdateId = 0
                    // 入力画面を生成
                    val intent = Intent(context, InputMemoActivity::class.java)
                    // 選択アイテムを設定
                    intent.putExtra("EDIT", true)
                    intent.putExtra("ID", (holder as ListViewAdapter.ViewHolder).id!!.toLong())
                    intent.putExtra("TITLE", holder.title.text.toString())
                    intent.putExtra("AUTHOR", holder.author.text.toString())
                    intent.putExtra("NUMBER", holder.number.text.toString())
                    intent.putExtra("MEMO", holder.memo.text.toString())
                    intent.putExtra("STATUS", holder.status!!.toLong())
                    startActivityForResult(intent, 1001)
                })
            }
        }
//        pageViewModel!!.getListData(index.toLong())?.observe(viewLifecycleOwner) { map ->
//            mListData = map
//            setSearchWordFilter(searchViewWord)
//        }
    }

    /**
     * 巻数データの更新処理
     *
     */
//    fun updateData() {
//        pageViewModel!!.updateData(index.toLong())
//    }

    /**
     * 検索文字列での巻数データ一覧ノフィルタ処理
     *
     * @param word 検索文字列
     */
    fun setSearchWordFilter(word: String) {
        searchViewWord = word
        val filter = (recyclerView!!.adapter as Filterable?)!!.filter
        if (TextUtils.isEmpty(searchViewWord)) {
            filter.filter(null)
        } else {
            filter.filter(searchViewWord)
        }
    }

    /**
     * 編集状態の有効・無効の切り替え処理
     *
     */
    fun changeEditEnable() {
        if (adapter.editEnable) {
            adapter.editEnable = false
            simpleCallbackHelper!!.setSwipeEnable(true)
        } else {
            adapter.editEnable = true
            simpleCallbackHelper!!.setSwipeEnable(false)
        }
        recyclerView!!.adapter = adapter
    }

    /**
     * 編集状態の有効・無効を指定する処理
     *
     * @param enable 編集状態の有効・無効
     */
    fun setEditEnable(enable: Boolean) {
        if (!enable && adapter.editEnable) {
            adapter.editEnable = false
            simpleCallbackHelper!!.setSwipeEnable(true)
            recyclerView!!.adapter = adapter
        } else if (enable && !adapter!!.editEnable) {
            adapter.editEnable = true
            simpleCallbackHelper!!.setSwipeEnable(false)
            recyclerView!!.adapter = adapter
        }
    }

    /**
     * 巻数データ一覧をソートキーでソート処理
     *
     * @param key ソートキー
     */
    fun sortData(key: String) {
        // TODO:ソート方法の検討
//        pageViewModel!!.sortData(index.toLong(), key)
    }

    /**
     * 巻数データ選択時の詳細画面遷移処理
     *
     * @param view 選択した巻数データView
     * @param position 選択位置
     */
    override fun onAdapterClicked(view: View, position: Int) {
        // 編集状態でない場合は入力画面に遷移しない
        if (!adapter.editEnable) {
            return
        }
        ListDataManager.instance!!.lastUpdateId = 0
        // 入力画面を生成
        val intent = Intent(context, InputMemoActivity::class.java)
        // 選択アイテムを設定
        val holder = view.tag as ComicListAdapter.ComicViewHolder
        intent.putExtra("EDIT", true)
        intent.putExtra("COMIC", holder.comic as Serializable)
        startActivityForResult(intent, 1001)
    }

    /**
     * 続刊・完結状態の選択処理
     *
     * @param view 選択した巻数データView
     * @param status 0:続刊 1:完結
     */
    override fun onAdapterStatusSelected(view: View?, status: Long) {
        val holder = view?.tag as ComicListAdapter.ComicViewHolder
        holder.let {
            if (it.status != null && it.status != status) {
                // TODO:最後に更新したIdの設定をどうにかする
                ListDataManager.instance!!.lastUpdateId = 0
                // TODO:以下2行は必要か？
                it.status = status
                it.inputdate.text = DateFormat.format("yyyy/MM/dd", Date())
                it.comic?.status = status
                it.comic?.inputdate = DateFormat.format("yyyy/MM/dd", Date()).toString()
                if (it.comic != null) pageViewModel.update(it.comic!!)
            }
        }

        // フィルタしている場合はフィルタデータの一覧も更新する
        setSearchWordFilter(searchViewWord)
    }

    /**
     * 巻数データの巻数追加ボタン処理
     *
     * @param view 選択した巻数データView
     */
    override fun onAdapterAddBtnClicked(view: View) {
        val holder = view.tag as ComicListAdapter.ComicViewHolder
        // 巻数を+1する
        var num = holder.comic?.number?.toInt()
        // 999を上限とする
        if (num != null) {
            if (num < COMIC_NUM_MAX) {
                num++
                // TODO:どうにかする
                ListDataManager.instance!!.lastUpdateId = holder.id!!.toInt()
            }
            holder.comic?.number = num.toString()
            holder.comic?.inputdate = DateFormat.format("yyyy/MM/dd", Date()) as String
            // TODO:以下2行は必要か？
            holder.number.text = holder.comic?.number
            holder.inputdate.text = holder.comic?.inputdate
            holder.comic?.let { pageViewModel.update(it) }
            // フィルタしている場合はフィルタデータの一覧も更新する
            setSearchWordFilter(searchViewWord)
        }
    }

    /**
     * 巻数データの削除ボタン処理
     *
     * @param view 選択した巻数データView
     */
    override fun onAdapterDelBtnClicked(view: View) {
        ListDataManager.instance!!.lastUpdateId = 0
        val holder = view.tag as ComicListAdapter.ComicViewHolder
        // データベースから削除する
        holder.id?.let { pageViewModel.delete(it) }
        // フィルタしている場合はフィルタデータの一覧も更新する
        setSearchWordFilter(searchViewWord)
    }

    override fun onPause() {
        super.onPause()
        ListDataManager.instance!!.lastUpdateId = 0
    }

    companion object {
        private const val COMIC_NUM_MAX = 999
        private const val ARG_SECTION_NUMBER = "section_number"
        fun newInstance(index: Int, comicPagerViewModel: ComicPagerViewModel): PlaceholderFragment {
            val fragment = PlaceholderFragment(comicPagerViewModel)
            val bundle = Bundle()
            bundle.putInt(ARG_SECTION_NUMBER, index)
            fragment.arguments = bundle
            return fragment
        }
    }
}