package com.highcom.comicmemo

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.google.android.gms.ads.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.highcom.comicmemo.databinding.ActivityComicMemoBinding
import jp.co.recruit_mp.android.rmp_appirater.RmpAppirater
import jp.co.recruit_mp.android.rmp_appirater.RmpAppirater.ShowRateDialogCondition
import java.util.*

class ComicMemo : AppCompatActivity() {
    private lateinit var binding: ActivityComicMemoBinding
    private var listDataManager: ListDataManager? = null
    private var sectionsPagerAdapter: SectionsPagerAdapter? = null
    private var mSearchWord = ""
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    private var mAdView: AdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComicMemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.elevation = 0f
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        MobileAds.initialize(this)
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(Arrays.asList("874848BA4D9A6B9B0A256F7862A47A31")).build()
        )

        binding.adViewFrame.post { loadBanner() }
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
        listDataManager = ListDataManager.Companion.createInstance(applicationContext)
        sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        binding.viewPager.adapter = sectionsPagerAdapter
        binding.itemtabs.setupWithViewPager(binding.viewPager)

        // 追加ボタン処理
        binding.fabNewEdit.setOnClickListener {
            val intent = Intent(this@ComicMemo, InputMemo::class.java)
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

    private fun loadBanner() {
        // Create an ad request.
        mAdView = AdView(this)
        mAdView!!.adUnitId = "ca-app-pub-3217012767112748/8829713111"
        binding.adViewFrame.removeAllViews()
        binding.adViewFrame.addView(mAdView)
        val adSize = adSize
        mAdView!!.adSize = adSize
        val adRequest = AdRequest.Builder().build()

        // Start loading the ad in the background.
        mAdView!!.loadAd(adRequest)
    }

    // Determine the screen width (less decorations) to use for the ad width.
    private val adSize: AdSize
        private get() {
            // Determine the screen width (less decorations) to use for the ad width.
            val display = windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)
            val density = outMetrics.density
            var adWidthPixels = binding.adViewFrame.width.toFloat()

            // If the ad hasn't been laid out, default to the full screen width.
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }
            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_comic_memo, menu)
        val searchMenuView = menu?.findItem(R.id.menu_search_view)
        val searchActionView = searchMenuView?.actionView as SearchView
        searchActionView.queryHint = "検索文字を入力"
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val fragment = sectionsPagerAdapter!!.currentFragment as PlaceholderFragment
        when (item.itemId) {
            R.id.edit_mode -> {
                // 編集状態の変更
                fragment.sortData("id")
                fragment.changeEditEnable()
            }
            R.id.sort_default -> {
                fragment.sortData("id")
                fragment.setEditEnable(false)
            }
            R.id.sort_title -> {
                fragment.sortData("title")
                fragment.setEditEnable(false)
            }
            R.id.sort_author -> {
                fragment.sortData("author")
                fragment.setEditEnable(false)
            }
            else -> {}
        }
        return super.onOptionsItemSelected(item)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // FragmentからのonAdapterClickedからではrequestCodeが引き継がれない
//        if (requestCode != 1001) {
//            return;
//        }
        val fragments = sectionsPagerAdapter!!.allFragment
        for (fragment in fragments) {
            (fragment as PlaceholderFragment).updateData()
            (fragment as PlaceholderFragment).setSearchWordFilter(mSearchWord)
        }
    }

    public override fun onDestroy() {
        mAdView!!.destroy()
        super.onDestroy()
    }
}