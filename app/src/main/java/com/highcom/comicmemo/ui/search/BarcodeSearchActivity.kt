package com.highcom.comicmemo.ui.search

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
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
import com.highcom.comicmemo.databinding.ActivityBarcodeSearchBinding
import com.highcom.comicmemo.databinding.ActivityRakutenBookBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * バーコード検索画面のActivity
 */
@AndroidEntryPoint
class BarcodeSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBarcodeSearchBinding
    /** AdMob広告 */
    private var mAdView: AdView? = null
    /** BillingManager */
    private lateinit var monthlyBillingManager: BillingManager
    private lateinit var yearlyBillingManager: BillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarcodeSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // アクションバーの戻るボタンを表示
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // BillingManagerの初期化
        monthlyBillingManager = BillingManager(this, getString(R.string.premium_subscription))
        yearlyBillingManager = BillingManager(this, getString(R.string.premium_subscription_yearly))
        lifecycle.addObserver(monthlyBillingManager)
        lifecycle.addObserver(yearlyBillingManager)

        // 購入状態の監視
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    monthlyBillingManager.purchaseState.collect { _ ->
                        updateAdVisibility()
                    }
                }
                launch {
                    yearlyBillingManager.purchaseState.collect { _ ->
                        updateAdVisibility()
                    }
                }
            }
        }

        // 広告のロード（有料会員でない場合のみ）
        updateAdVisibility()
    }

    /**
     * 広告の表示/非表示を更新
     */
    private fun updateAdVisibility() {
        if (SubscriptionManager.isPremium(this)) {
            // 有料会員の場合、広告を非表示にして破棄
            binding.adViewBarcodeSearchFrame.visibility = View.GONE
            mAdView?.destroy()
            mAdView = null
        } else {
            // 無料会員の場合、広告を表示
            binding.adViewBarcodeSearchFrame.visibility = View.VISIBLE
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
        mAdView?.adUnitId = getString(R.string.admob_barcode_search_id)
        binding.adViewBarcodeSearchFrame.removeAllViews()
        binding.adViewBarcodeSearchFrame.addView(mAdView)
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
            var adWidthPixels = binding.adViewBarcodeSearchFrame.width.toFloat()

            // 広告がレイアウトされていない場合は、デフォルトで全画面幅にする
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }
            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        }

    public override fun onDestroy() {
        mAdView?.destroy()
        lifecycle.removeObserver(monthlyBillingManager)
        lifecycle.removeObserver(yearlyBillingManager)
        super.onDestroy()
    }
}