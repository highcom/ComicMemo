package com.highcom.comicmemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.highcom.comicmemo.network.RakutenBookData
import com.highcom.comicmemo.network.RakutenBookViewModel

class RakutenBookActivity : AppCompatActivity() {

    private val viewModel: RakutenBookViewModel by lazy {
        val appId = getString(R.string.rakuten_app_id)
        val factory = RakutenBookViewModel.Factory(appId)
        ViewModelProvider(this, factory)[RakutenBookViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rakuten_book)

        // 楽天書籍データを監視
        viewModel.bookData.observe(this, Observer<RakutenBookData> {
            val items = mutableListOf<String>()
            val res = it.Items.iterator()
            for (item in res) {
                items.add(item.Item.title)
            }

            val adapter = ArrayAdapter(this@RakutenBookActivity, android.R.layout.simple_list_item_1, items)
            val list: ListView = findViewById(R.id.trend_list_view)
            list.adapter = adapter
        })
    }
}