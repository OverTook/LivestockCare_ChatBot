package com.hci.chatbot.utils

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.hci.chatbot.R
import com.hci.chatbot.chatui.Item
import com.hci.chatbot.chatui.ViewType
import com.hci.chatbot.maps.DiseaseAdapter
import com.hci.chatbot.network.DiseaseListItemData
import com.hci.chatbot.tab.ChatFragment
import com.hci.chatbot.tab.MapFragment
import com.kakao.vectormap.MapGravity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import smartdevelop.ir.eram.showcaseviewlib.GuideView
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType
import smartdevelop.ir.eram.showcaseviewlib.config.PointerType
import java.util.Date

class TutorialUtil private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: TutorialUtil? = null

        fun getInstance(): TutorialUtil =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: TutorialUtil().also { INSTANCE = it }
            }
    }

    private var step: Int = 0
    var mapTutorialing = false
    var chatTutorialing = false

    fun tutorialMap(activity: Activity, rootView: View, callback: () -> Unit) {
        when(step++) {
            0 -> {
                GuideView.Builder(activity)
                    .setTitle(activity.getString(R.string.tutorial_map_seq_0_title))
                    .setContentText(activity.getString(R.string.tutorial_map_seq_0_body))
                    .setGravity(smartdevelop.ir.eram.showcaseviewlib.config.Gravity.center)
                    .setTargetView(rootView.findViewById(R.id.tutorial_empty))
                    .setPointerType(PointerType.none)
                    .setDismissType(DismissType.selfView)
                    .setGuideListener {
                        tutorialMap(activity, rootView, callback)
                    }
                    .build()
                    .show()
            }
            1 -> {
                GuideView.Builder(activity)
                    .setTitle(activity.getString(R.string.tutorial_map_seq_1_title))
                    .setContentText(activity.getString(R.string.tutorial_map_seq_1_body))
                    .setGravity(smartdevelop.ir.eram.showcaseviewlib.config.Gravity.center)
                    .setTargetView(rootView.findViewById(R.id.tutorial_click_zone))
                    .setDismissType(DismissType.targetView)
                    .setGuideListener {
                        tutorialMap(activity, rootView, callback)
                    }
                    .build()
                    .show()
            }
            2 -> {
                val targetView: View = rootView.findViewById(R.id.tutorial_receiving_data)
                targetView.visibility = View.VISIBLE
                
                GuideView.Builder(activity)
                    .setTitle(activity.getString(R.string.tutorial_map_seq_2_title))
                    .setContentText(activity.getString(R.string.tutorial_map_seq_2_body))
                    .setGravity(smartdevelop.ir.eram.showcaseviewlib.config.Gravity.center)
                    .setTargetView(targetView)
                    .setDismissType(DismissType.selfView)
                    .setGuideListener {
                        val addressTextView = targetView.findViewById<TextView>(R.id.title)

                        val recyclerView = targetView.findViewById<RecyclerView>(R.id.disease_recycler_view)
                        recyclerView.layoutManager = LinearLayoutManager(activity)

                        val adapter = DiseaseAdapter(emptyList())
                        recyclerView.adapter = adapter

                        addressTextView.text = activity.getString(R.string.tutorial_map_example_area_text)

                        val list: List<DiseaseListItemData> = listOf(
                            DiseaseListItemData(
                                addressCode = "예시 지역 코드",
                                addressName = "서울특별시 OO구 OO로 XX-XX",
                                dgnssEngn = "",
                                diseaseCode = "",
                                diseaseName = "질병 이름",
                                endDate = Date(),
                                farmName = "농장 이름",
                                id = 1,
                                livestockType = "가축 종류-",
                                occurDate = Date(),
                                occurCount = 0
                            )
                        )
                        adapter.setItems(list)

                        val transition = ChangeBounds()
                        transition.duration = 300
                        transition.interpolator = FastOutSlowInInterpolator()
                        TransitionManager.beginDelayedTransition(targetView.findViewById<LinearLayout>(R.id.innerContainer), transition)

                        val layoutParams = recyclerView.layoutParams
                        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                        recyclerView.layoutParams = layoutParams
                        recyclerView.requestFocus()
                        recyclerView.bringToFront()

                        val bottomSheetView = targetView.parent as? ViewGroup
                        bottomSheetView?.let {
                            TransitionManager.beginDelayedTransition(it, transition)
                            val bottomSheetLayoutParams = it.layoutParams
                            bottomSheetLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT

                            it.layoutParams = bottomSheetLayoutParams
                        }

                        TransitionManager.endTransitions(targetView.findViewById<FrameLayout>(R.id.innerContainer))

                        tutorialMap(activity, rootView, callback)
                    }
                    .build()
                    .show()
            }
            3 -> {
                val targetView: View = rootView.findViewById(R.id.tutorial_receiving_data)

                GuideView.Builder(activity)
                    .setTitle(activity.getString(R.string.tutorial_map_seq_3_title))
                    .setContentText(activity.getString(R.string.tutorial_map_seq_3_body))
                    .setGravity(smartdevelop.ir.eram.showcaseviewlib.config.Gravity.center)
                    .setTargetView(targetView)
                    .setDismissType(DismissType.selfView)
                    .setGuideListener {
                        val addressTextView = targetView.findViewById<TextView>(R.id.title)

                        val recyclerView = targetView.findViewById<RecyclerView>(R.id.disease_recycler_view)
                        recyclerView.layoutManager = LinearLayoutManager(activity)

                        val adapter = DiseaseAdapter(emptyList())
                        recyclerView.adapter = adapter

                        addressTextView.text = activity.getString(R.string.receiving_data)
                        targetView.visibility = View.GONE

                        tutorialMap(activity, rootView, callback)
                    }
                    .build()
                    .show()
            }
            4 -> {
                GuideView.Builder(activity)
                    .setTitle(activity.getString(R.string.tutorial_map_seq_4_title))
                    .setContentText(activity.getString(R.string.tutorial_map_seq_4_body))
                    .setGravity(smartdevelop.ir.eram.showcaseviewlib.config.Gravity.center)
                    .setTargetView(rootView.findViewById(R.id.tutorial_click_zone))
                    .setDismissType(DismissType.targetView)
                    .setGuideListener {
                        tutorialMap(activity, rootView, callback)
                    }
                    .build()
                    .show()
            }
            5 -> {
                val targetView: View = rootView.findViewById(R.id.tutorial_receiving_data)
                targetView.visibility = View.VISIBLE

                GuideView.Builder(activity)
                    .setTitle(activity.getString(R.string.tutorial_map_seq_5_title))
                    .setContentText(activity.getString(R.string.tutorial_map_seq_5_body))
                    .setGravity(smartdevelop.ir.eram.showcaseviewlib.config.Gravity.center)
                    .setTargetView(targetView)
                    .setDismissType(DismissType.selfView)
                    .setGuideListener {
                        targetView.visibility = View.GONE
                        (activity as AppCompatActivity).lifecycleScope.launch {
                            delay(2000)
                            tutorialMap(activity, rootView, callback)
                        }
                    }
                    .build()
                    .show()
            }
            6 -> {
                GuideView.Builder(activity)
                    .setTitle(activity.getString(R.string.tutorial_map_seq_6_title))
                    .setContentText(activity.getString(R.string.tutorial_map_seq_6_body))
                    .setGravity(smartdevelop.ir.eram.showcaseviewlib.config.Gravity.center)
                    .setTargetView(rootView.findViewById(R.id.tutorial_empty))
                    .setPointerType(PointerType.none)
                    .setDismissType(DismissType.selfView)
                    .setGuideListener {
                        tutorialMap(activity, rootView, callback)
                    }
                    .build()
                    .show()
            }
            7 -> {
                GuideView.Builder(activity)
                    .setTitle(activity.getString(R.string.tutorial_map_seq_7_title))
                    .setContentText(activity.getString(R.string.tutorial_map_seq_7_body))
                    .setGravity(smartdevelop.ir.eram.showcaseviewlib.config.Gravity.center)
                    .setTargetView(rootView.findViewById(R.id.tutorial_click_zone))
                    .setDismissType(DismissType.targetView)
                    .setGuideListener {
                        tutorialMap(activity, rootView, callback)
                    }
                    .build()
                    .show()
            }
            8 -> {
                val targetView: View = rootView.findViewById(R.id.tutorial_receiving_data)
                targetView.visibility = View.VISIBLE

                GuideView.Builder(activity)
                    .setTitle(activity.getString(R.string.tutorial_map_seq_8_title))
                    .setContentText(activity.getString(R.string.tutorial_map_seq_8_body))
                    .setGravity(smartdevelop.ir.eram.showcaseviewlib.config.Gravity.center)
                    .setTargetView(targetView)
                    .setDismissType(DismissType.selfView)
                    .setGuideListener {
                        targetView.visibility = View.GONE
                        Toast.makeText(activity, activity.getString(R.string.no_disease_at_area), Toast.LENGTH_LONG).show()

                        (activity as AppCompatActivity).lifecycleScope.launch {
                            delay(2500)
                            tutorialMap(activity, rootView, callback)
                        }
                    }
                    .build()
                    .show()
            }
            9 -> {
                (rootView.tag as MapFragment).getCurrentLocation()

                GuideView.Builder(activity)
                    .setTitle(activity.getString(R.string.tutorial_map_seq_9_title))
                    .setContentText(activity.getString(R.string.tutorial_map_seq_9_body))
                    .setGravity(smartdevelop.ir.eram.showcaseviewlib.config.Gravity.center)
                    .setTargetView(rootView.findViewById(R.id.tutorial_empty))
                    .setPointerType(PointerType.none)
                    .setDismissType(DismissType.selfView)
                    .setGuideListener {
                        mapTutorialing = false
                        step = 0
                        callback()
                    }
                    .build()
                    .show()
            }
        }
    }

    fun tutorialChat(activity: Activity, rootView: View, callback: () -> Unit) {
        when(step++) {
            0 -> {
                GuideView.Builder(activity)
                    .setTitle(activity.getString(R.string.tutorial_chat_seq_0_title))
                    .setContentText(activity.getString(R.string.tutorial_chat_seq_0_body))
                    .setGravity(smartdevelop.ir.eram.showcaseviewlib.config.Gravity.center)
                    .setTargetView(rootView.findViewById(R.id.tutorial_empty))
                    .setPointerType(PointerType.none)
                    .setDismissType(DismissType.selfView)
                    .setGuideListener {
                        tutorialChat(activity, rootView, callback)
                    }
                    .build()
                    .show()
            }
            1 -> {
                GuideView.Builder(activity)
                    .setTitle(activity.getString(R.string.tutorial_chat_seq_1_title))
                    .setContentText(activity.getString(R.string.tutorial_chat_seq_1_body))
                    .setGravity(smartdevelop.ir.eram.showcaseviewlib.config.Gravity.center)
                    .setTargetView(rootView.findViewById(R.id.hello_animation))
                    .setDismissType(DismissType.targetView)
                    .setGuideListener {
                        tutorialChat(activity, rootView, callback)
                    }
                    .build()
                    .show()
            }
            2 -> {
                val chatFragment: ChatFragment = rootView.tag as ChatFragment

                val targetView = chatFragment.tutorialSimulate(0)!!
                targetView.show()

                GuideView.Builder(activity)
                    .setTitle(activity.getString(R.string.tutorial_chat_seq_2_title))
                    .setContentText(activity.getString(R.string.tutorial_chat_seq_2_body))
                    .setGravity(smartdevelop.ir.eram.showcaseviewlib.config.Gravity.center)
                    .setTargetView(targetView)
                    .setDismissType(DismissType.selfView)
                    .setGuideListener {
                        tutorialChat(activity, rootView, callback)
                    }
                    .build()
                    .show()
            }
            3 -> {
                val chatFragment: ChatFragment = rootView.tag as ChatFragment
                val targetView = chatFragment.tutorialSimulate(1)!!
                targetView.show()

                GuideView.Builder(activity)
                    .setTitle(activity.getString(R.string.tutorial_chat_seq_3_title))
                    .setContentText(activity.getString(R.string.tutorial_chat_seq_3_body))
                    .setGravity(smartdevelop.ir.eram.showcaseviewlib.config.Gravity.center)
                    .setTargetView(targetView)
                    .setDismissType(DismissType.selfView)
                    .setGuideListener {
                        tutorialChat(activity, rootView, callback)
                    }
                    .build()
                    .show()
            }
            4 -> {
                val chatFragment: ChatFragment = rootView.tag as ChatFragment
                val targetView = chatFragment.tutorialSimulate(2)!!
                targetView.show()

                GuideView.Builder(activity)
                    .setTitle(activity.getString(R.string.tutorial_chat_seq_4_title))
                    .setContentText(activity.getString(R.string.tutorial_chat_seq_4_body))
                    .setGravity(smartdevelop.ir.eram.showcaseviewlib.config.Gravity.center)
                    .setTargetView(targetView)
                    .setDismissType(DismissType.selfView)
                    .setGuideListener {
                        chatFragment.tutorialSimulate(-1)
                        tutorialChat(activity, rootView, callback)
                    }
                    .build()
                    .show()
            }
            5 -> {
                GuideView.Builder(activity)
                    .setTitle(activity.getString(R.string.tutorial_chat_seq_5_title))
                    .setContentText(activity.getString(R.string.tutorial_chat_seq_5_body))
                    .setGravity(smartdevelop.ir.eram.showcaseviewlib.config.Gravity.center)
                    .setTargetView(rootView.findViewById(R.id.relativeLayout))
                    .setPointerType(PointerType.none)
                    .setDismissType(DismissType.selfView)
                    .setGuideListener {
                        tutorialChat(activity, rootView, callback)
                    }
                    .build()
                    .show()
            }
            6 -> {
                val chatFragment: ChatFragment = rootView.tag as ChatFragment
                chatFragment.tutorialSimulate(3)

                GuideView.Builder(activity)
                    .setTitle(activity.getString(R.string.tutorial_chat_seq_6_title))
                    .setContentText(activity.getString(R.string.tutorial_chat_seq_6_body))
                    .setGravity(smartdevelop.ir.eram.showcaseviewlib.config.Gravity.center)
                    .setTargetView(rootView.findViewById(R.id.chat_send_btn))
                    .setDismissType(DismissType.targetView)
                    .setGuideListener {
                        tutorialChat(activity, rootView, callback)
                    }
                    .build()
                    .show()
            }
            7 -> {
                val chatFragment: ChatFragment = rootView.tag as ChatFragment
                val targetView = chatFragment.tutorialSimulate(4)!!
                targetView.show()

                GuideView.Builder(activity)
                    .setTitle(activity.getString(R.string.tutorial_chat_seq_7_title))
                    .setContentText(activity.getString(R.string.tutorial_chat_seq_7_body))
                    .setGravity(smartdevelop.ir.eram.showcaseviewlib.config.Gravity.center)
                    .setTargetView(targetView)
                    .setDismissType(DismissType.selfView)
                    .setGuideListener {
                        tutorialChat(activity, rootView, callback)
                    }
                    .build()
                    .show()
            }
            8 -> {
                val chatFragment: ChatFragment = rootView.tag as ChatFragment
                chatFragment.tutorialSimulate(5)

                (activity as FragmentActivity).lifecycleScope.launch {
                    delay(4000)

                    GuideView.Builder(activity)
                        .setTitle(activity.getString(R.string.tutorial_chat_seq_8_title))
                        .setContentText(activity.getString(R.string.tutorial_chat_seq_8_body))
                        .setGravity(smartdevelop.ir.eram.showcaseviewlib.config.Gravity.center)
                        .setTargetView(rootView.findViewById(R.id.tutorial_empty))
                        .setPointerType(PointerType.none)
                        .setDismissType(DismissType.selfView)
                        .setGuideListener {
                            tutorialChat(activity, rootView, callback)
                        }
                        .build()
                        .show()
                }
            }
            9 -> {
                GuideView.Builder(activity)
                    .setTitle(activity.getString(R.string.tutorial_chat_seq_9_title))
                    .setContentText(activity.getString(R.string.tutorial_chat_seq_9_body))
                    .setGravity(smartdevelop.ir.eram.showcaseviewlib.config.Gravity.center)
                    .setTargetView(rootView.findViewById(R.id.tutorial_empty))
                    .setPointerType(PointerType.none)
                    .setDismissType(DismissType.selfView)
                    .setGuideListener {
                        tutorialChat(activity, rootView, callback)
                    }
                    .build()
                    .show()
            }
            10 -> {
                GuideView.Builder(activity)
                    .setTitle(activity.getString(R.string.tutorial_chat_seq_10_title))
                    .setContentText(activity.getString(R.string.tutorial_chat_seq_10_body))
                    .setGravity(smartdevelop.ir.eram.showcaseviewlib.config.Gravity.center)
                    .setTargetView(rootView.findViewById(R.id.tutorial_empty))
                    .setPointerType(PointerType.none)
                    .setDismissType(DismissType.selfView)
                    .setGuideListener {
                        tutorialChat(activity, rootView, callback)
                    }
                    .build()
                    .show()
            }
            11 -> {
                val chatFragment: ChatFragment = rootView.tag as ChatFragment
                chatFragment.tutorialSimulate(-2)
                GuideView.Builder(activity)
                    .setTitle(activity.getString(R.string.tutorial_chat_seq_11_title))
                    .setContentText(activity.getString(R.string.tutorial_chat_seq_11_body))
                    .setGravity(smartdevelop.ir.eram.showcaseviewlib.config.Gravity.center)
                    .setTargetView(rootView.findViewById(R.id.tutorial_empty))
                    .setPointerType(PointerType.none)
                    .setDismissType(DismissType.selfView)
                    .setGuideListener {
                        chatTutorialing = false
                        step = 0
                        callback()
                    }
                    .build()
                    .show()
            }
        }
    }

    private fun ChatFragment.tutorialSimulate(step: Int): View? {
        when(step) {
            -1 -> {
                viewOfLayout.findViewById<RelativeLayout>(R.id.no_text_alert_layout).visibility = View.VISIBLE
                for (i in 0 until 3) {
                    val childView = recyclerView.layoutManager!!.getChildAt(i)!!
                    childView.hide() //숨긴 상태에서 원 크기르 복구하기
                    val param = childView.layoutParams
                    param.height = LayoutParams.WRAP_CONTENT
                    childView.layoutParams = param
                }
                adapter.clearItems()

                adapter.addItem(Item(getString(R.string.tutorial_chat_seq_6_prompt_0), getCurrentTime(), ViewType.RIGHT_CHAT, false))
                adapter.addItem(Item(getString(R.string.tutorial_chat_seq_6_prompt_1), getCurrentTime(), ViewType.LEFT_CHAT, false))
                return null
            }
            -2 -> {
                viewOfLayout.findViewById<EditText>(R.id.chat_input).isEnabled = true

                viewOfLayout.findViewById<RelativeLayout>(R.id.no_text_alert_layout).visibility = View.VISIBLE
                for (i in 0 until recyclerView.layoutManager!!.childCount) {
                    val childView = recyclerView.layoutManager!!.getChildAt(i)!!
                    val param = childView.layoutParams
                    param.height = LayoutParams.WRAP_CONTENT
                    childView.layoutParams = param
                }
                adapter.clearItems()
                return null
            }
            0 -> {
                viewOfLayout.findViewById<EditText>(R.id.chat_input).isEnabled = false

                viewOfLayout.findViewById<RelativeLayout>(R.id.no_text_alert_layout).visibility = View.GONE
                return recyclerView.findViewHolderForAdapterPosition(0)!!.itemView
            }
            1 -> {
                return recyclerView.findViewHolderForAdapterPosition(1)!!.itemView
            }
            2 -> {
                recyclerView.findViewHolderForAdapterPosition(1)!!.itemView.hide()
                return recyclerView.findViewHolderForAdapterPosition(2)!!.itemView
            }
            3 -> {
                viewOfLayout.findViewById<EditText>(R.id.chat_input).setText(getString(R.string.tutorial_chat_seq_6_prompt_0))
                return null
            }
            4 -> {
                viewOfLayout.findViewById<RelativeLayout>(R.id.no_text_alert_layout).visibility = View.GONE

                viewOfLayout.findViewById<EditText>(R.id.chat_input).text.clear()
                recyclerView.findViewHolderForAdapterPosition(0)!!.itemView.show()
                return recyclerView.findViewHolderForAdapterPosition(1)!!.itemView
            }
            5 -> {

                adapter.addItem(Item(getString(R.string.tutorial_chat_seq_6_prompt_2), getCurrentTime(), ViewType.RIGHT_CHAT, true))
                adapter.addItem(Item(getString(R.string.tutorial_chat_seq_6_prompt_3), getCurrentTime(), ViewType.LEFT_CHAT, true))

                recyclerView.smoothScrollToPosition(3)
                return null
            }
            else -> {
                return null
            }
        }
    }
}