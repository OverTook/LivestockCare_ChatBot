package com.hci.chatbot.billing

import android.app.Activity
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.google.android.material.snackbar.Snackbar
import com.hci.chatbot.R
import com.hci.chatbot.network.NetworkManager
import com.hci.chatbot.network.SaveReceiptRequest
import com.hci.chatbot.network.SaveReceiptResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback

interface BillingCallback {
    fun onBillingConnected()
    fun onSuccess(purchase: Purchase)
    fun onFailure(responseCode: Int)
}

class BillingManager(private val activity: Activity, private val callback: BillingCallback) {
    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        Log.d("BillingManager", "purchasesUpdatedListener called")
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                confirmPurchase(purchase)
                val purchaseToken = purchase.purchaseToken
                val productId = purchase.products[0]
                verifyPurchaseOnServer(productId, purchaseToken)
            }
        } else {
            callback.onFailure(billingResult.responseCode)
        }
    }

    private val billingClient = BillingClient.newBuilder(activity)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    init {
        billingClient.startConnection(object: BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                Log.d("BiilingManager", "== BillingClient onBillingServiceDisconnected() called ==")
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    callback.onBillingConnected()
                } else {
                    callback.onFailure(billingResult.responseCode)
                }
            }
        })
    }

    /**
     * 콘솔에 등록된 상품 리스트 불러오기
     * @param product 상품 ID
     * @param billingType IN_APP or SUBS
     * @param resultBlock 결과로 받을 상품 정보들에 대한 처리
     */
    fun getProductDetails(
        product: String,
        billingType: String,
        resultBlock: (List<ProductDetails>) -> Unit = {}
    ) {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(product)
                .setProductType(billingType)
                .build()
        )
        Log.d("DEBUG", "lists -> ${productList[0]}")
        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()

        Log.d("DEBUG", "params -> $params")

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            Log.d("DEBUG", "billingResult -> $productDetailsList")
            CoroutineScope(Dispatchers.Main).launch {
                resultBlock(productDetailsList ?: emptyList())
            }
            // Process the result
        }
    }

    //billingClient.queryProductDetailsAsync(params) { _, list ->
    //    CoroutineScope(Dispatchers.Main).launch {
    //        resultBlock(list ?: emptyList())
    //    }
    //}


    /**
     * 구매 시도
     * @param productDetail 구매 할 상품
     * @param selectedOfferToken 구독 상품일 경우 오퍼 토큰
     */
    fun purchaseProduct(productDetail: ProductDetails, selectedOfferToken: String) {
        val productDetailsParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetail)

        // 구독 상품인 경우에만 오퍼 토큰 설정
        selectedOfferToken?.let {
            productDetailsParamsBuilder.setOfferToken(it)
        }

        val productDetailsParamsList = listOf(productDetailsParamsBuilder.build())

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val responseCode = billingClient.launchBillingFlow(activity, flowParams).responseCode
        if (responseCode != BillingClient.BillingResponseCode.OK) {
            callback.onFailure(responseCode)
        }
    }

    /**
     * 구독 여부 확인
     * @param product 구매 확인 상품
     * @param resultBlock 구매 확인 상품에 대한 처리
     */
    fun checkSubscribed(product: String, resultBlock: (Purchase?) -> Unit) {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(product)
                .build()
        ) { _, purchases ->
            CoroutineScope(Dispatchers.Main).launch {
                for (purchase in purchases) {
                    if (purchase.isAcknowledged && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        return@launch resultBlock(purchase)
                    }
                }
                return@launch resultBlock(null)
            }
        }
    }

    /**
     * 구매 확인
     * @param purchase
     */
    private fun confirmPurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
            // 구매를 완료했지만 확인이 안된 경우 확인 처리
            val ackPurchaseParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken)

            CoroutineScope(Dispatchers.IO).launch {
                billingClient.acknowledgePurchase(ackPurchaseParams.build()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        if (it.responseCode == BillingClient.BillingResponseCode.OK) {
                            callback.onSuccess(purchase)
                        } else {
                            callback.onFailure(it.responseCode)
                        }
                    }
                }
            }
        }
    }

    /**
     * 구매 확인이 안 된 경우 다시 확인
     */
    fun onResume(type: String) {
        if (billingClient.isReady) {
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(type)
                    .build()
            ) { _, purchases ->
                for (purchase in purchases) {
                    if (!purchase.isAcknowledged && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        confirmPurchase(purchase)
                    }
                }
            }
        }
    }

    /**
     * 구매를 서버에서 검증
     * @param productId 구매한 제품 ID
     * @param purchaseToken 구매 토큰
     */
    private fun verifyPurchaseOnServer(productId: String, purchaseToken: String) {
        NetworkManager.apiService.saveReceipt(SaveReceiptRequest(productId, purchaseToken)).enqueue(object : Callback<SaveReceiptResponse> {
            override fun onResponse(call: Call<SaveReceiptResponse>, response: retrofit2.Response<SaveReceiptResponse>) {
                if(!response.isSuccessful) {
                    Log.e("Network", "요청에 실패했습니다.")
                    return
                }
            }

            override fun onFailure(call: Call<SaveReceiptResponse>, err: Throwable) {
                Snackbar.make(activity.findViewById(R.id.main), "영수증 검증을 진행하지 못하였습니다.", Snackbar.LENGTH_LONG).show();
            }
        })
    }
}