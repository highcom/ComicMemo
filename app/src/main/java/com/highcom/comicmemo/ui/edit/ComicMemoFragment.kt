package com.highcom.comicmemo.ui.edit

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.highcom.comicmemo.ComicMemoConstants
import com.highcom.comicmemo.R
import com.highcom.comicmemo.databinding.FragmentComicMemoBinding
import com.highcom.comicmemo.datamodel.Comic
import com.highcom.comicmemo.ui.search.RakutenBookActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * 巻数メモ一覧Fragment（ViewPager2対応版）
 */
@AndroidEntryPoint
class ComicMemoFragment : Fragment(), PlaceholderFragment.UpdateComicListListener {
    /** バインディング */
    private lateinit var binding: FragmentComicMemoBinding
    /** タブレイアウトのセクションページアダプタ */
    private lateinit var sectionsPagerAdapter: SectionsPagerAdapter
    /** アクションバーのメニュー */
    private var mMenu: Menu? = null
    /** 現在選択中のメニューID */
    private var currentMenuSelect: Int = R.id.sort_default
    /** 絞り込み検索文字列 */
    private var mSearchWord = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentComicMemoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Adapter を ViewPager2 にセット
        sectionsPagerAdapter = SectionsPagerAdapter(requireContext(), this)
        binding.viewPager.adapter = sectionsPagerAdapter
        binding.viewPager.offscreenPageLimit = sectionsPagerAdapter.itemCount
        // TabLayout と連携
        TabLayoutMediator(binding.itemtabs, binding.viewPager) { tab, position ->
            tab.text = sectionsPagerAdapter.getPageTitle(position)
        }.attach()
        // ページ切り替え時に現在表示中の Fragment を更新
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                notifyChangeCurrentFragment()
            }
        })

        // タイトル設定
        requireActivity().title = getString(R.string.app_name)
        // 戻るボタン非表示
        (requireActivity() as? ComicMemoActivity)?.setDisplayHomeAsUpEnabled(false)
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
                mSearchWord = ""
                forEachPlaceholder { it.setSearchWordFilter(mSearchWord) }

                // SearchView 設定
                val searchView = menu.findItem(R.id.menu_search_view)?.actionView as SearchView
                searchView.queryHint = getString(R.string.query_hint)

                // 検索文字列変更時
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?) = false
                    override fun onQueryTextChange(newText: String?): Boolean {
                        mSearchWord = newText.orEmpty()
                        forEachPlaceholder { it.setSearchWordFilter(mSearchWord) }
                        return false
                    }
                })

                // SearchView 閉じた時の処理
                searchView.setOnCloseListener {
                    forEachPlaceholder { it.setSmoothScrollPosition(0) }
                    false
                }
            }

            /**
             * アクションバーのメニュー選択処理
             *
             * @param menuItem 選択メニューアイテム
             * @return 選択処理を行った場合は true
             */
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                val fragment = currentPlaceholder() ?: return false

                when (menuItem.itemId) {
                    R.id.edit_mode -> {
                        // 編集状態切替
                        fragment.sortData(ComicListPersistent.SortType.ID)
                        if (fragment.getEditEnable()) {
                            setCurrentSelectMenuTitle(mMenu?.findItem(R.id.sort_default), R.id.sort_default)
                        } else {
                            setCurrentSelectMenuTitle(menuItem, R.id.edit_mode)
                        }
                        fragment.changeEditEnable()
                    }
                    R.id.sort_default -> {
                        // ID でソート
                        setCurrentSelectMenuTitle(menuItem, R.id.sort_default)
                        fragment.sortData(ComicListPersistent.SortType.ID)
                        fragment.setEditEnable(false)
                    }
                    R.id.sort_title -> {
                        // タイトル名でソート
                        setCurrentSelectMenuTitle(menuItem, R.id.sort_title)
                        fragment.sortData(ComicListPersistent.SortType.TITLE)
                        fragment.setEditEnable(false)
                    }
                    R.id.sort_author -> {
                        // 著者名でソート
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
                    else -> return false
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        // 追加ボタン処理
        binding.fabNewEdit.setOnClickListener {
            val index = currentPlaceholder()?.index?.toLong() ?: 0L
            findNavController().navigate(
                ComicMemoFragmentDirections.actionComicMemoFragmentToInputMemoFragment(
                    isEdit = false, status = index, comic = Comic(0, "", "", "", "", "", "", "", "", index)))
        }
    }

    /**
     * 現在表示されている PlaceholderFragment を取得
     *
     * @return 現在のフラグメント
     */
    private fun currentPlaceholder(): PlaceholderFragment? {
        val tag = "f${binding.viewPager.currentItem}"
        return childFragmentManager.findFragmentByTag(tag) as? PlaceholderFragment
    }

    /**
     * すべての PlaceholderFragment に対して処理を実行
     *
     * @param action 実行処理
     */
    private fun forEachPlaceholder(action: (PlaceholderFragment) -> Unit) {
        for (i in 0 until sectionsPagerAdapter.itemCount) {
            val tag = "f$i"
            (childFragmentManager.findFragmentByTag(tag) as? PlaceholderFragment)?.let(action)
        }
    }

    /**
     * メニューの初期状態設定
     */
    private fun setInitialMenuTitle() {
        // 全メニューの選択アイコンをリセット
        listOf(R.id.edit_mode, R.id.sort_default, R.id.sort_title, R.id.sort_author).forEach { id ->
            mMenu?.findItem(id)?.let { item ->
                item.title = item.title.toString()
                    .replace(getString(R.string.select_menu_icon), getString(R.string.no_select_menu_icon))
            }
        }

        val fragment = currentPlaceholder()
        currentMenuSelect = when (fragment?.getSortType()) {
            ComicListPersistent.SortType.ID -> R.id.sort_default
            ComicListPersistent.SortType.TITLE -> R.id.sort_title
            ComicListPersistent.SortType.AUTHOR -> R.id.sort_author
            else -> R.id.sort_default
        }
        if (fragment?.getEditEnable() == true) currentMenuSelect = R.id.edit_mode

        // 選択中メニューにアイコンを設定
        mMenu?.findItem(currentMenuSelect)?.let { item ->
            item.title = item.title.toString()
                .replace(getString(R.string.no_select_menu_icon), getString(R.string.select_menu_icon))
        }
    }

    /**
     * 選択中メニュータイトル更新
     *
     * @param item メニューアイテム
     * @param id メニューID
     */
    private fun setCurrentSelectMenuTitle(item: MenuItem?, id: Int) {
        // 前回の選択アイコンをリセット
        mMenu?.findItem(currentMenuSelect)?.let { current ->
            current.title = current.title.toString()
                .replace(getString(R.string.select_menu_icon), getString(R.string.no_select_menu_icon))
        }
        // 新しい選択アイコンを設定
        currentMenuSelect = id
        item?.let {
            it.title = it.title.toString()
                .replace(getString(R.string.no_select_menu_icon), getString(R.string.select_menu_icon))
        }
    }

    /**
     * 続刊の巻数一覧数更新処理
     *
     * @param count 巻数一覧数
     */
    override fun onUpdateContinueComicsCount(count: Int) {
        binding.itemtabs.getTabAt(0)?.text =
            getString(R.string.tab_text_1) + "（$count）"
    }

    /**
     * 完結の巻数一覧数更新処理
     *
     * @param count 巻数一覧数
     */
    override fun onUpdateCompleteComicsCount(count: Int) {
        binding.itemtabs.getTabAt(1)?.text =
            getString(R.string.tab_text_2) + "（$count）"
    }

    /**
     * 現在表示中のタブ変更時に呼ぶ
     */
    private fun notifyChangeCurrentFragment() {
        setInitialMenuTitle()
    }
}