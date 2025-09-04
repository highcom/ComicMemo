package com.highcom.comicmemo.ui.edit

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.highcom.comicmemo.ComicMemoConstants
import com.highcom.comicmemo.R
import com.highcom.comicmemo.databinding.FragmentInputMemoBinding
import com.highcom.comicmemo.datamodel.Comic
import com.highcom.comicmemo.viewmodel.ComicPagerViewModel
import java.util.*

/**
 * 巻数メモ入力Activity
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

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Fragmentのメニューを有効にする
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInputMemoBinding.inflate(inflater)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        binding = ActivityInputMemoBinding.inflate(layoutInflater)
//        setContentView(binding.root)

        // 渡されたデータを取得する
//        val intent = intent
//        isEdit = intent.getBooleanExtra(ComicMemoConstants.ARG_EDIT, false)
//        status = intent.getLongExtra(ComicMemoConstants.ARG_STATUS, 0L)
//        comic = intent.getSerializableExtra(ComicMemoConstants.ARG_COMIC) as? Comic ?: Comic(0, "", "", "", "", "", status)
        isEdit = args.isEdit
        status = args.status
        comic = args.comic
        binding.editTitle.setText(comic.title)
        binding.editAuthor.setText(comic.author)
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

        // タイトルの設定
        requireActivity().title = if (isEdit) {
            getString(R.string.input_edit)
        } else {
            getString(R.string.input_new)
        }

        // アクションバーの戻るボタンを表示
        // TODO:戻るボタンはActivityに委ねる
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    /**
     * アクションバーのメニュー生成処理
     *
     * @param menu アクションバーメニュー
     * @param inflater インフレーター
     * @return
     */
    @Deprecated("Deprecated in Java", ReplaceWith(
        "inflater.inflate(R.menu.menu_done, menu)",
        "com.highcom.comicmemo.R"
    )
    )
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_done, menu)
    }

    /**
     * アクションバーのメニュー選択処理
     *
     * @param item 選択項目
     * @return
     */
    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // TODO:comic_memo_containerの場合も考慮
                requireActivity().findNavController(R.id.rakuten_book_container).run {
                    popBackStack()
                }
            }
            R.id.action_done -> {
                var chgNumber = 0
                if (binding.editNumber.text.toString() != "") {
                    chgNumber = binding.editNumber.text.toString().toInt()
                }
                comic.title = binding.editTitle.text.toString()
                comic.author = binding.editAuthor.text.toString()
                comic.number = chgNumber.toString()
                comic.memo = binding.editMemo.text.toString()
                comic.inputdate = DateFormat.format("yyyy/MM/dd", Date()).toString()
                comic.status = java.lang.Long.valueOf(status)
                // TODO:Navigatonで戻るときにデータを渡す
//                val intent = Intent()
//                intent.putExtra(ComicMemoConstants.ARG_COMIC, comic)
//                setResult(Activity.RESULT_OK, intent)
//                // 詳細画面を終了
//                finish()
                // TODO:isEditも考慮してupdateにする
                pageViewModel.insert(comic)
                // TODO:rakuten_book_containerの場合も考慮
                requireActivity().findNavController(R.id.comic_memo_container).run {
                    popBackStack()
                }
            }
        }
        return super.onOptionsItemSelected(item)
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
     * 完結を選択時の処理
     *
     */
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