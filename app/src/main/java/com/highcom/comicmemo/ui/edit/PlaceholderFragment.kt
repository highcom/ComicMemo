package com.highcom.comicmemo.ui.edit

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.text.format.DateFormat
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.highcom.comicmemo.ComicMemoConstants
import com.highcom.comicmemo.R
import com.highcom.comicmemo.databinding.FragmentPlaceholderBinding
import com.highcom.comicmemo.datamodel.Comic
import com.highcom.comicmemo.datamodel.ComicMemoRepository
import com.highcom.comicmemo.ui.SimpleCallbackHelper
import com.highcom.comicmemo.ui.edit.ComicListAdapter.AdapterListener
import com.highcom.comicmemo.ui.SimpleCallbackHelper.SimpleCallbackListener
import com.highcom.comicmemo.viewmodel.ComicPagerViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

/**
 * 巻数データ一覧を表示するためのFragment
 *
 */
@AndroidEntryPoint
class PlaceholderFragment : Fragment(), AdapterListener, Filterable {
    private lateinit var binding: FragmentPlaceholderBinding
    /** 巻数一覧を制御するためのViewModel */
    private val pageViewModel: ComicPagerViewModel by activityViewModels()
    /** 巻数データ一覧を格納するためのView */
    private var recyclerView: RecyclerView? = null
    /** 巻数データを表示するためのadapter */
    private lateinit var adapter: ComicListAdapter
    /** スワイプメニュー用ヘルパー */
    private var simpleCallbackHelper: SimpleCallbackHelper? = null
    /** 検索文字列 */
    private var searchViewWord = ""
    /** 0:続刊 1:完結のインデックス */
    var index = 0
        private set
    /** 初期表示かどうか */
    private var isInitPositionSet: Boolean = false
    /** 登録されている巻数一覧データ */
    private var origComicList: List<Comic>? = null
    /** ソートしている種別 */
    private var sortType: ComicListPersistent.SortType = ComicListPersistent.SortType.ID
    /** 巻数一覧更新通知リスナー */
    private var updateComicListListener: UpdateComicListListener? = null

    /**
     * 巻数一覧更新通知リスナーインタフェース
     *
     */
    interface UpdateComicListListener {
        /**
         * 続刊の巻数一覧数更新通知
         *
         * @param count 巻数一覧数
         */
        fun onUpdateContinueComicsCount(count: Int)

        /**
         * 完結の巻数一覧数更新通知
         *
         * @param count 巻数一覧数
         */
        fun onUpdateCompleteComicsCount(count: Int)
    }

    /**
     * スワイプメニュー用リスナー
     *
     */
    inner class MySimpleCallbackListener : SimpleCallbackListener {
        private var fromPos = -1
        private var toPos = -1
        /**
         * 並べ替えイベントコールバック
         *
         * @param viewHolder 元の位置のデータ
         * @param target 移動後の位置のデータ
         * @return 移動したかどうか
         */
        @Suppress("DEPRECATION")
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
            val rearrangeComicList = rearrangeComicList(fromPos, toPos)
            pageViewModel.update(rearrangeComicList)
            // 移動位置情報を初期化
            fromPos = -1
            toPos = -1
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            index = requireArguments().getInt(ComicMemoConstants.ARG_SECTION_NUMBER)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlaceholderBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        isInitPositionSet = false
        adapter = ComicListAdapter(index, pageViewModel, viewLifecycleOwner, this)
        adapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT
        recyclerView = binding.comicListView
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        recyclerView!!.adapter = adapter
        // プラスボタン押下時のアニメーションによるちらつきを止める
        (recyclerView!!.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false

        // 続刊・完結のデータ更新を監視する
        if (index.toLong() == ComicMemoRepository.STATE_CONTINUE) {
            pageViewModel.continueComics.observe(viewLifecycleOwner) { continueComics ->
                continueComics.let {
                    origComicList = it
                    updateComicListListener?.onUpdateContinueComicsCount(origComicList?.size ?: 0)
                    setSearchWordFilter(searchViewWord)
                }
            }
        } else if (index.toLong() == ComicMemoRepository.STATE_COMPLETE) {
            pageViewModel.completeComics.observe(viewLifecycleOwner) { completeComics ->
                completeComics.let {
                    origComicList = it
                    updateComicListListener?.onUpdateCompleteComicsCount(origComicList?.size ?: 0)
                    setSearchWordFilter(searchViewWord)
                }
            }
        }

        val scale = resources.displayMetrics.density
        // 項目スワイプ時の操作を実装する
        simpleCallbackHelper = object : SimpleCallbackHelper(context, recyclerView, scale, MySimpleCallbackListener()) {
            override fun instantiateUnderlayButton(
                viewHolder: RecyclerView.ViewHolder,
                underlayButtons: MutableList<UnderlayButton>
            ) {
                if (viewHolder.itemView.id == R.id.row_footer) return

                underlayButtons.add(UnderlayButton(
                    getString(R.string.swipe_delete),
                    0,
                    Color.parseColor("#FF3C30"),
                    viewHolder as ComicListAdapter.ComicViewHolder
                ) { holder, _ ->
                    // 項目が削除されるためバッファデータをクリアして描画データの再生成
                    clearButtonBuffer()
                    // 最後に更新した項目IDをクリアする
                    ComicListPersistent.lastUpdateId = 0L
                    // データベースから削除する
                    val comic = (holder as ComicListAdapter.ComicViewHolder).comic
                    comic?.let { pageViewModel.delete(it.id) }
                })
                underlayButtons.add(UnderlayButton(
                    getString(R.string.swipe_edit),
                    0,
                    Color.parseColor("#C7C7CB"),
                    viewHolder
                ) { holder, _ ->
                    // 最後に更新した項目IDをクリアする
                    ComicListPersistent.lastUpdateId = 0L
                    // 選択アイテムを設定
                    val comic = (holder as ComicListAdapter.ComicViewHolder).comic
                    val status = comic?.status ?:0L
                    findNavController().navigate(ComicMemoFragmentDirections.actionComicMemoFragmentToInputMemoFragment(
                        isEdit = true, status = status, comic ?: Comic(0, "", "", "", "", "", status)))
                })
            }
        }
    }

    /**
     * 巻数データ追加処理
     *
     * @param comic 巻数データ
     */
    fun insert(comic: Comic) {
        pageViewModel.insert(comic)
    }

    /**
     * 巻数データ更新処理
     *
     * @param comic 巻数データ
     */
    fun update(comic: Comic) {
        pageViewModel.update(comic)
    }

    /**
     * 検索文字列での巻数データ一覧ノフィルタ処理
     *
     * @param word 検索文字列
     */
    fun setSearchWordFilter(word: String) {
        searchViewWord = word
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
        } else if (enable && !adapter.editEnable) {
            adapter.editEnable = true
            simpleCallbackHelper!!.setSwipeEnable(false)
        }
        recyclerView!!.adapter = adapter
    }

    /**
     * 編集状態の有効・無効の取得処理
     *
     * @return 編集状態の有効・無効
     */
    fun getEditEnable(): Boolean {
        return adapter.editEnable
    }

    /**
     * 巻数データ一覧をソートキーでソート処理
     *
     * @param key ソート種別
     */
    fun sortData(key: ComicListPersistent.SortType) {
        sortType = key
        setSearchWordFilter(searchViewWord)
    }

    /**
     * 巻数データ一覧のソート種別取得処理
     *
     * @return ソート種別
     */
    fun getSortType(): ComicListPersistent.SortType {
        return sortType
    }

    /**
     * スクロール位置の設定処理
     *
     * @param position スクロール位置
     */
    fun setSmoothScrollPosition(position: Int) {
        recyclerView?.smoothScrollToPosition(position)
    }

    /**
     * 巻数データ選択時の詳細画面遷移処理
     *
     * @param view 選択した巻数データView
     */
    override fun onAdapterClicked(view: View) {
        ComicListPersistent.lastUpdateId = 0L
        // 選択アイテムを設定
        val comic = view.tag as Comic
        // 編集モードに応じて遷移先を変更
        if (adapter.editEnable) {
            // 入力画面を生成
            findNavController().navigate(ComicMemoFragmentDirections.actionComicMemoFragmentToInputMemoFragment(true, comic.status, comic))
        } else {
            // 参照画面を生成
            findNavController().navigate(ComicMemoFragmentDirections.actionComicMemoFragmentToReferenceMemoFragment(true, comic.status, comic))
        }
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
        if (num >= ComicMemoConstants.COMIC_NUM_MAX) {
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
                            if (orig.title.lowercase().contains(constraint.toString())) results.add(orig)
                        }
                    }
                    oReturn.values = results
                } else {
                    oReturn.values = origComicList
                }
                return oReturn
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(
                constraint: CharSequence?,
                results: FilterResults
            ) {
                val resultList = sortComicList(sortType, results.values as MutableList<Comic>?)
                adapter.submitList(resultList) {
                    // 初期表示の時は先頭位置にする
                    if (!isInitPositionSet) {
                        recyclerView?.scrollToPosition(0)
                        isInitPositionSet = true
                    }
                }
            }
        }
    }

    /**
     * 巻数データ一覧のソート処理
     *
     * @param key ソート種別
     */
    private fun sortComicList(key: ComicListPersistent.SortType, comicList: List<Comic>?): List<Comic>? {
        sortType = key
        // 比較処理の実装
        val comparator = Comparator<Comic> { t1, t2 ->
            var result = when(sortType) {
                ComicListPersistent.SortType.ID -> t1.id.compareTo(t2.id)
                ComicListPersistent.SortType.TITLE -> t1.title.compareTo(t2.title)
                ComicListPersistent.SortType.AUTHOR -> t1.author.compareTo(t2.author)
            }

            // ソート順が決まらない場合には、idで比較する
            if (result == 0) {
                result = t1.id.compareTo(t2.id)
            }
            return@Comparator result
        }
        return comicList?.sortedWith(comparator)
    }

    /**
     * 巻数データ一覧の並べ替え処理
     *
     * @param fromPos 移動元の位置
     * @param toPos 移動先の位置
     */
    private fun rearrangeComicList(fromPos: Int, toPos: Int): List<Comic> {
        val origComicIds = ArrayList<Long>()
        val rearrangeComicList = ArrayList<Comic>()
        // 元のIDの並びを保持と並べ替えができるリストに入れ替える
        origComicList?.let {
            for (comic in origComicList!!) {
                origComicIds.add(comic.id)
                rearrangeComicList.add(comic)
            }
        }
        // 引数で渡された位置で並べ替え
        val fromComic = rearrangeComicList[fromPos]
        rearrangeComicList.removeAt(fromPos)
        rearrangeComicList.add(toPos, fromComic)
        // 再度IDを振り直す
        val itr = origComicIds.listIterator()
        for (comic in rearrangeComicList) {
            comic.id = itr.next()
        }

        return rearrangeComicList
    }

    companion object {
        fun newInstance(index: Int, listener: UpdateComicListListener): PlaceholderFragment {
            val fragment = PlaceholderFragment()
            fragment.updateComicListListener = listener
            val bundle = Bundle()
            bundle.putInt(ComicMemoConstants.ARG_SECTION_NUMBER, index)
            fragment.arguments = bundle
            return fragment
        }
    }
}