package com.highcom.comicmemo.ui.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ToggleButton
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.highcom.comicmemo.R
import com.highcom.comicmemo.databinding.FragmentReferenceMemoBinding
import com.highcom.comicmemo.datamodel.Comic

/**
 * 巻数メモ参照画面
 */
class ReferenceMemoFragment : Fragment() {
    /** バインディング */
    private lateinit var binding: FragmentReferenceMemoBinding
    /** Navigationで渡された引数 */
    private val args: ReferenceMemoFragmentArgs by navArgs()
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReferenceMemoBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isEdit = args.isEdit
        status = args.status
        comic = args.comic
        binding.editTitle.setText(comic.title)
        binding.editAuthor.setText(comic.author)
        binding.editNumber.setText(comic.number)
        binding.editMemo.setText(comic.memo)
        tbContinue = binding.toggleContinue
        tbComplete = binding.toggleComplete
        if (status == 0L) {
            // 続刊
            setEnableToggleContinue()
        } else {
            // 完結
            setEnableToggleComplete()
        }
        // タイトルの設定
        requireActivity().title = getString(R.string.reference_title)

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
                menuInflater.inflate(R.menu.menu_edit, menu)
            }

            /**
             * アクションバーのメニュー選択処理
             *
             * @param menuItem メニューアイテム
             * @return 選択処理を行った場合はtrue
             */
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    android.R.id.home -> findNavController().popBackStack()
                    R.id.action_edit -> findNavController().navigate(ReferenceMemoFragmentDirections.actionReferenceMemoFragmentToInputMemoFragment(true, status, comic))
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
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
        tbComplete?.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
        tbComplete?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.toggle_disable_button
            )
        )
    }

    /**
     * 完結を選択時の処理
     *
     */
    @Suppress("DEPRECATION")
    private fun setEnableToggleComplete() {
        // 表示を完結を選択された状態に設定
        tbContinue?.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
        tbContinue?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.toggle_disable_button
            )
        )
        tbComplete?.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        tbComplete?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.toggle_select_button
            )
        )
    }
}