package com.highcom.comicmemo.ui.edit

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.highcom.comicmemo.ComicMemoConstants
import com.highcom.comicmemo.R
import com.highcom.comicmemo.databinding.FragmentComicMemoBinding
import com.highcom.comicmemo.datamodel.Comic
import com.highcom.comicmemo.ui.search.RakutenBookActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * 巻数メモ一覧Activity
 */
@AndroidEntryPoint
class ComicMemoFragment : Fragment(), SectionsPagerAdapter.SectionPagerAdapterListener, PlaceholderFragment.UpdateComicListListener {
    /** バインディング */
    private lateinit var binding: FragmentComicMemoBinding
    /** タブレイアウトのセクションページアダプタ */
    private var sectionsPagerAdapter: SectionsPagerAdapter? = null
    /** 絞り込み検索文字列 */
    private var mSearchWord = ""
    /** メニュー */
    private var mMenu: Menu? = null
    /** 現在選択中のメニュー */
    private var currentMenuSelect: Int = R.id.sort_default

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentComicMemoBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 各セクションページに表示する一覧データの設定
        sectionsPagerAdapter = SectionsPagerAdapter(requireContext(), this, childFragmentManager)
        binding.viewPager.adapter = sectionsPagerAdapter
        binding.itemtabs.setupWithViewPager(binding.viewPager)
        // タイトルの設定
        requireActivity().title = getString(R.string.app_name)
        // メニューの戻るボタンを非表示
        val activity = requireActivity()
        if (activity is ComicMemoActivity) activity.setDisplayHomeAsUpEnabled(false)
        // Fragment のライフサイクルに紐付けて MenuProvider を登録
        requireActivity().addMenuProvider(object : MenuProvider {
            /**
             * アクションバーのメニュー生成
             *
             * @param menu メニュー
             * @param menuInflater インフレーター
             */
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_comic_memo, menu)
                mMenu = menu
                setInitialMenuTitle()
                // 文字列検索の処理を設定
                val searchMenuView = menu.findItem(R.id.menu_search_view)
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
                searchActionView.setOnCloseListener(SearchView.OnCloseListener {
                    // 検索終了時もスクロールを先頭の初期位置に戻す
                    val fragments = sectionsPagerAdapter!!.allFragment
                    for (fragment in fragments) {
                        (fragment as PlaceholderFragment).setSmoothScrollPosition(0)
                    }

                    return@OnCloseListener false
                })
            }

            /**
             * アクションバーのメニュー選択処理
             *
             * @param menuItem メニューアイテム
             * @return 選択処理を行った場合はtrue
             */
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                val fragment = sectionsPagerAdapter!!.currentFragment as PlaceholderFragment
                when (menuItem.itemId) {
                    R.id.edit_mode -> {
                        // 編集状態
                        fragment.sortData(ComicListPersistent.SortType.ID)
                        if (fragment.getEditEnable()) {
                            setCurrentSelectMenuTitle(mMenu?.findItem(R.id.sort_default), R.id.sort_default)
                        } else {
                            setCurrentSelectMenuTitle(menuItem, R.id.edit_mode)
                        }
                        fragment.changeEditEnable()
                    }
                    R.id.sort_default -> {
                        // idでのソート
                        setCurrentSelectMenuTitle(menuItem, R.id.sort_default)
                        fragment.sortData(ComicListPersistent.SortType.ID)
                        fragment.setEditEnable(false)
                    }
                    R.id.sort_title -> {
                        // タイトル名でのソート
                        setCurrentSelectMenuTitle(menuItem, R.id.sort_title)
                        fragment.sortData(ComicListPersistent.SortType.TITLE)
                        fragment.setEditEnable(false)
                    }
                    R.id.sort_author -> {
                        // 著者名でのソート
                        setCurrentSelectMenuTitle(menuItem, R.id.sort_author)
                        fragment.sortData(ComicListPersistent.SortType.AUTHOR)
                        fragment.setEditEnable(false)
                    }
                    R.id.search_book -> {
                        // 人気書籍検索
                        val intent = Intent(requireActivity(), RakutenBookActivity::class.java)
                        intent.putExtra(ComicMemoConstants.KEY_BOOK_MODE, ComicMemoConstants.BOOK_MODE_SEARCH)
                        startActivity(intent)
                    }
                    R.id.new_book -> {
                        // 新刊検索
                        val intent = Intent(requireActivity(), RakutenBookActivity::class.java)
                        intent.putExtra(ComicMemoConstants.KEY_BOOK_MODE, ComicMemoConstants.BOOK_MODE_NEW)
                        startActivity(intent)
                    }
                    else -> { return false }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        // 追加ボタン処理
        binding.fabNewEdit.setOnClickListener {
            var index = 0L
            if (sectionsPagerAdapter!!.currentFragment != null) {
                index = (sectionsPagerAdapter!!.currentFragment as PlaceholderFragment).index.toLong()
            }
            findNavController().navigate(ComicMemoFragmentDirections.actionComicMemoFragmentToInputMemoFragment(
                isEdit = false, status = index, comic = Comic(0, "", "", "", "", "", index)))
        }
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

        val fragment = sectionsPagerAdapter?.currentFragment as PlaceholderFragment?
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
     * 続刊の巻数一覧数更新処理
     *
     * @param count 巻数一覧数
     */
    override fun onUpdateContinueComicsCount(count: Int) {
        // 続刊タブに巻数一覧数を設定する
        val tab1 = binding.itemtabs.getTabAt(0)
        tab1?.text = getString(R.string.tab_text_1) + "（" + count + "）"

    }

    /**
     * 完結の巻数一覧数更新処理
     *
     * @param count 巻数一覧数
     */
    override fun onUpdateCompleteComicsCount(count: Int) {
        // 完結タブに巻数一覧数を設定する
        val tab2 = binding.itemtabs.getTabAt(1)
        tab2?.text = getString(R.string.tab_text_2) + "（" + count + "）"
    }
}