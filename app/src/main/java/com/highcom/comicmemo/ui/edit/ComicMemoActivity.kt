package com.highcom.comicmemo.ui.edit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.google.android.gms.ads.*
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.analytics.FirebaseAnalytics
import com.highcom.comicmemo.R
import com.highcom.comicmemo.ComicMemoConstants
import com.highcom.comicmemo.databinding.ActivityComicMemoBinding
import com.highcom.comicmemo.datamodel.Comic
import com.highcom.comicmemo.ui.search.RakutenBookActivity
import dagger.hilt.android.AndroidEntryPoint
import jp.co.recruit_mp.android.rmp_appirater.RmpAppirater
import jp.co.recruit_mp.android.rmp_appirater.RmpAppirater.ShowRateDialogCondition
import java.util.*

/**
 * 巻数メモ一覧Activity
 */
@AndroidEntryPoint
class ComicMemoActivity : AppCompatActivity(), SectionsPagerAdapter.SectionPagerAdapterListener {
    /** バインディング */
    private lateinit var binding: ActivityComicMemoBinding
    /** タブレイアウトのセクションページアダプタ */
    private lateinit var sectionsPagerAdapter: SectionsPagerAdapter
    /** 絞り込み検索文字列 */
    private var mSearchWord = ""
    /** メニュー */
    private var mMenu: Menu? = null
    /** 現在選択中のメニュー */
    private var currentMenuSelect: Int = R.id.sort_default
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
            ShowRateDialogCondition { appLaunchCount, appThisVersionCodeLaunchCount, _, appVersionCode, _, rateClickDate, reminderClickDate, doNotShowAgain -> // 現在のアプリのバージョンで3回以上起動したか
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

        // 各セクションページに表示する一覧データの設定
        sectionsPagerAdapter = SectionsPagerAdapter(this)
        binding.viewPager.adapter = sectionsPagerAdapter
        TabLayoutMediator(binding.itemtabs, binding.viewPager) { tab, position ->
            if (position == 0) tab.text = getString(R.string.tab_text_1)
            else if (position == 1) tab.text = getString(R.string.tab_text_2)
        }.attach()

        // タブ切り替えによるアクティブなフラグメントを設定
        binding.itemtabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val fragment = sectionsPagerAdapter.allFragment.getOrNull(tab?.position ?: 0)
                sectionsPagerAdapter.setCurrentFragment(fragment)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        // 追加ボタン処理
        binding.fabNewEdit.setOnClickListener {
            val intent = Intent(this@ComicMemoActivity, InputMemoActivity::class.java)
            val index = (sectionsPagerAdapter.currentFragment as PlaceholderFragment?)?.index?.toLong() ?: 0
            intent.putExtra(ComicMemoConstants.ARG_STATUS, index)
            intent.putExtra(ComicMemoConstants.ARG_EDIT, false)
            startActivityForResult(intent, 1001)
        }
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
     * アクションバーのメニュー生成処理
     *
     * @param menu アクションバーメニュー
     * @return
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_comic_memo, menu)
        mMenu = menu
        setInitialMenuTitle()
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
                val fragments = sectionsPagerAdapter.allFragment
                for (fragment in fragments) {
                    (fragment as PlaceholderFragment).setSearchWordFilter(mSearchWord)
                }
                return false
            }
        })
        searchActionView.setOnCloseListener(SearchView.OnCloseListener {
            // 検索終了時もスクロールを先頭の初期位置に戻す
            val fragments = sectionsPagerAdapter.allFragment
            for (fragment in fragments) {
                (fragment as PlaceholderFragment).setSmoothScrollPosition(0)
            }

            return@OnCloseListener false
        })

        return super.onCreateOptionsMenu(menu)
    }

    /**
     * 現在表示されているFragmentの変更通知
     *
     */
    override fun notifyChangeCurrentFragment() {
        setInitialMenuTitle()
    }

    /**
     * Fragment毎に設定されているソート種別に合わせたメニュータイトル設定処理
     *
     */
    private fun setInitialMenuTitle() {
        // 選択メニュー状態を一旦元に戻す
        mMenu?.findItem(R.id.edit_mode)?.title = mMenu?.findItem(R.id.edit_mode)?.title.toString()
            .replace(getString(R.string.select_menu_icon), getString(R.string.no_select_menu_icon))
        mMenu?.findItem(R.id.sort_default)?.title = mMenu?.findItem(R.id.sort_default)?.title.toString()
            .replace(getString(R.string.select_menu_icon), getString(R.string.no_select_menu_icon))
        mMenu?.findItem(R.id.sort_title)?.title = mMenu?.findItem(R.id.sort_title)?.title.toString()
            .replace(getString(R.string.select_menu_icon), getString(R.string.no_select_menu_icon))
        mMenu?.findItem(R.id.sort_author)?.title = mMenu?.findItem(R.id.sort_author)?.title.toString()
            .replace(getString(R.string.select_menu_icon), getString(R.string.no_select_menu_icon))

        val fragment = sectionsPagerAdapter.currentFragment as PlaceholderFragment?
        currentMenuSelect = when(fragment?.getSortType()) {
            ComicListPersistent.SortType.ID -> R.id.sort_default
            ComicListPersistent.SortType.TITLE -> R.id.sort_title
            ComicListPersistent.SortType.AUTHOR -> R.id.sort_author
            else -> R.id.sort_default
        }
        if (fragment?.getEditEnable() == true) {
            currentMenuSelect = R.id.edit_mode
        }
            // 現在選択されている選択アイコンを設定する
        val selectMenuTitle = mMenu?.findItem(currentMenuSelect)?.title.toString()
            .replace(getString(R.string.no_select_menu_icon), getString(R.string.select_menu_icon))
        mMenu?.findItem(currentMenuSelect)?.title = selectMenuTitle
    }

    /**
     * アクションバーのメニュー選択処理
     *
     * @param item 選択項目
     * @return
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val fragment = sectionsPagerAdapter.currentFragment as PlaceholderFragment?
        when (item.itemId) {
            R.id.edit_mode -> {
                // 編集状態
                fragment?.sortData(ComicListPersistent.SortType.ID)
                if (fragment?.getEditEnable() == true) {
                    setCurrentSelectMenuTitle(mMenu?.findItem(R.id.sort_default), R.id.sort_default)
                } else {
                    setCurrentSelectMenuTitle(item, R.id.edit_mode)
                }
                fragment?.changeEditEnable()
            }
            R.id.sort_default -> {
                // idでのソート
                setCurrentSelectMenuTitle(item, R.id.sort_default)
                fragment?.sortData(ComicListPersistent.SortType.ID)
                fragment?.setEditEnable(false)
            }
            R.id.sort_title -> {
                // タイトル名でのソート
                setCurrentSelectMenuTitle(item, R.id.sort_title)
                fragment?.sortData(ComicListPersistent.SortType.TITLE)
                fragment?.setEditEnable(false)
            }
            R.id.sort_author -> {
                // 著者名でのソート
                setCurrentSelectMenuTitle(item, R.id.sort_author)
                fragment?.sortData(ComicListPersistent.SortType.AUTHOR)
                fragment?.setEditEnable(false)
            }
            R.id.search_book -> {
                val intent = Intent(this@ComicMemoActivity, RakutenBookActivity::class.java)
                startActivity(intent)
            }
            else -> {}
        }
        notifyChangeCurrentFragment()
        return super.onOptionsItemSelected(item)
    }

    /**
     * 選択メニュータイトル変更処理
     * 選択されているメニューのタイトルの先頭文字を変更する
     *
     * @param item メニューアイテム
     * @param id 選択メニューID
     */
    private fun setCurrentSelectMenuTitle(item: MenuItem?, id: Int) {
        // 現在選択されているメニューの選択アイコンを戻す
        val currentMenuTitle: String = mMenu?.findItem(currentMenuSelect)?.title.toString()
            .replace(getString(R.string.select_menu_icon), getString(R.string.no_select_menu_icon))
        mMenu?.findItem(currentMenuSelect)?.title = currentMenuTitle
        // 今回選択されたメニューに選択アイコンを設定する
        currentMenuSelect = id
        val selectMenuTitle = item?.title.toString().replace(getString(R.string.no_select_menu_icon), getString(
            R.string.select_menu_icon
        ))
        item?.title = selectMenuTitle
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

        val comic = data?.getSerializableExtra(ComicMemoConstants.ARG_COMIC) as? Comic
        if (comic != null) {
            // idが0の場合は新規作成でDBのautoGenerateで自動採番される
            if (comic.id == 0L) {
                (sectionsPagerAdapter.currentFragment as PlaceholderFragment?)?.insert(comic)
            } else {
                (sectionsPagerAdapter.currentFragment as PlaceholderFragment?)?.update(comic)
            }
        }
        // 入力画面で作成されたデータを一覧に反映する
        val fragments = sectionsPagerAdapter.allFragment
        for (fragment in fragments) {
            (fragment as PlaceholderFragment).setSearchWordFilter(mSearchWord)
        }
    }

    public override fun onDestroy() {
        mAdView?.destroy()
        super.onDestroy()
    }
}