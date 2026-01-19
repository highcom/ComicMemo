package com.highcom.comicmemo.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.highcom.comicmemo.R
import com.highcom.comicmemo.billing.BillingManager
import com.highcom.comicmemo.billing.PurchaseState
import com.highcom.comicmemo.billing.SubscriptionManager
import com.highcom.comicmemo.databinding.FragmentSettingBinding
import com.highcom.comicmemo.ui.edit.ComicMemoActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    private lateinit var billingManager: BillingManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // アクションバーのタイトルを設定
        requireActivity().title = getString(R.string.settings)
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
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        // BillingManagerの初期化とライフサイクル管理
        billingManager = BillingManager(
            requireContext(),
            getString(R.string.subscription_id)
        )
        viewLifecycleOwner.lifecycle.addObserver(billingManager)

        // 現在のステータスを表示
        updateMembershipStatus()

        // 購入状態の監視
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                billingManager.purchaseState.collect { state ->
                    handlePurchaseState(state)
                }
            }
        }

        // 購入ボタンのクリックリスナー
        binding.subscribeButton.setOnClickListener {
            if (!SubscriptionManager.isPremium(requireContext())) {
                // プロダクト詳細を取得してから購入フロー開始
                binding.subscribeButton.isEnabled = false
                binding.subscribeButton.text = getString(R.string.loading)
                billingManager.queryProductDetails { productDetails ->
                    if (productDetails != null) {
                        billingManager.launchPurchaseFlow(requireActivity(), productDetails)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.subscription_error),
                            Toast.LENGTH_LONG
                        ).show()
                        binding.subscribeButton.isEnabled = true
                        binding.subscribeButton.text = getString(R.string.become_premium)
                    }
                }
            }
        }
    }

    private fun updateMembershipStatus() {
        val isPremium = SubscriptionManager.isPremium(requireContext())

        if (isPremium) {
            binding.membershipStatus.text = getString(R.string.premium_member)
            binding.subscribeButton.text = getString(R.string.already_subscribed)
            binding.subscribeButton.isEnabled = false
        } else {
            binding.membershipStatus.text = getString(R.string.free_member)
            binding.subscribeButton.text = getString(R.string.become_premium)
            binding.subscribeButton.isEnabled = true
        }
    }

    private fun handlePurchaseState(state: PurchaseState) {
        when (state) {
            is PurchaseState.Purchased -> {
                updateMembershipStatus()
                Toast.makeText(
                    requireContext(),
                    "有料会員になりました！",
                    Toast.LENGTH_LONG
                ).show()
                binding.subscribeButton.isEnabled = true
                binding.subscribeButton.text = getString(R.string.already_subscribed)
            }
            is PurchaseState.Cancelled -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.subscription_cancelled),
                    Toast.LENGTH_SHORT
                ).show()
                binding.subscribeButton.isEnabled = true
                binding.subscribeButton.text = getString(R.string.become_premium)
            }
            is PurchaseState.Error -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.subscription_error) + ": ${state.message}",
                    Toast.LENGTH_LONG
                ).show()
                binding.subscribeButton.isEnabled = true
                binding.subscribeButton.text = getString(R.string.become_premium)
            }
            else -> {
                // NotPurchased などの他の状態では何もしない
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 設定画面では現在のSubscriptionManagerの状態を表示するだけ
        // 購入状態の再確認は購入時のみ行う
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}