package com.highcom.comicmemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.highcom.comicmemo.databinding.ActivityRakutenBookBinding
import com.highcom.comicmemo.network.RakutenBookData
import com.highcom.comicmemo.network.RakutenBookViewModel
import com.highcom.comicmemo.network.Item

/**
 * 楽天書籍APIを利用した書籍一覧画面のActivity
 */
class RakutenBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRakutenBookBinding
    private lateinit var itemAdapter: BookDataGridItemAdapter
    /** API に問い合わせ中は true になる。 */
    private var nowLoading = true
    private val handler = Handler()

    private val viewModel: RakutenBookViewModel by lazy {
        val factory = RakutenBookViewModel.Factory(getString(R.string.rakuten_app_id))
        ViewModelProvider(this, factory)[RakutenBookViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRakutenBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recyclerView = binding.bookItemGridView
        itemAdapter = BookDataGridItemAdapter()

        recyclerView.adapter = itemAdapter
        recyclerView.addOnScrollListener(InfiniteScrollListener())

        // 楽天書籍データを監視
        viewModel.bookData.observe(this, Observer<RakutenBookData> {
            val items = mutableListOf<Item>()
            // 既に表示しているデータを一度設定
            val currentList = itemAdapter.currentList
            for (item in currentList.iterator()) {
                items.add(item)
            }
            // APIで新しく取得したデータを追加する
            val res = it.Items.iterator()
            for (item in res) {
                items.add(item)
            }

            itemAdapter.submitList(items)
            nowLoading = false
            handler.post { binding.progressBar.visibility = View.INVISIBLE }
        })
    }

    /**
     * リストの下端までスクロールしたタイミングで発火するリスナー。
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

            // 何度もリクエストしないようにロード中は何もしない。
            if (nowLoading) {
                return
            }

            // 以下の条件に当てはまれば一番下までスクロールされたと判断できる。
            if (itemCount == childCount + firstPosition) {
                // API 問い合わせ中は true となる。
                nowLoading = true
                handler.post { binding.progressBar.visibility = View.VISIBLE }
                viewModel.getRakutenBookData()
            }
        }
    }
}