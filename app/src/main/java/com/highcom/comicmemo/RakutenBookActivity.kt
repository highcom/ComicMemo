package com.highcom.comicmemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.highcom.comicmemo.databinding.ActivityRakutenBookBinding
import com.highcom.comicmemo.network.RakutenBookViewModel

/**
 * 楽天書籍APIを利用した書籍画面のActivity
 */
class RakutenBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRakutenBookBinding
    /** 検索文字列 */
    private var searchWord: String? = null

    private val viewModel: RakutenBookViewModel by viewModels()

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        return RakutenBookViewModel.Factory(getString(R.string.rakuten_app_id))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRakutenBookBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = getString(R.string.trend_book)

        val navController = findNavController(R.id.rakuten_book_container)
        val navGraph = navController.navInflater.inflate(R.navigation.rakuten_book_navigation)
        navController.setGraph(navGraph, null)
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

}