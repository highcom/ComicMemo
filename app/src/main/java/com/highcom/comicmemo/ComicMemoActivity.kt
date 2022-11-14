package com.highcom.comicmemo

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.google.android.gms.ads.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.highcom.comicmemo.databinding.ActivityComicMemoBinding
import com.highcom.comicmemo.datamodel.Comic
import jp.co.recruit_mp.android.rmp_appirater.RmpAppirater
import jp.co.recruit_mp.android.rmp_appirater.RmpAppirater.ShowRateDialogCondition
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.thread

/**
 * 巻数メモ一覧Activity
 */
class ComicMemoActivity : AppCompatActivity() {
    /** バインディング */
    private lateinit var binding: ActivityComicMemoBinding
    /** 巻数一覧を制御するためのViewModel */
    private val comicPagerViewModel: ComicPagerViewModel by viewModels {
        ComicPagerViewModelFactory((application as ComicMemoApplication).repository)
    }

    /** 巻数の一覧データ管理 */
    private var listDataManager: ListDataManager? = null
    /** タブレイアウトのセクションページアダプタ */
    private var sectionsPagerAdapter: SectionsPagerAdapter? = null
    /** 絞り込み検索文字列 */
    private var mSearchWord = ""
    /** Firebase解析 */
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    /** AdMob広告 */
    private var mAdView: AdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComicMemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // ActionBarの影をなくす
        supportActionBar?.elevation = 0f
        // Firebaseの初期化
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        MobileAds.initialize(this)
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(listOf("874848BA4D9A6B9B0A256F7862A47A31")).build()
        )
        // 広告のロード
        binding.adViewFrame.post { loadBanner() }
        // 起動時にアプリの評価をお願いする
        RmpAppirater.appLaunched(this,
            ShowRateDialogCondition { appLaunchCount, appThisVersionCodeLaunchCount, firstLaunchDate, appVersionCode, previousAppVersionCode, rateClickDate, reminderClickDate, doNotShowAgain -> // 現在のアプリのバージョンで3回以上起動したか
                if (appThisVersionCodeLaunchCount < 3) {
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
            }
        )

        // 各セクションページに表示する一覧データの設定
        listDataManager = ListDataManager.createInstance(applicationContext)
        sectionsPagerAdapter = SectionsPagerAdapter(this, comicPagerViewModel, supportFragmentManager)
        binding.viewPager.adapter = sectionsPagerAdapter
        binding.itemtabs.setupWithViewPager(binding.viewPager)

        // 追加ボタン処理
        binding.fabNewEdit.setOnClickListener {
            val intent = Intent(this@ComicMemoActivity, InputMemoActivity::class.java)
            if (sectionsPagerAdapter!!.currentFragment != null) {
                val index =
                    (sectionsPagerAdapter!!.currentFragment as PlaceholderFragment).index.toLong()
                intent.putExtra("ID", listDataManager!!.newId)
                intent.putExtra("STATUS", index)
            }
            intent.putExtra("EDIT", false)
            startActivityForResult(intent, 1001)
        }
    }

    /**
     * AdMobバナー広告のロード
     */
    private fun loadBanner() {
        // 広告リクエストの生成
        mAdView = AdView(this)
        mAdView?.adUnitId = "ca-app-pub-3217012767112748/8829713111"
        binding.adViewFrame.removeAllViews()
        binding.adViewFrame.addView(mAdView)
        val adSize = adSize
        mAdView?.adSize = adSize
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
     * アクションバーのメニュー生成処理
     *
     * @param menu アクションばーメニュー
     * @return
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_comic_memo, menu)
        // 文字列検索の処理を設定
        val searchMenuView = menu?.findItem(R.id.menu_search_view)
        val searchActionView = searchMenuView?.actionView as SearchView
        searchActionView.queryHint = getString(R.string.query_hint)
        searchActionView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                mSearchWord = newText ?: ""
                val fragments = sectionsPagerAdapter!!.allFragment
                for (fragment in fragments) {
                    (fragment as PlaceholderFragment).setSearchWordFilter(mSearchWord)
                }
                return false
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    /**
     * アクションバーのメニュー選択処理
     *
     * @param item 選択項目
     * @return
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val fragment = sectionsPagerAdapter!!.currentFragment as PlaceholderFragment
        when (item.itemId) {
            R.id.edit_mode -> {
                // 編集状態
                fragment.sortData(ComicListPersistent.SortType.ID)
                fragment.changeEditEnable()
            }
            R.id.sort_default -> {
                // idでのソート
                fragment.sortData(ComicListPersistent.SortType.ID)
                fragment.setEditEnable(false)
            }
            R.id.sort_title -> {
                // タイトル名でのソート
                fragment.sortData(ComicListPersistent.SortType.TITLE)
                fragment.setEditEnable(false)
            }
            R.id.sort_author -> {
                // 著者名でのソート
                fragment.sortData(ComicListPersistent.SortType.AUTHOR)
                fragment.setEditEnable(false)
            }
            else -> {}
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

        // Fragmentからの呼び出されるので、下位16bitで判定する
        if (requestCode and 0xFFFF != 1001) return

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
        }
        // 入力画面で作成されたデータを一覧に反映する
        val fragments = sectionsPagerAdapter!!.allFragment
        for (fragment in fragments) {
            (fragment as PlaceholderFragment).setSearchWordFilter(mSearchWord)
        }
    }

    public override fun onDestroy() {
        mAdView?.destroy()
        super.onDestroy()
    }
}