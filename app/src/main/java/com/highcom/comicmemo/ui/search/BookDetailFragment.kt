package com.highcom.comicmemo.ui.search

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.highcom.comicmemo.ui.edit.InputMemoActivity
import com.highcom.comicmemo.R
import com.highcom.comicmemo.databinding.FragmentBookDetailBinding
import com.highcom.comicmemo.datamodel.Comic
import com.highcom.comicmemo.network.Item
import java.io.Serializable

/**
 * 選択した書籍の詳細情報を表示する
 *
 */
class BookDetailFragment : Fragment() {
    private lateinit var binding: FragmentBookDetailBinding
    private lateinit var item: Item

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBookDetailBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 選択されたアイテムデータを取得する
        requireArguments().let { arguments ->
            item = arguments.getSerializable("BUNDLE_ITEM_DATA") as Item
        }

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
            val chooser = Intent.createChooser(intent, "選択")
            startActivity(chooser)
        }
        // 選択した書籍の情報から巻数メモデータを新規作成する
        binding.detailNewButton.setOnClickListener {
            // 入力画面を生成
            val intent = Intent(context, InputMemoActivity::class.java)
            // 選択した書籍からデータを作成
            val comic = Comic(0, item.Item.title, item.Item.author, "", "", "", 0)
            intent.putExtra("EDIT", false)
            intent.putExtra("COMIC", comic as Serializable)
            startActivityForResult(intent, 1001)
        }
    }
}