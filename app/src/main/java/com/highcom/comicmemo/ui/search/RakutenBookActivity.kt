package com.highcom.comicmemo.ui.search

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.highcom.comicmemo.ComicMemoConstants
import com.highcom.comicmemo.R
import com.highcom.comicmemo.databinding.ActivityRakutenBookBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * 楽天書籍APIを利用した書籍画面のActivity
 */
@AndroidEntryPoint
class RakutenBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRakutenBookBinding
    /** 書籍検索モード */
    private var bookMode: Int = 0
    /** AdMob広告 */
    private var mAdView: AdView? = null
    /** 画面の更新が必要かどうか */
    var isNeedUpdate = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRakutenBookBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bookMode = intent.getIntExtra(ComicMemoConstants.KEY_BOOK_MODE, ComicMemoConstants.BOOK_MODE_SEARCH)
        // Fragmentナビゲーションの設定
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.rakuten_book_container) as NavHostFragment
        val navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.rakuten_book_navigation)
        val bundle = Bundle()
        bundle.putInt(ComicMemoConstants.KEY_BOOK_MODE, bookMode)
        navController.setGraph(navGraph, bundle)
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

    public override fun onDestroy() {
        mAdView?.destroy()
        super.onDestroy()
    }
}