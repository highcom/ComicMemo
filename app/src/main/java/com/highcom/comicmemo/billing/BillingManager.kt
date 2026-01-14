package com.highcom.comicmemo.billing

import android.app.Activity
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Google Play Billing管理クラス
 *
 * @param context コンテキスト
 * @param subscriptionId サブスクリプションID（Google Play Consoleで設定したID）
 */
class BillingManager(
    private val context: Context,
    private val subscriptionId: String
) : DefaultLifecycleObserver, PurchasesUpdatedListener, BillingClientStateListener {

    private var billingClient: BillingClient? = null
    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.NotPurchased)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

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
     * 購入状態を確認
     */
    fun queryPurchases() {
        val client = billingClient ?: return

        if (!client.isReady) {
            client.startConnection(this)
            return
        }

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.SkuType.SUBS)
            .build()

        client.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val isPurchased = purchases.any { it.products.contains(subscriptionId) }
                updatePurchaseState(isPurchased)
            }
        }
    }

    /**
     * 購入フローの開始
     *
     * @param activity Activity
     * @param productDetails プロダクト詳細
     */
    fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails) {
        val client = billingClient ?: return

        if (!client.isReady) {
            client.startConnection(this)
            return
        }

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

    /**
     * プロダクト詳細をクエリ
     *
     * @param callback コールバック
     */
    fun queryProductDetails(callback: (ProductDetails?) -> Unit) {
        val client = billingClient ?: return

        if (!client.isReady) {
            client.startConnection(this)
            callback(null)
            return
        }

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(subscriptionId)
                .setProductType(BillingClient.SkuType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        client.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                callback(productDetailsList.firstOrNull())
            } else {
                callback(null)
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    handlePurchase(purchase)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _purchaseState.value = PurchaseState.Cancelled
            }
            else -> {
                _purchaseState.value = PurchaseState.Error(billingResult.debugMessage)
            }
        }
    }

    /**
     * 購入を処理
     */
    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            }
            updatePurchaseState(true)
        }
    }

    /**
     * 購入を承認
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

    /**
     * 購入状態を更新
     */
    private fun updatePurchaseState(isPurchased: Boolean) {
        SubscriptionManager.setPremium(context, isPurchased)
        _purchaseState.value = if (isPurchased) {
            PurchaseState.Purchased
        } else {
            PurchaseState.NotPurchased
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
    object Cancelled : PurchaseState()
    data class Error(val message: String) : PurchaseState()
}
