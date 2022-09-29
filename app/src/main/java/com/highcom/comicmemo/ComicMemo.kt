package com.highcom.comicmemo

import androidx.fragment.app.FragmentActivity
import com.highcom.comicmemo.ListDataManager
import com.highcom.comicmemo.SectionsPagerAdapter
import com.google.firebase.analytics.FirebaseAnalytics
import android.os.Bundle
import com.highcom.comicmemo.R
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.gms.ads.initialization.InitializationStatus
import jp.co.recruit_mp.android.rmp_appirater.RmpAppirater
import jp.co.recruit_mp.android.rmp_appirater.RmpAppirater.ShowRateDialogCondition
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.highcom.comicmemo.PlaceholderFragment
import android.content.Intent
import com.highcom.comicmemo.InputMemo
import android.util.DisplayMetrics
import android.app.Activity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import android.database.sqlite.SQLiteDatabase
import android.content.ContentValues
import com.highcom.comicmemo.ListDataOpenHelper
import com.highcom.comicmemo.ListViewAdapter.AdapterListener
import androidx.recyclerview.widget.RecyclerView
import android.util.TypedValue
import android.graphics.drawable.ColorDrawable
import android.annotation.SuppressLint
import android.view.View.OnLongClickListener
import android.widget.Filter.FilterResults
import android.database.sqlite.SQLiteOpenHelper
import com.highcom.comicmemo.SimpleCallbackHelper.SimpleCallbackListener
import com.highcom.comicmemo.PageViewModel
import com.highcom.comicmemo.ListViewAdapter
import com.highcom.comicmemo.SimpleCallbackHelper
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.highcom.comicmemo.SimpleCallbackHelper.UnderlayButton
import com.highcom.comicmemo.SimpleCallbackHelper.UnderlayButtonClickListener
import android.text.TextUtils
import androidx.fragment.app.FragmentPagerAdapter
import androidx.annotation.StringRes
import androidx.recyclerview.widget.ItemTouchHelper
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.View.OnTouchListener
import kotlin.jvm.Synchronized
import android.graphics.RectF
import android.content.res.TypedArray
import android.view.*
import android.widget.*
import com.google.android.gms.ads.*
import java.util.*

class ComicMemo : FragmentActivity() {
    private var listDataManager: ListDataManager? = null
    private var sectionsPagerAdapter: SectionsPagerAdapter? = null
    private var mSearchWord = ""
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    private var adContainerView: FrameLayout? = null
    private var mAdView: AdView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comic_memo)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        MobileAds.initialize(this) { }
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(Arrays.asList("874848BA4D9A6B9B0A256F7862A47A31")).build()
        )
        adContainerView = findViewById(R.id.adViewFrame)
        adContainerView.post(Runnable { loadBanner() })
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
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.itemtabs)
        tabs.setupWithViewPager(viewPager)

        // 編集ボタン処理
        val editbtn = findViewById<View>(R.id.edit) as Button
        editbtn.setOnClickListener { arg0 ->
            val popup = PopupMenu(applicationContext, arg0)
            popup.menuInflater.inflate(R.menu.menu_comic_memo, popup.menu)
            popup.show()

            // ポップアップメニューのメニュー項目のクリック処理
            popup.setOnMenuItemClickListener { item ->
                val fragment = sectionsPagerAdapter.getCurrentFragment() as PlaceholderFragment
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
                true
            }
        }

        // 追加ボタン処理
        val addbtn = findViewById<View>(R.id.add) as Button
        addbtn.setOnClickListener {
            val intent = Intent(this@ComicMemo, InputMemo::class.java)
            if (sectionsPagerAdapter.getCurrentFragment() != null) {
                val index =
                    (sectionsPagerAdapter.getCurrentFragment() as PlaceholderFragment).index.toLong()
                intent.putExtra("ID", listDataManager.getNewId())
                intent.putExtra("STATUS", index)
            }
            intent.putExtra("EDIT", false)
            startActivityForResult(intent, 1001)
        }
        val searchView = findViewById<View>(R.id.searchView) as SearchView
        searchView.queryHint = "検索文字を入力"
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(searchWord: String): Boolean {
                mSearchWord = searchWord
                val fragments = sectionsPagerAdapter.getAllFragment()
                for (fragment in fragments!!) {
                    (fragment as PlaceholderFragment).setSearchWordFilter(mSearchWord)
                }
                return false
            }
        })
    }

    private fun loadBanner() {
        // Create an ad request.
        mAdView = AdView(this)
        mAdView!!.adUnitId = "ca-app-pub-3217012767112748/8829713111"
        adContainerView!!.removeAllViews()
        adContainerView!!.addView(mAdView)
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
            var adWidthPixels = adContainerView!!.width.toFloat()

            // If the ad hasn't been laid out, default to the full screen width.
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }
            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // FragmentからのonAdapterClickedからではrequestCodeが引き継がれない
//        if (requestCode != 1001) {
//            return;
//        }
        val fragments = sectionsPagerAdapter.getAllFragment()
        for (fragment in fragments!!) {
            (fragment as PlaceholderFragment).updateData()
            (fragment as PlaceholderFragment).setSearchWordFilter(mSearchWord)
        }
    }

    public override fun onDestroy() {
        mAdView!!.destroy()
        super.onDestroy()
    }
}