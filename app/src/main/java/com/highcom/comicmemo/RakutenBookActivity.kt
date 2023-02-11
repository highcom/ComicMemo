package com.highcom.comicmemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.highcom.comicmemo.databinding.ActivityRakutenBookBinding
import com.highcom.comicmemo.network.RakutenBookData
import com.highcom.comicmemo.network.RakutenBookViewModel
import com.highcom.comicmemo.network.Item

/**
 * 楽天書籍APIを利用した書籍一覧画面のActivity
 */
class RakutenBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRakutenBookBinding

    private val viewModel: RakutenBookViewModel by lazy {
        val appId = getString(R.string.rakuten_app_id)
        val factory = RakutenBookViewModel.Factory(appId)
        ViewModelProvider(this, factory)[RakutenBookViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRakutenBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recyclerView = binding.bookItemGridView
        val itemAdapter = BookDataGridItemAdapter()

        recyclerView.adapter = itemAdapter

        // 楽天書籍データを監視
        viewModel.bookData.observe(this, Observer<RakutenBookData> {
            val items = mutableListOf<Item>()
            val res = it.Items.iterator()
            for (item in res) {
                items.add(item)
            }

            itemAdapter.submitList(items)
        })
    }
}