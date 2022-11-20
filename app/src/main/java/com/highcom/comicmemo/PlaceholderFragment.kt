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
import com.highcom.comicmemo.datamodel.Comic
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
    private val pageViewModel: ComicPagerViewModel by viewModels {
        ComicPagerViewModelFactory((activity?.application as ComicMemoApplication).repository)
    }
    /** 巻数データ一覧を格納するためのView */
    private var recyclerView: RecyclerView? = null
    /** 巻数データを表示するためのadapter */
    private lateinit var adapter: ComicListAdapter
    /** スワイプメニュー用リスナー */
    private var simpleCallbackHelper: SimpleCallbackHelper? = null
    /** 検索文字列 */
    private var searchViewWord = ""
    /** 0:続刊 1:完結のインデックス */
    var index = 0
        private set

    /**
     * スワイプメニュー用リスナー
     *
     */
    inner class MySimpleCallbackListener : SimpleCallbackListener {
        var fromPos = -1
        var toPos = -1
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
                // 移動元位置は最初のイベント時の値を保持する
                if (fromPos == -1) fromPos = viewHolder.adapterPosition
                // 通知用の移動元位置は毎回更新する
                val notifyFromPos = viewHolder.adapterPosition
                // 移動先位置は最後イベント時の値を保持する
                toPos = target.adapterPosition
                adapter.notifyItemMoved(notifyFromPos, toPos)
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
            ComicListPersistent.lastUpdateId = 0L
            // 入れ替え完了後に最後に一度DBの更新をする
            val rearrangeComicList = adapter.rearrangeComicList(fromPos, toPos)
            pageViewModel.update(rearrangeComicList)
            // 移動位置情報を初期化
            fromPos = -1
            toPos = -1
            // 項目入れ替え後にAdapterを再設定する事で＋ボタンを動作させる
            recyclerView.adapter = adapter
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            index = arguments!!.getInt(ARG_SECTION_NUMBER)
        }
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
        adapter = ComicListAdapter(context, this)
        recyclerView = binding.comicListView
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        recyclerView!!.adapter = adapter
        recyclerView!!.itemAnimator = null

        // 続刊・完結のデータ更新を監視する
        if (index.toLong() == ComicMemoRepository.STATE_CONTINUE) {
            pageViewModel.continueComics.observe(viewLifecycleOwner) { continueComics ->
                continueComics.let {
                    adapter.setOrigComicList(it)
                    setSearchWordFilter(searchViewWord)
                }
            }
        } else if (index.toLong() == ComicMemoRepository.STATE_COMPLETE) {
            pageViewModel.completeComics.observe(viewLifecycleOwner) { completeComics ->
                completeComics.let {
                    adapter.setOrigComicList(it)
                    setSearchWordFilter(searchViewWord)
                }
            }
        }

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
                    viewHolder as ComicListAdapter.ComicViewHolder
                ) { holder, _ ->
                    ComicListPersistent.lastUpdateId = 0L
                    // データベースから削除する
                    val comic = (holder as ComicListAdapter.ComicViewHolder).comic
                    comic?.let { pageViewModel.delete(it.id) }
                })
                underlayButtons.add(UnderlayButton(
                    "編集",
                    0,
                    Color.parseColor("#C7C7CB"),
                    viewHolder
                ) { holder, _ ->
                    ComicListPersistent.lastUpdateId = 0L
                    // 入力画面を生成
                    val intent = Intent(context, InputMemoActivity::class.java)
                    // 選択アイテムを設定
                    val comic = (holder as ComicListAdapter.ComicViewHolder).comic
                    intent.putExtra("EDIT", true)
                    intent.putExtra("COMIC", comic as Serializable)
                    startActivityForResult(intent, 1001)
                })
            }
        }
    }

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
        } else if (enable && !adapter.editEnable) {
            adapter.editEnable = true
            simpleCallbackHelper!!.setSwipeEnable(false)
            recyclerView!!.adapter = adapter
        }
    }

    /**
     * 巻数データ一覧をソートキーでソート処理
     *
     * @param key ソート種別
     */
    fun sortData(key: ComicListPersistent.SortType) {
        adapter.sortComicList(key)
        setSearchWordFilter(searchViewWord)
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
        ComicListPersistent.lastUpdateId = 0L
        // 入力画面を生成
        val intent = Intent(context, InputMemoActivity::class.java)
        // 選択アイテムを設定
        val comic = view.tag as Comic
        intent.putExtra("EDIT", true)
        intent.putExtra("COMIC", comic as Serializable)
        startActivityForResult(intent, 1001)
    }

    /**
     * 続刊・完結状態の選択処理
     *
     * @param view 選択した巻数データView
     * @param status 0:続刊 1:完結
     */
    override fun onAdapterStatusSelected(view: View?, status: Long) {
        val comic = view?.tag as Comic
        comic.let {
            if (it.status != status) {
                ComicListPersistent.lastUpdateId = 0L
                it.status = status
                it.inputdate = DateFormat.format("yyyy/MM/dd", Date()).toString()
                pageViewModel.update(it)
            }
        }
    }

    /**
     * 巻数データの巻数追加ボタン処理
     *
     * @param view 選択した巻数データView
     */
    override fun onAdapterAddBtnClicked(view: View) {
        val comic = view.tag as Comic
        // 巻数を+1する
        var num = comic.number.toInt()
        // 999を上限とする
        if (num >= COMIC_NUM_MAX) {
            return
        }
        num++
        ComicListPersistent.lastUpdateId = comic.id
        comic.number = num.toString()
        comic.inputdate = DateFormat.format("yyyy/MM/dd", Date()) as String
        pageViewModel.update(comic)
    }

    /**
     * 巻数データの削除ボタン処理
     *
     * @param view 選択した巻数データView
     */
    override fun onAdapterDelBtnClicked(view: View) {
        ComicListPersistent.lastUpdateId = 0L
        val comic = view.tag as Comic
        // データベースから削除する
        pageViewModel.delete(comic.id)
    }

    override fun onPause() {
        super.onPause()
        ComicListPersistent.lastUpdateId = 0L
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