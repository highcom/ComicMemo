package com.highcom.comicmemo.ui.edit

import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.highcom.comicmemo.ComicMemoConstants
import com.highcom.comicmemo.R
import com.highcom.comicmemo.billing.SubscriptionManager
import com.highcom.comicmemo.databinding.FragmentInputMemoBinding
import com.highcom.comicmemo.datamodel.Comic
import com.highcom.comicmemo.ui.search.BarcodeSearchActivity
import com.highcom.comicmemo.ui.search.RakutenBookActivity
import com.highcom.comicmemo.viewmodel.ComicPagerViewModel
import kotlinx.coroutines.launch
import java.util.*

/**
 * 巻数メモ入力画面
 */
class InputMemoFragment : Fragment(), CompoundButton.OnCheckedChangeListener {
    /** バインディング */
    private lateinit var binding: FragmentInputMemoBinding
    /** 巻数一覧を制御するためのViewModel */
    private val pageViewModel: ComicPagerViewModel by activityViewModels()
    /** Navigationで渡された引数 */
    private val args: InputMemoFragmentArgs by navArgs()
    /** 編集モードかどうか */
    private var isEdit = false
    /** 巻数データ */
    private lateinit var comic: Comic
    /** アクティブなセクションページ 0:続刊 1:完結 */
    private var status: Long = 0
    /** トグルボタン（続刊） */
    private var tbContinue: ToggleButton? = null
    /** トグルボタン（完結） */
    private var tbComplete: ToggleButton? = null
    /** インタースティシャル広告 */
    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInputMemoBinding.inflate(inflater)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isEdit = args.isEdit
        status = args.status
        comic = args.comic
        binding.editTitle.setText(comic.title)
        binding.editKana.setText(comic.title_kana)
        binding.editAuthor.setText(comic.author)
        binding.editPublisher.setText(comic.publisher)
        binding.editIsbn.setText(comic.isbn)
        binding.editNumber.setText(comic.number)
        binding.editAddbutton.setOnClickListener {
            var strNumber = binding.editNumber.text.toString()
            if (strNumber.isEmpty()) strNumber = "0"
            var chgNumber = strNumber.toInt()
            // 999を上限とする
            if (chgNumber >= ComicMemoConstants.COMIC_NUM_MAX) {
                return@setOnClickListener
            }
            chgNumber++
            binding.editNumber.setText(chgNumber.toString())
        }
        binding.editMinusButton.setOnClickListener {
            var strNumber = binding.editNumber.text.toString()
            if (strNumber.isEmpty()) strNumber = "0"
            var chgNumber = strNumber.toInt()
            // 0を下限とする
            if (chgNumber <= ComicMemoConstants.COMIC_NUM_MIN) {
                return@setOnClickListener
            }
            chgNumber--
            binding.editNumber.setText(chgNumber.toString())
        }

        binding.editMemo.setText(comic.memo)

        tbContinue = binding.toggleContinue
        tbComplete = binding.toggleComplete
        tbContinue?.setOnCheckedChangeListener(this)
        tbComplete?.setOnCheckedChangeListener(this)
        if (status == 0L) {
            // 続刊
            setEnableToggleContinue()
        } else {
            // 完結
            setEnableToggleComplete()
        }

        // 広告のロード
        if (!isEdit && !SubscriptionManager.isHideAds(requireContext())) {
            loadInterstitialAd()
        }

        // タイトルの設定
        requireActivity().title = if (isEdit) {
            getString(R.string.input_edit)
        } else {
            getString(R.string.input_new)
        }

        // メニューの戻るボタンを表示
        val activity = requireActivity()
        if (activity is ComicMemoActivity) activity.setDisplayHomeAsUpEnabled(true)
        // Fragment のライフサイクルに紐付けて MenuProvider を登録
        requireActivity().addMenuProvider(object : MenuProvider {
            /**
             * アクションバーのメニュー生成
             *
             * @param menu メニュー
             * @param menuInflater インフレーター
             */
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_done, menu)
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
                        finishInputMemo()
                    }
                    R.id.action_done -> {
                        viewLifecycleOwner.lifecycleScope.launch {
                            var chgNumber = 0
                            if (binding.editNumber.text.toString() != "") {
                                chgNumber = binding.editNumber.text.toString().toInt()
                            }
                            comic.title = binding.editTitle.text.toString()
                            comic.title_kana = binding.editKana.text.toString()
                            comic.author = binding.editAuthor.text.toString()
                            comic.publisher = binding.editPublisher.text.toString()
                            comic.isbn = binding.editIsbn.text.toString()
                            comic.number = chgNumber.toString()
                            comic.memo = binding.editMemo.text.toString()
                            comic.inputdate = DateFormat.format("yyyy/MM/dd", Date()).toString()
                            comic.status = java.lang.Long.valueOf(status)
                            // 入力データを更新
                            if (isEdit) {
                                pageViewModel.update(comic).join()
                                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                                    "comic_title",
                                    comic.title
                                )
                                // 入力画面を終了する
                                finishInputMemo()
                            } else {
                                pageViewModel.insert(comic).join()
                                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                                    "comic_title",
                                    comic.title
                                )

                                // インタースティシャル広告の判定（新規作成かつ登録数30以上）
                                val totalCount = pageViewModel.getTotalCount()
                                val isHideAds = SubscriptionManager.isHideAds(requireContext())
                                val randomValue = Random().nextDouble()

                                if (!isHideAds && totalCount >= 30 && randomValue < 0.2 && mInterstitialAd != null) {
                                    mInterstitialAd?.fullScreenContentCallback =
                                        object : FullScreenContentCallback() {
                                            override fun onAdDismissedFullScreenContent() {
                                                // 広告を閉じたら画面を終了
                                                finishInputMemo()
                                            }

                                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                                // 広告表示失敗時も画面を終了
                                                finishInputMemo()
                                            }
                                        }
                                    mInterstitialAd?.show(requireActivity())
                                } else {
                                    // 条件に合致しない場合はそのまま終了
                                    finishInputMemo()
                                }
                            }
                        }
                    }
                    else -> { return false }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    /**
     * ポップアップメニュー選択イベント処理
     *
     * @param buttonView ポップアップメニューボタン
     * @param isChecked 選択したかどうか
     */
    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (buttonView.id == R.id.toggleContinue) {
            setEnableToggleContinue()
        } else if (buttonView.id == R.id.toggleComplete) {
            setEnableToggleComplete()
        }
    }

    /**
     * 続刊を選択時の処理
     *
     */
    @Suppress("DEPRECATION")
    private fun setEnableToggleContinue() {
        // 表示を続刊を選択された状態に設定
        tbContinue?.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        tbContinue?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.toggle_select_button
            )
        )
        tbComplete?.setTextColor(ContextCompat.getColor(requireContext(), R.color.appcolor))
        tbComplete?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.toggle_unselect_button
            )
        )
        // 続刊のステータスに設定
        status = 0
    }

    /**
     * インタースティシャル広告のロード
     */
    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(requireContext(), getString(R.string.admob_interstitial_id), adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                }
            })
    }

    /**
     * 巻数メモ入力画面終了処理
     *
     */
    private fun finishInputMemo() {
        val homeActivity = requireActivity()
        // Activityに紐づくNavigationに応じて戻る
        if (homeActivity is ComicMemoActivity) {
            requireActivity().findNavController(R.id.comic_memo_container).run {
                popBackStack()
            }
        } else if (homeActivity is RakutenBookActivity) {
            requireActivity().findNavController(R.id.rakuten_book_container).run {
                popBackStack()
            }
        } else if (homeActivity is BarcodeSearchActivity) {
            requireActivity().findNavController(R.id.barcode_search_container).run {
                popBackStack()
            }
        }
    }

    /**
     * 完結を選択時の処理
     *
     */
    @Suppress("DEPRECATION")
    private fun setEnableToggleComplete() {
        // 表示を完結を選択された状態に設定
        tbContinue?.setTextColor(ContextCompat.getColor(requireContext(), R.color.appcolor))
        tbContinue?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.toggle_unselect_button
            )
        )
        tbComplete?.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        tbComplete?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.toggle_select_button
            )
        )
        // 完結のステータスに設定
        status = 1
    }
}