package com.highcom.comicmemo

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.highcom.comicmemo.databinding.ActivityRakutenBookBinding
import com.highcom.comicmemo.datamodel.Comic
import com.highcom.comicmemo.network.RakutenBookViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * 楽天書籍APIを利用した書籍画面のActivity
 */
class RakutenBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRakutenBookBinding
    /** 人気書籍検索画面のViewModel */
    private val viewModel: RakutenBookViewModel by viewModels()
    /** AdMob広告 */
    private var mAdView: AdView? = null

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

        // アクションバーの戻るボタンを表示
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // 広告のロード
        binding.adViewBookListFrame.post { loadBanner() }
    }

    /**
     * AdMobバナー広告のロード
     */
    private fun loadBanner() {
        // 広告リクエストの生成
        mAdView = AdView(this)
        mAdView?.adUnitId = getString(R.string.admob_book_list_id)
        binding.adViewBookListFrame.removeAllViews()
        binding.adViewBookListFrame.addView(mAdView)
        mAdView?.setAdSize(adSize)
        val adRequest = AdRequest.Builder().build()

        // 広告のロード
        mAdView?.loadAd(adRequest)
    }

    /**
     * 広告の幅に使用する画面の幅 (装飾を減らしたもの) を決定する
     */
    private val adSize: AdSize
        get() {
            // 広告の幅に使用する画面の幅 (装飾を減らしたもの) を決定
            val display = windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)
            val density = outMetrics.density
            var adWidthPixels = binding.adViewBookListFrame.width.toFloat()

            // 広告がレイアウトされていない場合は、デフォルトで全画面幅にする
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }
            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        }

    /**
     * アクションバーのメニュー生成処理
     *
     * @param menu メニュー
     * @return
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_rakuten_book, menu)
        // 文字列検索の処理を設定
        val searchMenuView = menu?.findItem(R.id.menu_rakuten_book_search_view)
        val searchActionView = searchMenuView?.actionView as SearchView
        searchActionView.queryHint = getString(R.string.query_hint)
        searchActionView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
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
            viewModel.clearSerachWord()
            viewModel.getSalesList()
            false
        }

        return super.onCreateOptionsMenu(menu)
    }

    /**
     * アクションバーのメニュー選択処理
     *
     * @param item 選択項目
     * @return
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                findNavController(R.id.rakuten_book_container).run {
                    when (currentDestination?.id) {
                        R.id.bookListFragment -> finish()
                        else -> popBackStack()
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 入力画面終了時の結果
     *
     * @param requestCode リクエストコード
     * @param resultCode　結果コード
     * @param data インテント
     */
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 入力完了意外で戻ってきた場合は登録しない
        if (resultCode != Activity.RESULT_OK) return

        val comic = data?.getSerializableExtra("COMIC") as? Comic
        if (comic != null) {
            GlobalScope.launch {
                // idが0の場合は新規作成でDBのautoGenerateで自動採番される
                if (comic.id == 0L) {
                    (application as ComicMemoApplication).repository.insert(comic)
                } else {
                    (application as ComicMemoApplication).repository.update(comic)
                }
            }
            Toast.makeText(applicationContext, getString(R.string.register_book) + comic.title , Toast.LENGTH_LONG).show()
        }
    }

    public override fun onDestroy() {
        mAdView?.destroy()
        super.onDestroy()
    }
}