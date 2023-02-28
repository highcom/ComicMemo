package com.highcom.comicmemo

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.highcom.comicmemo.databinding.FragmentBookListBinding
import com.highcom.comicmemo.network.Item
import com.highcom.comicmemo.network.RakutenApiStatus
import com.highcom.comicmemo.network.RakutenBookViewModel

/**
 * 楽天APIを利用して取得した書籍の一覧を表示するFragment
 *
 */
class BookListFragment : Fragment(), BookItemViewHolder.BookItemListener {
    private lateinit var binding: FragmentBookListBinding
    private lateinit var itemAdapter: BookDataGridItemAdapter
    private val handler = Handler()
    /** API に問い合わせ中は true になる。 */
    private var nowLoading = true
    /** 検索文字列 */
    private var searchWord: String? = null
    /** Activityで生成されたViewModelを利用する */
    private val viewModel: RakutenBookViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = binding.bookItemGridView
        itemAdapter = BookDataGridItemAdapter(this)

        recyclerView.adapter = itemAdapter
        recyclerView.addOnScrollListener(InfiniteScrollListener())

        // 楽天APIの呼び出し状況に応じてプログレスサークルの表示
        viewModel.status.observe(viewLifecycleOwner) { apiStatus ->
            when (apiStatus) {
                RakutenApiStatus.LOADING -> {
                    nowLoading = true
                    handler.post { binding.progressBar.visibility = View.VISIBLE }
                }
                RakutenApiStatus.DONE, RakutenApiStatus.ERROR -> {
                    nowLoading = false
                    handler.post { binding.progressBar.visibility = View.INVISIBLE }
                }
            }
        }

        // 楽天書籍データを監視
        viewModel.bookList.observe(viewLifecycleOwner) {
            itemAdapter.submitList(it)
        }
    }

    /**
     * リストの下端までスクロールしたタイミングで発火するリスナー
     */
    inner class InfiniteScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            // アダプターが保持しているアイテムの合計
            val itemCount = itemAdapter.itemCount
            // 画面に表示されているアイテム数
            val childCount = recyclerView.childCount
            val manager = recyclerView.layoutManager as LinearLayoutManager
            // 画面に表示されている一番上のアイテムの位置
            val firstPosition = manager.findFirstVisibleItemPosition()

            // 何度もリクエストしないようにロード中は何もしない
            if (nowLoading) {
                return
            }

            // 一番下までスクロールされた場合
            if (itemCount == childCount + firstPosition) {
                if (searchWord != null) {
                    viewModel.search(searchWord!!)
                } else {
                    viewModel.getSalesList()
                }
            }
        }
    }

    /**
     * 書籍詳細画面への遷移処理
     */
    override fun bookItemSelected(item: Item) {
        findNavController().navigate(R.id.action_bookListFragment_to_bookDetailFragment, bundleOf("BUNDLE_ITEM_DATA" to item))
    }
}