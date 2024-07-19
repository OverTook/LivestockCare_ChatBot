package com.hci.chatbot

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.google.android.material.snackbar.Snackbar
import com.hci.chatbot.billing.BillingCallback
import com.hci.chatbot.billing.BillingManager
import com.hci.chatbot.network.NetworkManager
import com.hci.chatbot.network.ValidReceptionResponse
import retrofit2.Call
import retrofit2.Callback


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
                showSnackbar("구매가 성공적으로 완료되었습니다.")
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
                    NetworkManager.apiService.validReception(productDetail.toString()).enqueue(object : Callback<ValidReceptionResponse> {
                        override fun onResponse(call: Call<ValidReceptionResponse>, response: retrofit2.Response<ValidReceptionResponse>) {
                            if(!response.isSuccessful) {
                                Log.e("Network", "요청에 실패했습니다.")
                                return
                            }
                        }

                        override fun onFailure(call: Call<ValidReceptionResponse>, err: Throwable) {
                            Snackbar.make(activity!!.findViewById(R.id.main), "영수증 검증을 진행하지 못하였습니다.", Snackbar.LENGTH_LONG).show();
                        }
                    })
                }
            } ?: also {
                showSnackbar("구매 가능 한 상품이 없습니다.")
            }
        }


    }

    private fun getProductDetails() {
        var info = ""
        for (productDetail in mProductDetails) {
            info += "${productDetail.title} \n"
        }
        showSnackbar(info)
    }

    @SuppressLint("SetTextI18n")
    private fun updateSubscriptionState() {
        currentSubscription?.let {
            val purchaseButton = findViewById<LinearLayout>(R.id.purchase_button)
            val itemPrice = findViewById<TextView>(R.id.item_price)
            itemPrice.text = "구독중"
        } ?: also {

        }
    }

    private fun showSnackbar(message: String) {
        val activity = context as? Activity ?: return
        val rootView = activity.findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show()
    }
}
