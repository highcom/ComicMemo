package com.highcom.comicmemo.ui.search

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.highcom.comicmemo.ComicMemoConstants
import com.highcom.comicmemo.R
import com.highcom.comicmemo.databinding.FragmentBookDetailBinding
import com.highcom.comicmemo.datamodel.Comic
import com.highcom.comicmemo.network.Item
import com.highcom.comicmemo.viewmodel.RakutenBookViewModel

/**
 * 選択した書籍の詳細情報を表示する
 *
 */
class BookDetailFragment : Fragment() {
    private lateinit var binding: FragmentBookDetailBinding
    /** Activityで生成されたViewModelを利用する */
    private val viewModel: RakutenBookViewModel by activityViewModels()
    /** 楽天ブックデータ */
    private lateinit var item: Item

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookDetailBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // タイトルを設定
        requireActivity().title = viewModel.title
        // 追加登録された時だけSnackbarを表示する
        val navBackStackEntry = findNavController().currentBackStackEntry
        navBackStackEntry?.savedStateHandle?.getLiveData<String>("comic_title")?.observe(viewLifecycleOwner) { title ->
            Snackbar.make(view, getString(R.string.register_book) + title, Snackbar.LENGTH_LONG).setAction("Action", null).show()
        }

        // 選択されたアイテムデータを取得する
        requireArguments().let { arguments ->
            item = arguments.getSerializable(ComicMemoConstants.BUNDLE_ITEM_DATA) as Item
        }

        requireActivity().addMenuProvider(object : MenuProvider {
            /**
             * アクションバーのメニュー生成
             *
             * @param menu メニュー
             * @param menuInflater インフレーター
             */
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Fragmentのメニューを有効にする
                setHasOptionsMenu(true)
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
                        findNavController().popBackStack()
                        return true
                    }
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        // 画像URLから画像を表示する
        val imgUri = item.Item.largeImageUrl.toUri().buildUpon().scheme("https").build()
        Glide.with(binding.detailImageView.context)
            .load(imgUri)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.ic_baseline_broken_image_24))
            .into(binding.detailImageView)
        // それぞれのテキストエリアに情報を設定
        if (item.Item.title.isNotEmpty()) binding.detailTitleView.text = item.Item.title
        if (item.Item.subTitle.isNotEmpty()) binding.detailSubtitleView.text = item.Item.subTitle
        if (item.Item.author.isNotEmpty()) binding.detailAuthorView.text = item.Item.author
        if (item.Item.publisherName.isNotEmpty()) binding.detailPublisherView.text = item.Item.publisherName
        if (item.Item.salesDate.isNotEmpty()) binding.detailSalesView.text = item.Item.salesDate
        binding.detailCaptionView.text = item.Item.itemCaption
        // 楽天の商品URLに飛ぶ
        binding.detailUrl.setOnClickListener {
            val uri = Uri.parse(item.Item.itemUrl)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            val chooser = Intent.createChooser(intent, getString(R.string.chooser_title))
            startActivity(chooser)
        }
        // 選択した書籍の情報から巻数メモデータを新規作成する
        binding.detailNewButton.setOnClickListener {
            // 入力画面を生成
            val comic = Comic(0, item.Item.title, item.Item.author, "", "", "", 0)
            val status = comic.status
            findNavController().navigate(
                BookDetailFragmentDirections.actionBookDetailFragmentToInputMemoFragment(isEdit = false, status = status, comic))
        }
    }
}