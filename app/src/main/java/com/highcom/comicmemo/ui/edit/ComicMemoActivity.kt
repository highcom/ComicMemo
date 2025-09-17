package com.highcom.comicmemo.ui.edit

import android.os.Bundle
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.firebase.analytics.FirebaseAnalytics
import com.highcom.comicmemo.R
import com.highcom.comicmemo.databinding.ActivityComicMemoBinding
import dagger.hilt.android.AndroidEntryPoint
import jp.co.recruit_mp.android.rmp_appirater.RmpAppirater
import java.util.Date

@AndroidEntryPoint
class ComicMemoActivity : AppCompatActivity() {
    /** バインディング */
    private lateinit var binding: ActivityComicMemoBinding
    /** Firebase解析 */
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    /** AdMob広告 */
    private var mAdView: AdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        binding = ActivityComicMemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // ActionBarの影をなくす
        supportActionBar?.elevation = 0f
        // Firebaseの初期化
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        MobileAds.initialize(this)
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(listOf("874848BA4D9A6B9B0A256F7862A47A31", "A02A04D245766C519D07D09F0E258E1E")).build()
        )
        // 広告のロード
        binding.adViewFrame.post { loadBanner() }
        // レビュー評価依頼のダイアログに表示する内容を設定
        val options = RmpAppirater.Options(
            getString(R.string.review_dialog_title),
            getString(R.string.review_dialog_message),
            getString(R.string.review_dialog_rate),
            getString(R.string.review_dialog_rate_later),
            getString(R.string.review_dialog_rate_cancel)
        )
        // 起動時にアプリの評価をお願いする
        RmpAppirater.appLaunched(this,
            RmpAppirater.ShowRateDialogCondition { appLaunchCount, appThisVersionCodeLaunchCount, _, appVersionCode, _, rateClickDate, reminderClickDate, doNotShowAgain -> // 現在のアプリのバージョンで3回以上起動したか
                // レビュー依頼の文言を変えたバージョンでは、まだレビューをしておらず
                // 長く利用していてバージョンアップしたユーザーに新バージョンで3回目に一度だけ必ず表示する
                if (appVersionCode == 17 && rateClickDate == null && appLaunchCount > 30 && appThisVersionCodeLaunchCount == 3L) {
                    return@ShowRateDialogCondition true
                }

                if (appThisVersionCodeLaunchCount < 10) {
                    return@ShowRateDialogCondition false
                }
                // ダイアログで「いいえ」を選択していないか
                if (doNotShowAgain) {
                    return@ShowRateDialogCondition false
                }
                // ユーザーがまだ評価していないか
                if (rateClickDate != null) {
                    return@ShowRateDialogCondition false
                }
                // ユーザーがまだ「あとで」を選択していないか
                if (reminderClickDate != null) {
                    // 「あとで」を選択してから3日以上経過しているか
                    val prevtime = reminderClickDate.time
                    val nowtime = Date().time
                    val diffDays = (nowtime - prevtime) / (1000 * 60 * 60 * 24)
                    if (diffDays < 3) {
                        return@ShowRateDialogCondition false
                    }
                }
                true
            }, options
        )
    }

    /**
     * AdMobバナー広告のロード
     */
    private fun loadBanner() {
        // 広告リクエストの生成
        mAdView = AdView(this)
        mAdView?.adUnitId = getString(R.string.admob_comic_memo_id)
        binding.adViewFrame.removeAllViews()
        binding.adViewFrame.addView(mAdView)
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
            var adWidthPixels = binding.adViewFrame.width.toFloat()

            // 広告がレイアウトされていない場合は、デフォルトで全画面幅にする
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }
            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        }

    /**
     * ActionBarの戻るボタンの有効/無効設定
     *
     * @param enabled 有効にする場合はtrue
     */
    fun setDisplayHomeAsUpEnabled(enabled: Boolean) {
        supportActionBar?.setDisplayHomeAsUpEnabled(enabled)
    }

    public override fun onDestroy() {
        mAdView?.destroy()
        super.onDestroy()
    }
}