package com.highcom.comicmemo.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import androidx.core.net.toUri

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
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {}

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

        // BillingManagerの初期化とライフサイクル監視
        billingManager = BillingManager(requireContext())
        viewLifecycleOwner.lifecycle.addObserver(billingManager)

        // 現在のステータスを表示
        updateMembershipStatus()

        // 購入状態の監視
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                billingManager.purchaseState.collect { state -> handlePurchaseState(state) }
            }
        }

        // 月額購入ボタンのクリックリスナー
        binding.subscribeButtonMonthly.setOnClickListener {
            if (!SubscriptionManager.isPremium(requireContext())) {
                binding.subscribeButtonMonthly.isEnabled = false
                binding.subscribeButtonMonthly.text = getString(R.string.loading)
                billingManager.startPurchaseFlow(requireActivity(), getString(R.string.premium_subscription))
            }
        }

        // 年額購入ボタンのクリックリスナー
        binding.subscribeButtonYearly.setOnClickListener {
            if (!SubscriptionManager.isPremium(requireContext())) {
                binding.subscribeButtonYearly.isEnabled = false
                binding.subscribeButtonYearly.text = getString(R.string.loading)
                billingManager.startPurchaseFlow(requireActivity(), getString(R.string.premium_subscription_yearly))
            }
        }

        // 広告非表示ボタンのクリックリスナー
        binding.hideAdsButton.setOnClickListener {
            if (!SubscriptionManager.isPremium(requireContext())) {
                binding.hideAdsButton.isEnabled = false
                binding.hideAdsButton.text = getString(R.string.loading)
                billingManager.startPurchaseFlow(requireActivity(), getString(R.string.hide_ads_in_app))
            }
        }

        // 購入の復元ボタンのクリックリスナー
        binding.restoreButton.setOnClickListener {
            billingManager.restorePurchases()
        }

        // プライバシーポリシーボタンのクリックリスナー
        binding.privacyPolicyButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, getString(R.string.privacy_policy_url).toUri())
            startActivity(intent)
        }

        // アプリ評価ボタンのクリックリスナー
        binding.appEvaluationButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, getString(R.string.app_store_url).toUri())
            startActivity(intent)
        }

        // 機能説明リンクのクリックリスナー
        binding.featureDescriptionLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, getString(R.string.barcode_feature_url).toUri())
            startActivity(intent)
        }
    }

    /**
     * 有料会員状態表示変更処理
     */
    private fun updateMembershipStatus() {
        val isPremiumMonthly = SubscriptionManager.isPremiumMonthly(requireContext())
        val isPremiumYearly = SubscriptionManager.isPremiumYearly(requireContext())
        val isHideAds = SubscriptionManager.isHideAds(requireContext())

        if (isPremiumMonthly) {
            binding.membershipStatus.text = getString(R.string.premium_member_monthly)
        } else if (isPremiumYearly) {
            binding.membershipStatus.text = getString(R.string.premium_member_yearly)
        } else {
            binding.membershipStatus.text = getString(R.string.free_member)
        }

        if (isPremiumMonthly || isPremiumYearly) {
            binding.subscribeButtonMonthly.text = getString(R.string.already_subscribed)
            binding.subscribeButtonYearly.text = getString(R.string.already_subscribed)
            binding.subscribeButtonMonthly.isEnabled = false
            binding.subscribeButtonYearly.isEnabled = false
        } else {
            binding.subscribeButtonMonthly.text = getString(R.string.become_premium_monthly)
            binding.subscribeButtonYearly.text = getString(R.string.become_premium_yearly)
            binding.subscribeButtonMonthly.isEnabled = true
            binding.subscribeButtonYearly.isEnabled = true
        }

        if (isHideAds) {
            binding.hideAdsButton.text = getString(R.string.already_subscribed)
            binding.hideAdsButton.isEnabled = false
        } else {
            binding.hideAdsButton.text = getString(R.string.hide_ads_price)
            binding.hideAdsButton.isEnabled = true
        }
    }

    /**
     * 購入状態を処理し、UIを更新する
     *
     * @param state 購入状態
     */
    private fun handlePurchaseState(state: PurchaseState) {
        when (state) {
            is PurchaseState.Purchased -> {
                updateMembershipStatus()
            }
            is PurchaseState.Cancelled -> {
                Toast.makeText(requireContext(), getString(R.string.subscription_cancelled), Toast.LENGTH_SHORT).show()
                resetButtonState(state.productId)
            }
            is PurchaseState.Error -> {
                Toast.makeText(requireContext(), getString(R.string.subscription_error) + ": ${state.message}", Toast.LENGTH_LONG).show()
                resetButtonState(state.productId)
            }
            else -> {
                // NotPurchased などの他の状態では何もしない
            }
        }
    }

    /**
     * 指定された商品IDのボタンの状態を元に戻す
     *
     * @param productId 商品ID
     */
    private fun resetButtonState(productId: String?) {
        when (productId) {
            getString(R.string.premium_subscription) -> {
                binding.subscribeButtonMonthly.isEnabled = true
                binding.subscribeButtonMonthly.text = getString(R.string.become_premium_monthly)
            }
            getString(R.string.premium_subscription_yearly) -> {
                binding.subscribeButtonYearly.isEnabled = true
                binding.subscribeButtonYearly.text = getString(R.string.become_premium_yearly)
            }
            getString(R.string.hide_ads_in_app) -> {
                binding.hideAdsButton.isEnabled = true
                binding.hideAdsButton.text = getString(R.string.hide_ads_price)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}