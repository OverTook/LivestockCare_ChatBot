package com.hci.chatbot

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.google.android.material.snackbar.Snackbar
import com.hci.chatbot.billing.BillingCallback
import com.hci.chatbot.billing.BillingManager
import com.hci.chatbot.network.CheckSubscriptionStatusResponse
import com.hci.chatbot.network.NetworkManager
import okhttp3.Callback
import retrofit2.Call


class BillingDialog(context: Context, private val activity: Activity) : Dialog(context) {

    private lateinit var manager: BillingManager
    val subsItemID = "kuvet_sub"

    private var mProductDetails = listOf<ProductDetails>()
        set(value) {
            field = value
            getProductDetails()
        }

    private var currentSubscription: Purchase? = null
        set(value) {
            field = value
            updateSubscriptionState()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.requestFeature(Window.FEATURE_NO_TITLE)

        setContentView(R.layout.billing_popup)

        // Dialog를 전체 화면으로 설정
        window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val dialogButton = findViewById<ImageButton>(R.id.close_btn)

        dialogButton.setOnClickListener {
            dismiss()
        }

        manager = BillingManager(activity, object : BillingCallback {
            override fun onBillingConnected() {
                manager.getProductDetails(subsItemID, billingType = BillingClient.ProductType.SUBS) { list ->
                    mProductDetails = list
                    Log.d("DEBUG", "Received product details: $mProductDetails")
                }
                manager.checkSubscribed(subsItemID) {
                    currentSubscription = it
                }
            }

            override fun onSuccess(purchase: Purchase) {
                currentSubscription = purchase
                Log.d("DEBUG", "Purchase successful: $purchase")
                showSnackbar("구매가 성공적으로 완료되었습니다.")
                this@BillingDialog.dismiss()
                activity.finish()
            }

            override fun onFailure(responseCode: Int) {
                showSnackbar("구매 도중 오류가 발생하였습니다.")
            }
        })

        val purchaseButton = findViewById<LinearLayout>(R.id.purchase_button)


        purchaseButton.setOnClickListener {
            mProductDetails.find { it.productId == subsItemID }?.let { productDetail ->
                Log.d("DEBUG", "product -> ${productDetail.toString()}")
                val selectedOfferToken = productDetail.subscriptionOfferDetails?.getOrNull(0)?.offerToken
                Log.d("DEBUG", "offerToken -> $selectedOfferToken")
                if (selectedOfferToken != null) {
                    manager.purchaseProduct(productDetail, selectedOfferToken)

                }
            } ?: also {
                showSnackbar("구매 가능 한 상품이 없습니다.")
            }
        }

        NetworkManager.apiService.checkSubscriptionStatus().enqueue(object : retrofit2.Callback<CheckSubscriptionStatusResponse> {
            override fun onResponse(call: Call<CheckSubscriptionStatusResponse>, response: retrofit2.Response<CheckSubscriptionStatusResponse>) {
                if (!response.isSuccessful) {
                    Log.e("Network", "요청에 실패했습니다.")
                    return
                }

                Log.d("DEBUG", response.body()?.subscriptionStatus.toString())

                if (response.body()?.subscriptionStatus == "active") {
                    val purchaseButton = findViewById<LinearLayout>(R.id.purchase_button)
                    val itemPrice = findViewById<TextView>(R.id.item_price)
                    itemPrice.text = "구독중"
                    purchaseButton.setOnClickListener(null)
                    purchaseButton.isClickable = false
                } else {
                    val itemPrice = findViewById<TextView>(R.id.item_price)
                    itemPrice.text = "₩5,900"
                }
            }

            override fun onFailure(call: Call<CheckSubscriptionStatusResponse>, err: Throwable) {
                showSnackbar("영수증 검증을 진행하지 못하였습니다.")
            }
        })
    }

    private fun getProductDetails() {
        var info = ""
        for (productDetail in mProductDetails) {
            info += "${productDetail.title} \n"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateSubscriptionState() {
        currentSubscription?.let {
            val purchaseButton = findViewById<LinearLayout>(R.id.purchase_button)
            val itemPrice = findViewById<TextView>(R.id.item_price)
            itemPrice.text = "구독중"
            purchaseButton.setOnClickListener(null)
            purchaseButton.isClickable = false
        } ?: also {
            val purchaseButton = findViewById<LinearLayout>(R.id.purchase_button)
            val itemPrice = findViewById<TextView>(R.id.item_price)
            itemPrice.text = "₩5,900"
        }
    }

    private fun showSnackbar(message: String) {
        Log.d("SNACKBAR", message)
        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show()
    }
}
