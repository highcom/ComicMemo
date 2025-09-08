package com.highcom.comicmemo.ui.search

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.highcom.comicmemo.ComicMemoConstants
import com.highcom.comicmemo.R
import com.highcom.comicmemo.databinding.ActivityRakutenBookBinding
import com.highcom.comicmemo.datamodel.Comic
import com.highcom.comicmemo.viewmodel.RakutenBookViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * 楽天書籍APIを利用した書籍画面のActivity
 */
@AndroidEntryPoint
class RakutenBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRakutenBookBinding
    /** 人気書籍検索画面のViewModel */
    private val viewModel: RakutenBookViewModel by viewModels()
    /** ComicMemoアプリ用SharedPreference */
    private lateinit var sharedPreferences: SharedPreferences
    /** 書籍検索モード */
    private var bookMode: Int = 0
    /** 現在選択中のメニュー */
    private var currentGenreSelect = RakutenBookViewModel.GENRE_ID_COMIC
    /** メニュー */
    private var mMenu: Menu? = null
    /** 現在選択中のメニュー */
    private var currentMenuSelect: Int = R.id.search_mode_comic
    /** AdMob広告 */
    private var mAdView: AdView? = null
    /** 画面の更新が必要かどうか */
    var isNeedUpdate = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRakutenBookBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bookMode = intent.getIntExtra(ComicMemoConstants.KEY_BOOK_MODE, ComicMemoConstants.BOOK_MODE_SEARCH)
        if (bookMode == ComicMemoConstants.BOOK_MODE_NEW) {
            title = getString(R.string.new_book)
        } else {
            sharedPreferences = getPreferences(Context.MODE_PRIVATE)
            currentGenreSelect = sharedPreferences.getString(
                ComicMemoConstants.SELECT_GENRE,
                RakutenBookViewModel.GENRE_ID_COMIC
            ) ?: RakutenBookViewModel.GENRE_ID_COMIC
            setMenu(currentGenreSelect)
            setTitle(currentGenreSelect)
        }
        // Fragmentナビゲーションの設定
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.rakuten_book_container) as NavHostFragment
        val navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.rakuten_book_navigation)
        val bundle = Bundle()
        bundle.putInt(ComicMemoConstants.KEY_BOOK_MODE, bookMode)
        navController.setGraph(navGraph, bundle)
        // アクションバーの戻るボタンを表示
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // ViewModelの初期設定
        viewModel.initialize(getString(R.string.rakuten_app_id), currentGenreSelect)
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
     * メニュー選択設定
     *
     * @param genre 選択ジャンル
     */
    private fun setMenu(genre: String) {
        when (genre) {
            RakutenBookViewModel.GENRE_ID_COMIC -> currentMenuSelect = R.id.search_mode_comic
            RakutenBookViewModel.GENRE_ID_NOVEL -> currentMenuSelect = R.id.search_mode_novel
            RakutenBookViewModel.GENRE_ID_LIGHT_NOVEL -> currentMenuSelect = R.id.search_mode_light_novel
            RakutenBookViewModel.GENRE_ID_PAPERBACK -> currentMenuSelect = R.id.search_mode_paperback
            RakutenBookViewModel.GENRE_ID_NEW_BOOK -> currentMenuSelect = R.id.search_mode_new_book
        }
    }

    /**
     * タイトル名称設定
     *
     * @param genre 選択ジャンル
     */
    private fun setTitle(genre: String) {
        when (genre) {
            RakutenBookViewModel.GENRE_ID_COMIC -> title = getString(R.string.trend_book) + getString(R.string.header_comic)
            RakutenBookViewModel.GENRE_ID_NOVEL -> title = getString(R.string.trend_book) + getString(R.string.header_novel)
            RakutenBookViewModel.GENRE_ID_LIGHT_NOVEL -> title = getString(R.string.trend_book) + getString(R.string.header_light_novel)
            RakutenBookViewModel.GENRE_ID_PAPERBACK -> title = getString(R.string.trend_book) + getString(R.string.header_paperback)
            RakutenBookViewModel.GENRE_ID_NEW_BOOK -> title = getString(R.string.trend_book) + getString(R.string.header_new_book)
        }
    }

    /**
     * アクションバーのメニュー生成処理
     *
     * @param menu メニュー
     * @return
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // 新刊検索の場合には著作者名編集メニューを表示する
        if (bookMode == ComicMemoConstants.BOOK_MODE_NEW) {
            menuInflater.inflate(R.menu.menu_edit, menu)
            title = getString(R.string.new_book)
            mMenu = menu
            return super.onCreateOptionsMenu(menu)
        }

        menuInflater.inflate(R.menu.menu_rakuten_book, menu)
        // 初期検索種別を設定
        mMenu = menu
        mMenu?.findItem(currentMenuSelect)?.title = mMenu?.findItem(currentMenuSelect)?.title.toString()
            .replace(getString(R.string.no_select_menu_icon), getString(R.string.select_menu_icon))
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
                return super.onOptionsItemSelected(item)
            }
            R.id.action_edit -> {
                // 著作者名編集で編集しない場合には更新不要なので一旦不要とする
                isNeedUpdate = false
                // 著作者名編集に行く時は検索途中でもキャンセルする
                viewModel.timer.cancel()
                findNavController(R.id.rakuten_book_container).navigate(R.id.action_bookListFragment_to_authorEditFragment, null)
                title = getString(R.string.edit_author)
                item.setVisible(false)
                return super.onOptionsItemSelected(item)
            }
            R.id.search_mode_comic -> {
                currentGenreSelect = RakutenBookViewModel.GENRE_ID_COMIC
            }
            R.id.search_mode_novel -> {
                currentGenreSelect = RakutenBookViewModel.GENRE_ID_NOVEL
            }
            R.id.search_mode_light_novel -> {
                currentGenreSelect = RakutenBookViewModel.GENRE_ID_LIGHT_NOVEL
            }
            R.id.search_mode_paperback -> {
                currentGenreSelect = RakutenBookViewModel.GENRE_ID_PAPERBACK
            }
            R.id.search_mode_new_book -> {
                currentGenreSelect = RakutenBookViewModel.GENRE_ID_NEW_BOOK
            }
        }
        // 前回選択したメニューの状態を戻して今回選択されたメニューを選択状態にする
        setCurrentSelectMenuTitle(item, currentMenuSelect)
        // 選択された項目でメニューとジャンルを設定
        currentMenuSelect =item.itemId
        viewModel.getSalesList(currentGenreSelect)
        setTitle(currentGenreSelect)
        // 次回起動時のために選択したジャンルを保存する
        sharedPreferences.edit().putString(ComicMemoConstants.SELECT_GENRE, currentGenreSelect).apply()

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
        val selectMenuTitle = item?.title.toString().replace(getString(R.string.no_select_menu_icon), getString(
            R.string.select_menu_icon
        ))
        item?.title = selectMenuTitle
    }

    /**
     * 著作者名一覧編集画面遷移ボタンの表示処理
     *
     * @param visible 表示状態
     */
    fun setActionEditVisiblity(visible: Boolean) {
        mMenu?.findItem(R.id.action_edit)?.setVisible(visible)
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
                viewModel.insert(comic)
            } else {
                viewModel.update(comic)
            }
            Toast.makeText(applicationContext, getString(R.string.register_book) + comic.title , Toast.LENGTH_LONG).show()
        }
    }

    public override fun onDestroy() {
        mAdView?.destroy()
        super.onDestroy()
    }
}