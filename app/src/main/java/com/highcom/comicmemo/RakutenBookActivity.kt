package com.highcom.comicmemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.highcom.comicmemo.databinding.ActivityRakutenBookBinding
import com.highcom.comicmemo.network.RakutenBookViewModel

/**
 * 楽天書籍APIを利用した書籍一覧画面のActivity
 */
class RakutenBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRakutenBookBinding
    private lateinit var itemAdapter: BookDataGridItemAdapter
    private val handler = Handler()
    /** API に問い合わせ中は true になる。 */
    private var nowLoading = true
    /** 検索文字列 */
    private var searchWord: String? = null

    private val viewModel: RakutenBookViewModel by lazy {
        val factory = RakutenBookViewModel.Factory(getString(R.string.rakuten_app_id))
        ViewModelProvider(this, factory)[RakutenBookViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRakutenBookBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = getString(R.string.trend_book)

        val recyclerView = binding.bookItemGridView
        itemAdapter = BookDataGridItemAdapter()

        recyclerView.adapter = itemAdapter
        recyclerView.addOnScrollListener(InfiniteScrollListener())

        // 楽天書籍データを監視
        viewModel.bookList.observe(this) {
            itemAdapter.submitList(it)
            nowLoading = false
            handler.post { binding.progressBar.visibility = View.INVISIBLE }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_rakuten_book, menu)
        // 文字列検索の処理を設定
        val searchMenuView = menu?.findItem(R.id.menu_rakuten_book_search_view)
        val searchActionView = searchMenuView?.actionView as SearchView
        searchActionView.queryHint = getString(R.string.query_hint)
        searchActionView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchWord = query
                searchWord?.let {
                    viewModel.search(it)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
        // バツボタンが押下されて検索を終了する場合
        searchActionView.setOnCloseListener {
            searchWord = null
            viewModel.getSalesList()
            false
        }

        return super.onCreateOptionsMenu(menu)
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
                // API 問い合わせ中はtrue
                nowLoading = true
                handler.post { binding.progressBar.visibility = View.VISIBLE }
                if (searchWord != null) {
                    viewModel.search(searchWord!!)
                } else {
                    viewModel.getSalesList()
                }
            }
        }
    }
}