package com.highcom.comicmemo.billing

import android.app.Activity
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.android.billingclient.api.*
import com.highcom.comicmemo.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Google Play Billing管理クラス
 *
 * @param context コンテキスト
 */
class BillingManager(
    private val context: Context,
) : DefaultLifecycleObserver, PurchasesUpdatedListener, BillingClientStateListener {

    private var billingClient: BillingClient? = null
    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.NotPurchased)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

    private var pendingProductId: String? = null

    init {
        initializeBillingClient()
    }

    /**
     * BillingClientの初期化
     */
    private fun initializeBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient?.startConnection(this)
    }

    /**
     * 購入状態を確認して、月額・年額それぞれのステータスを更新する
     */
    private fun queryPurchases() {
        val client = billingClient ?: return

        if (!client.isReady) {
            client.startConnection(this)
            return
        }

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        client.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val monthlyId = context.getString(R.string.premium_subscription)
                val yearlyId = context.getString(R.string.premium_subscription_yearly)
                val hasMonthly = purchases.any { it.products.contains(monthlyId) }
                val hasYearly = purchases.any { it.products.contains(yearlyId) }
                SubscriptionManager.setPremiumMonthly(context, hasMonthly)
                SubscriptionManager.setPremiumYearly(context, hasYearly)

                if (hasMonthly || hasYearly) {
                    _purchaseState.value = PurchaseState.Purchased
                } else {
                    _purchaseState.value = PurchaseState.NotPurchased
                }
            }
        }
    }

    /**
     * 指定された商品IDの購入フローを開始する
     *
     * @param activity Activity
     * @param productId 商品ID
     */
    fun startPurchaseFlow(activity: Activity, productId: String) {
        val client = billingClient ?: return

        if (!client.isReady) {
            client.startConnection(this)
            _purchaseState.value = PurchaseState.Error("Billing client not ready", productId)
            return
        }

        pendingProductId = productId

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        client.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val productDetails = productDetailsList.firstOrNull()
                if (productDetails != null) {
                    launchPurchaseFlowInternal(activity, productDetails)
                } else {
                    _purchaseState.value = PurchaseState.Error("Product not found on Google Play.", pendingProductId)
                    pendingProductId = null
                }
            } else {
                _purchaseState.value = PurchaseState.Error(billingResult.debugMessage, pendingProductId)
                pendingProductId = null
            }
        }
    }

    /**
     * 内部用の購入フローの開始処理
     *
     * @param activity Activity
     * @param productDetails プロダクト詳細
     */
    private fun launchPurchaseFlowInternal(activity: Activity, productDetails: ProductDetails) {
        val client = billingClient ?: return

        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
            ?: return

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        client.launchBillingFlow(activity, billingFlowParams)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    handlePurchase(purchase)
                }
                pendingProductId = null
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _purchaseState.value = PurchaseState.Cancelled(pendingProductId)
                pendingProductId = null
            }
            else -> {
                _purchaseState.value = PurchaseState.Error(billingResult.debugMessage, pendingProductId)
                pendingProductId = null
            }
        }
    }

    /**
     * 購入を処理する
     */
    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            }
            queryPurchases()
        }
    }

    /**
     * 購入を承認する
     */
    private fun acknowledgePurchase(purchase: Purchase) {
        val client = billingClient ?: return

        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        client.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            // 承認結果の処理
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            queryPurchases()
        }
    }

    override fun onBillingServiceDisconnected() {
        // Billingサービスが切断された場合の処理
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        billingClient?.endConnection()
        billingClient = null
    }
}

/**
 * 購入状態
 */
sealed class PurchaseState {
    object NotPurchased : PurchaseState()
    object Purchased : PurchaseState()
    data class Cancelled(val productId: String?, val timestamp: Long = System.currentTimeMillis()) : PurchaseState()
    data class Error(val message: String, val productId: String?, val timestamp: Long = System.currentTimeMillis()) : PurchaseState()
}
