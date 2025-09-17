package com.highcom.comicmemo.ui.search

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.highcom.comicmemo.ComicMemoConstants
import com.highcom.comicmemo.R
import com.highcom.comicmemo.databinding.FragmentBookListBinding
import com.highcom.comicmemo.network.Item
import com.highcom.comicmemo.viewmodel.RakutenApiStatus
import com.highcom.comicmemo.viewmodel.RakutenBookViewModel

/**
 * 楽天APIを利用して取得した書籍の一覧を表示するFragment
 *
 */
class BookListFragment : Fragment(), BookItemViewHolder.BookItemListener {
    private lateinit var binding: FragmentBookListBinding
    private lateinit var itemAdapter: BookDataGridItemAdapter
    @Suppress("DEPRECATION")
    private val handler = Handler()
    /** API に問い合わせ中は true になる。 */
    private var nowLoading = true
    /** 検索文字列 */
    private var searchWord: String? = null
    /** ComicMemoアプリ用SharedPreference */
    private lateinit var sharedPreferences: SharedPreferences
    /** メニュー */
    private var mMenu: Menu? = null
    /** 現在選択中のメニュージャンル */
    private var currentGenreSelect = RakutenBookViewModel.GENRE_ID_COMIC
    /** 現在選択中のメニュー */
    private var currentMenuSelect: Int = R.id.search_mode_comic
    /** 画面の更新が必要かどうか */
    var isNeedUpdate = true
    /** Activityで生成されたViewModelを利用する */
    private val viewModel: RakutenBookViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bookMode = requireArguments().getInt(ComicMemoConstants.KEY_BOOK_MODE)

        itemAdapter = BookDataGridItemAdapter(this, bookMode)
        binding.bookItemGridView.adapter = itemAdapter

        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        if (bookMode == ComicMemoConstants.BOOK_MODE_NEW) {
            requireActivity().title = getString(R.string.new_book)
        } else {
            currentGenreSelect = sharedPreferences.getString(
                ComicMemoConstants.SELECT_GENRE,
                RakutenBookViewModel.GENRE_ID_COMIC
            ) ?: RakutenBookViewModel.GENRE_ID_COMIC
            when (currentGenreSelect) {
                RakutenBookViewModel.GENRE_ID_COMIC -> currentMenuSelect = R.id.search_mode_comic
                RakutenBookViewModel.GENRE_ID_NOVEL -> currentMenuSelect = R.id.search_mode_novel
                RakutenBookViewModel.GENRE_ID_LIGHT_NOVEL -> currentMenuSelect = R.id.search_mode_light_novel
                RakutenBookViewModel.GENRE_ID_PAPERBACK -> currentMenuSelect = R.id.search_mode_paperback
                RakutenBookViewModel.GENRE_ID_NEW_BOOK -> currentMenuSelect = R.id.search_mode_new_book
            }
            setTitle(currentGenreSelect)
        }
        viewModel.title = requireActivity().title.toString()
        // ViewModelの初期設定
        viewModel.initialize(getString(R.string.rakuten_app_id), currentGenreSelect)

        requireActivity().addMenuProvider(object : MenuProvider {
            /**
             * アクションバーのメニュー生成
             *
             * @param menu メニュー
             * @param menuInflater インフレーター
             */
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // 新刊検索の場合には著作者名編集メニューを表示する
                if (bookMode == ComicMemoConstants.BOOK_MODE_NEW) {
                    menuInflater.inflate(R.menu.menu_edit, menu)
                    requireActivity().title = getString(R.string.new_book)
                    viewModel.title = requireActivity().title.toString()
                    mMenu = menu
                    return
                }

                menuInflater.inflate(R.menu.menu_rakuten_book, menu)
                // 初期検索種別を設定
                mMenu = menu
                mMenu?.findItem(currentMenuSelect)?.title = mMenu?.findItem(currentMenuSelect)?.title.toString()
                    .replace(getString(R.string.no_select_menu_icon), getString(R.string.select_menu_icon))
                // 文字列検索の処理を設定
                val searchMenuView = menu.findItem(R.id.menu_rakuten_book_search_view)
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
                // 戻ってきた時に入力文字を復元
                viewModel.searchWord.value?.let { savedQuery ->
                    if (savedQuery.isNotEmpty()) {
                        searchActionView.setQuery(savedQuery, false)
                    }
                }
                // バツボタンが押下されて検索を終了する場合
                searchActionView.setOnCloseListener {
                    viewModel.clearSerachWord()
                    viewModel.getSalesList()
                    false
                }
            }

            /**
             * アクションバーのメニュー選択処理
             *
             * @param menuItem メニューアイテム
             * @return 選択処理を行った場合はtrue
             */
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    android.R.id.home -> {
                        requireActivity().finish()
                        return true
                    }
                    R.id.action_edit -> {
                        // 著作者名編集で編集しない場合には更新不要なので一旦不要とする
                        isNeedUpdate = false
                        // 著作者名編集に行く時は検索途中でもキャンセルする
                        viewModel.timer.cancel()
                        findNavController().navigate(BookListFragmentDirections.actionBookListFragmentToAuthorEditFragment(), null)
                        requireActivity().title = getString(R.string.edit_author)
                        menuItem.isVisible = false
                        return true
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
                // 現在選択されているメニューの選択アイコンを戻す
                val currentMenuTitle: String = mMenu?.findItem(currentMenuSelect)?.title.toString()
                    .replace(getString(R.string.select_menu_icon), getString(R.string.no_select_menu_icon))
                mMenu?.findItem(currentMenuSelect)?.title = currentMenuTitle
                // 今回選択されたメニューに選択アイコンを設定する
                val selectMenuTitle = menuItem.title.toString().replace(getString(R.string.no_select_menu_icon), getString(
                    R.string.select_menu_icon
                ))
                menuItem.title = selectMenuTitle
                // 選択された項目でメニューとジャンルを設定
                currentMenuSelect =menuItem.itemId
                viewModel.getSalesList(currentGenreSelect)
                setTitle(currentGenreSelect)
                viewModel.title = requireActivity().title.toString()
                // 次回起動時のために選択したジャンルを保存する
                sharedPreferences.edit().putString(ComicMemoConstants.SELECT_GENRE, currentGenreSelect).apply()

                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        // 検索モードによって1行に表示するアイテムの数を変更する
        if (bookMode == ComicMemoConstants.BOOK_MODE_NEW) {
            (binding.bookItemGridView.layoutManager as GridLayoutManager).spanCount = 1
            // 著作者名が登録されていない場合はプログレスサークルを表示しない
            if (viewModel.getAuthorListSync().isEmpty()) {
                nowLoading = false
                handler.post { binding.progressBar.visibility = View.INVISIBLE }
            }
            // 著作者名一覧で新刊書籍検索
            if (isNeedUpdate) viewModel.searchAuthorList(viewModel.getAuthorListSync())
        } else {
            (binding.bookItemGridView.layoutManager as GridLayoutManager).spanCount = 3
            binding.bookItemGridView.addOnScrollListener(InfiniteScrollListener())
            // 人気書籍の検索
            if (isNeedUpdate) viewModel.getSalesList()
        }
        isNeedUpdate = true

        // 楽天APIの呼び出し状況に応じてプログレスサークルの表示
        viewModel.status.observe(viewLifecycleOwner) { apiStatus ->
            @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
            when (apiStatus) {
                RakutenApiStatus.LOADING -> {
                    nowLoading = true
                    handler.post { binding.progressBar.visibility = View.VISIBLE }
                }
                RakutenApiStatus.DONE, RakutenApiStatus.ERROR -> {
                    nowLoading = false
                    handler.post { binding.progressBar.visibility = View.INVISIBLE }
                }
            }
        }

        // 楽天書籍データを監視
        viewModel.bookList.observe(viewLifecycleOwner) {
            itemAdapter.submitList(it)
        }

        // 検索文字列を監視
        viewModel.searchWord.observe(viewLifecycleOwner) {
            searchWord = it
        }
    }

    /**
     * タイトル名称設定
     *
     * @param genre 選択ジャンル
     */
    private fun setTitle(genre: String) {
        when (genre) {
            RakutenBookViewModel.GENRE_ID_COMIC -> requireActivity().title = getString(R.string.trend_book) + getString(R.string.header_comic)
            RakutenBookViewModel.GENRE_ID_NOVEL -> requireActivity().title = getString(R.string.trend_book) + getString(R.string.header_novel)
            RakutenBookViewModel.GENRE_ID_LIGHT_NOVEL -> requireActivity().title = getString(R.string.trend_book) + getString(R.string.header_light_novel)
            RakutenBookViewModel.GENRE_ID_PAPERBACK -> requireActivity().title = getString(R.string.trend_book) + getString(R.string.header_paperback)
            RakutenBookViewModel.GENRE_ID_NEW_BOOK -> requireActivity().title = getString(R.string.trend_book) + getString(R.string.header_new_book)
        }
    }

    /**
     * リストの下端までスクロールしたタイミングで発火するリスナー
     */
    inner class InfiniteScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            // アダプターが保持しているアイテムの合計
            val itemCount = itemAdapter.itemCount
            // 画面に表示されているアイテム数
            val childCount = recyclerView.childCount
            val manager = recyclerView.layoutManager as LinearLayoutManager
            // 画面に表示されている一番上のアイテムの位置
            val firstPosition = manager.findFirstVisibleItemPosition()

            // 何度もリクエストしないようにロード中は何もしない
            if (nowLoading) {
                return
            }

            // 一番下までスクロールされた場合
            if (itemCount == childCount + firstPosition) {
                if (searchWord != null) {
                    viewModel.search(searchWord!!)
                } else {
                    viewModel.getSalesList()
                }
            }
        }
    }

    /**
     * 書籍詳細画面への遷移処理
     */
    override fun bookItemSelected(item: Item) {
        // 詳細画面から戻ってくる場合には更新は不要
        isNeedUpdate = false
        mMenu?.findItem(R.id.action_edit)?.isVisible = false
        findNavController().navigate(R.id.action_bookListFragment_to_bookDetailFragment, bundleOf("BUNDLE_ITEM_DATA" to item))
    }
}