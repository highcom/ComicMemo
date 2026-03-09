package com.highcom.comicmemo.ui.search

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.highcom.comicmemo.ComicMemoConstants
import com.highcom.comicmemo.R
import com.highcom.comicmemo.billing.BillingManager
import com.highcom.comicmemo.billing.SubscriptionManager
import com.highcom.comicmemo.databinding.ActivityRakutenBookBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
    /** BillingManager */
    private lateinit var billingManager: BillingManager
    /** 画面の更新が必要かどうか */
    var isNeedUpdate = true

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityRakutenBookBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(left = systemBars.left, right = systemBars.right)
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top)
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.adViewBookListFrame) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = systemBars.bottom)
            insets
        }

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

        // BillingManagerの初期化
        billingManager = BillingManager(this)
        lifecycle.addObserver(billingManager)

        // 購入状態の監視
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                billingManager.purchaseState.collect { _ ->
                    updateAdVisibility()
                }
            }
        }

        // 広告のロード
        updateAdVisibility()
    }

    /**
     * 広告の表示/非表示を更新
     */
    private fun updateAdVisibility() {
        if (SubscriptionManager.isPremium(this) || SubscriptionManager.isHideAds(this)) {
            // 有料会員の場合、広告を非表示にして破棄
            binding.adViewBookListFrame.visibility = View.GONE
            mAdView?.destroy()
            mAdView = null
        } else {
            // 無料会員の場合、広告を表示
            binding.adViewBookListFrame.visibility = View.VISIBLE
            if (mAdView == null) {
                loadBanner()
            }
        }
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
        lifecycle.removeObserver(billingManager)
        super.onDestroy()
    }
}