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
import com.highcom.comicmemo.databinding.ActivityBarcodeSearchBinding
import com.highcom.comicmemo.databinding.ActivityRakutenBookBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * バーコード検索画面のActivity
 */
@AndroidEntryPoint
class BarcodeSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBarcodeSearchBinding
    /** AdMob広告 */
    private var mAdView: AdView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarcodeSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // アクションバーの戻るボタンを表示
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // 広告のロード
        binding.adViewBarcodeSearchFrame.post { loadBanner() }
    }

    /**
     * AdMobバナー広告のロード
     */
    private fun loadBanner() {
        // 広告リクエストの生成
        mAdView = AdView(this)
        // TODO: 広告ユニットを別途作成する
        mAdView?.adUnitId = getString(R.string.admob_book_list_id)
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
        super.onDestroy()
    }
}